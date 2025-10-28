package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseOrderManager {
    private static final String TAG = "SupabaseOrderManager";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static SupabaseOrderManager instance;
    private final SupabaseClient supabaseClient;
    private final Gson gson;

    private SupabaseOrderManager(Context context) {
        this.supabaseClient = SupabaseClient.getInstance(context);
        this.gson = new Gson();
    }

    public static synchronized SupabaseOrderManager getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseOrderManager(context);
        }
        return instance;
    }

    // Criar pedido no Supabase
    public Call createOrder(OrderRequest orderRequest, String accessToken, OrderCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Criar Order a partir do OrderRequest
        Order order = new Order();
        order.setStudentId(orderRequest.getStudentId());
        order.setStudentName(orderRequest.getStudentName());

        // Adicionar itens ao pedido (usando dados do OrderRequest, não do OrderManager)
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            // ⭐ LOG 1: Verificar dados do ItemRequest
            Log.d(TAG, "========== ITEM REQUEST ==========");
            Log.d(TAG, "Produto ID: " + itemRequest.getProductId());
            Log.d(TAG, "Nome: " + itemRequest.getProductName());
            Log.d(TAG, "Preço: " + itemRequest.getPrice());
            Log.d(TAG, "Quantidade: " + itemRequest.getQuantity());

            // Criar OrderItem diretamente do ItemRequest (sem buscar do OrderManager)
            OrderItem item = new OrderItem(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    itemRequest.getPrice()
            );

            // ⭐ LOG 2: Verificar OrderItem criado
            Log.d(TAG, "========== ORDER ITEM ==========");
            Log.d(TAG, "Preço item (getPrice): " + item.getPrice());
            Log.d(TAG, "Quantidade: " + item.getQuantity());
            Log.d(TAG, "Subtotal: " + item.getSubtotal());
            order.addItem(item);
        }

        // Validar estoque antes de enviar (OPCIONAL - se quiser manter validação)
        // String stockError = OrderManager.validateStock(order);
        // if (stockError != null) {
        //     callback.onError(stockError);
        //     return null;
        // }

        // Converter para formato do Supabase - SEM nome_usuario
        OrderSupabaseRequest supabaseRequest = new OrderSupabaseRequest(order);
        String json = gson.toJson(supabaseRequest);
        Log.d(TAG, "Criando pedido: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos")
                .post(body)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao criar pedido", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta criar pedido - Código: " + response.code());
                Log.d(TAG, "Resposta criar pedido - Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        if (ordersResponse != null && !ordersResponse.isEmpty()) {
                            OrderSupabaseResponse orderResponse = ordersResponse.get(0);
                            Order createdOrder = convertSupabaseResponseToOrder(orderResponse);

                            // ⭐ CORREÇÃO CRÍTICA: Copiar itens do order original para o createdOrder
                            Log.d(TAG, "Copiando " + order.getItems().size() + " itens para o pedido criado");
                            for (OrderItem item : order.getItems()) {
                                createdOrder.addItem(item);
                            }

                            // Criar itens do pedido (agora createdOrder tem os itens)
                            createOrderItems(String.valueOf(createdOrder.getId()), createdOrder.getItems(), accessToken, callback, createdOrder);
                        } else {
                            callback.onError("Resposta inválida do servidor");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar resposta", e);
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    callback.onError("Erro ao criar pedido: " + response.code());
                }
            }
        });

        return call;
    }

    private void createOrderItems(String orderId, List<OrderItem> items, String accessToken,
                                  OrderCallback callback, Order createdOrder) {

        List<OrderItemSupabaseRequest> itemsRequest = new ArrayList<>();
        for (OrderItem item : items) {
            // ⭐ LOG 3: Verificar item ANTES de criar o request
            Log.d(TAG, "========== ANTES DO REQUEST ==========");
            Log.d(TAG, "Item: " + item.getProductName());
            Log.d(TAG, "Preço (item.getPrice()): " + item.getPrice());

            OrderItemSupabaseRequest itemRequest = new OrderItemSupabaseRequest(orderId, item);

            // ⭐ LOG 4: Verificar o request DEPOIS de criado
            Log.d(TAG, "========== DEPOIS DO REQUEST ==========");
            Log.d(TAG, "precoProduto: " + itemRequest.precoProduto);
            Log.d(TAG, "quantidade: " + itemRequest.quantidade);

            itemsRequest.add(itemRequest);
        }

        String json = gson.toJson(itemsRequest);
        // ⭐ LOG 5: JSON COMPLETO que será enviado
        Log.d(TAG, "========== JSON FINAL ==========");
        Log.d(TAG, json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/itens_pedido")
                .post(body)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao salvar itens do pedido", e);
                    callback.onError("Erro ao salvar itens do pedido");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                // ⭐ LOG 6: Resposta do servidor
                Log.d(TAG, "========== RESPOSTA CRIAR ITENS ==========");
                Log.d(TAG, "Código: " + response.code());
                Log.d(TAG, "Body: " + responseBody);

                if (response.isSuccessful()) {
                    // ⭐ NOTA: Não atualizamos mais o estoque do OrderManager
                    // pois ele tem dados desatualizados. O estoque deve ser
                    // gerenciado no banco de dados (Supabase)
                    createdOrder.setStatus(OrderManager.STATUS_CONFIRMED);
                    callback.onSuccess(createdOrder);
                } else {
                    callback.onError("Erro ao salvar itens do pedido: " + response.code());
                }
            }
        });
    }

    // Buscar pedidos do estudante
    public Call getStudentOrders(String studentId, String accessToken, OrdersCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id_usuario=eq." + studentId + "&order=id.desc")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        List<Order> orders = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                orders.add(convertSupabaseResponseToOrder(orderResponse));
                            }
                        }

                        callback.onSuccess(orders);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar pedidos", e);
                        callback.onError("Erro ao processar pedidos");
                    }
                } else {
                    callback.onError("Erro ao buscar pedidos: " + response.code());
                }
            }
        });

        return call;
    }

    // Atualizar status do pedido
    public Call updateOrderStatus(String orderId, String newStatus, String accessToken, OrderCallback callback) {
        StatusUpdateRequest statusRequest = new StatusUpdateRequest(newStatus);
        String json = gson.toJson(statusRequest);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id=eq." + orderId)
                .patch(body)
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                if (response.isSuccessful()) {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        if (ordersResponse != null && !ordersResponse.isEmpty()) {
                            Order updatedOrder = convertSupabaseResponseToOrder(ordersResponse.get(0));
                            callback.onSuccess(updatedOrder);
                        } else {
                            callback.onError("Resposta inválida do servidor");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    callback.onError("Erro ao atualizar status: " + response.code());
                }
            }
        });

        return call;
    }

    private Order convertSupabaseResponseToOrder(OrderSupabaseResponse response) {
        Order order = new Order();

        try {
            // Definir ID usando reflection
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, response.id);

            // Definir createdAt usando reflection
            java.lang.reflect.Field createdAtField = Order.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);

            // Converter string ISO 8601 para Date
            if (response.createdAt != null) {
                try {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = isoFormat.parse(response.createdAt.replace("Z", "").substring(0, 19));
                    createdAtField.set(order, date);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao parsear data: " + response.createdAt, e);
                    createdAtField.set(order, new Date());
                }
            }

            // Definir code usando reflection
            java.lang.reflect.Field codeField = Order.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(order, response.code != null ? response.code : "ORD" + response.id.substring(0, Math.min(6, response.id.length())));

            // Definir total usando reflection
            java.lang.reflect.Field totalField = Order.class.getDeclaredField("total");
            totalField.setAccessible(true);
            totalField.set(order, response.totalAmount);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao definir campos do pedido", e);
        }

        // Campos públicos normais
        order.setStudentId(String.valueOf(response.idUsuario));
        order.setStatus(response.status);

        return order;
    }

    // Classes para requisições e respostas
    private static class OrderSupabaseRequest {
        @SerializedName("id_usuario")
        public String idUsuario;

        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        public OrderSupabaseRequest(Order order) {
            this.idUsuario = order.getStudentId();
            this.status = order.getStatus();
            this.totalAmount = order.getTotal();
        }
    }

    private static class OrderItemSupabaseRequest {
        @SerializedName("id_pedido")
        public String idPedido;
        @SerializedName("id_produto")
        public String idProduto;
        public int quantidade;
        @SerializedName("preco_produto")
        public double precoProduto;

        public OrderItemSupabaseRequest(String idPedido, OrderItem item) {
            this.idPedido = idPedido;
            this.idProduto = item.getProductId();
            this.quantidade = item.getQuantity();
            this.precoProduto = item.getPrice();
        }
    }

    private static class OrderSupabaseResponse {
        public String id;

        @SerializedName("id_usuario")
        public int idUsuario;

        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        @SerializedName("created_at")
        public String createdAt;

        public String code;
    }

    private static class StatusUpdateRequest {
        public String status;

        public StatusUpdateRequest(String status) {
            this.status = status;
        }
    }

    public interface OrderCallback {
        void onSuccess(Order order);
        void onError(String error);
    }

    public interface OrdersCallback {
        void onSuccess(List<Order> orders);
        void onError(String error);
    }

    public Call getOrderById(String orderId, String accessToken, OrderCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id=eq." + orderId)
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        if (ordersResponse != null && !ordersResponse.isEmpty()) {
                            Order order = convertSupabaseResponseToOrder(ordersResponse.get(0));
                            getOrderItems(orderId, accessToken, order, callback);
                        } else {
                            callback.onError("Pedido não encontrado");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar pedido", e);
                        callback.onError("Erro ao processar pedido");
                    }
                } else {
                    callback.onError("Erro ao buscar pedido: " + response.code());
                }
            }
        });

        return call;
    }

    // Buscar TODOS os pedidos (para admin)
    public Call getAllOrders(String accessToken, OrdersCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?order=created_at.desc")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        List<Order> orders = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                Order order = convertSupabaseResponseToOrder(orderResponse);
                                orders.add(order);
                            }
                        }

                        callback.onSuccess(orders);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar pedidos", e);
                        callback.onError("Erro ao processar pedidos");
                    }
                } else {
                    callback.onError("Erro ao buscar pedidos: " + response.code());
                }
            }
        });

        return call;
    }

    // Confirmar retirada do pedido
    public Call confirmOrderPickup(String orderId, String accessToken, OrderCallback callback) {
        return updateOrderStatus(orderId, "RETIRADO", accessToken, callback);
    }

    // Buscar pedidos por status
    public Call getOrdersByStatus(String status, String accessToken, OrdersCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?status=eq." + status + "&order=created_at.desc")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderSupabaseResponse>>(){}.getType();
                        List<OrderSupabaseResponse> ordersResponse = gson.fromJson(responseBody, listType);

                        List<Order> orders = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                orders.add(convertSupabaseResponseToOrder(orderResponse));
                            }
                        }

                        callback.onSuccess(orders);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar pedidos", e);
                        callback.onError("Erro ao processar pedidos");
                    }
                } else {
                    callback.onError("Erro ao buscar pedidos: " + response.code());
                }
            }
        });

        return call;
    }

    private void getOrderItems(String orderId, String accessToken, Order order, OrderCallback callback) {
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/itens_pedido?id_pedido=eq." + orderId)
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    callback.onError("Erro ao buscar itens do pedido");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<OrderItemSupabaseResponse>>(){}.getType();
                        List<OrderItemSupabaseResponse> itemsResponse = gson.fromJson(responseBody, listType);

                        if (itemsResponse != null) {
                            for (OrderItemSupabaseResponse itemResponse : itemsResponse) {
                                OrderItem item = new OrderItem(
                                        String.valueOf(itemResponse.idProduto),
                                        itemResponse.nomeProduto,
                                        itemResponse.quantidade,
                                        itemResponse.precoProduto
                                );
                                order.addItem(item);
                            }
                        }

                        callback.onSuccess(order);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar itens", e);
                        callback.onError("Erro ao processar itens do pedido");
                    }
                } else {
                    callback.onSuccess(order);
                }
            }
        });
    }

    public Call cancelOrder(String orderId, String accessToken, OrderCallback callback) {
        return updateOrderStatus(orderId, "CANCELADO", accessToken, callback);
    }

    private static class OrderItemSupabaseResponse {
        public int id;
        @SerializedName("id_pedido")
        public int idPedido;
        @SerializedName("id_produto")
        public int idProduto;
        @SerializedName("nome_produto")
        public String nomeProduto;
        public int quantidade;
        @SerializedName("preco_produto")
        public double precoProduto;
    }
}