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

public class SupabasePedidoManager {
    private static final String TAG = "SupabaseOrderManager";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static SupabasePedidoManager instance;
    private final SupabaseClient supabaseClient;
    private final Gson gson;

    private SupabasePedidoManager(Context context) {
        this.supabaseClient = SupabaseClient.getInstance(context);
        this.gson = new Gson();
    }

    public static synchronized SupabasePedidoManager getInstance(Context context) {
        if (instance == null) {
            instance = new SupabasePedidoManager(context);
        }
        return instance;
    }

    // Criar pedido no Supabase
    public Call createOrder(PedidoRequest pedidoRequest, String accessToken, OrderCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Criar Order a partir do OrderRequest
        Pedido pedido = new Pedido();
        pedido.setStudentId(pedidoRequest.getStudentId());
        pedido.setStudentName(pedidoRequest.getStudentName());

        // Adicionar itens ao pedido (usando dados do OrderRequest, não do OrderManager)
        for (PedidoItemRequest itemRequest : pedidoRequest.getItems()) {
            // ⭐ LOG 1: Verificar dados do ItemRequest
            Log.d(TAG, "========== ITEM REQUEST ==========");
            Log.d(TAG, "Produto ID: " + itemRequest.getProductId());
            Log.d(TAG, "Nome: " + itemRequest.getProductName());
            Log.d(TAG, "Preço: " + itemRequest.getPrice());
            Log.d(TAG, "Quantidade: " + itemRequest.getQuantity());

            // Criar OrderItem diretamente do ItemRequest (sem buscar do OrderManager)
            PedidoItem item = new PedidoItem(
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
            pedido.addItem(item);
        }

        // Validar estoque antes de enviar (OPCIONAL - se quiser manter validação)
        // String stockError = OrderManager.validateStock(order);
        // if (stockError != null) {
        //     callback.onError(stockError);
        //     return null;
        // }

        // Converter para formato do Supabase - SEM nome_usuario
        OrderSupabaseRequest supabaseRequest = new OrderSupabaseRequest(pedido);
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
                            Pedido createdPedido = convertSupabaseResponseToOrder(orderResponse);

                            // ⭐ CORREÇÃO CRÍTICA: Copiar itens do order original para o createdOrder
                            Log.d(TAG, "Copiando " + pedido.getItems().size() + " itens para o pedido criado");
                            for (PedidoItem item : pedido.getItems()) {
                                createdPedido.addItem(item);
                            }

                            // Criar itens do pedido (agora createdOrder tem os itens)
                            createOrderItems(String.valueOf(createdPedido.getId()), createdPedido.getItems(), accessToken, callback, createdPedido);
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

    private void createOrderItems(String orderId, List<PedidoItem> items, String accessToken,
                                  OrderCallback callback, Pedido createdPedido) {

        List<OrderItemSupabaseRequest> itemsRequest = new ArrayList<>();
        for (PedidoItem item : items) {
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
                    createdPedido.setStatus(PedidoManager.STATUS_CONFIRMED);
                    callback.onSuccess(createdPedido);
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

                        List<Pedido> pedidos = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                pedidos.add(convertSupabaseResponseToOrder(orderResponse));
                            }
                        }

                        callback.onSuccess(pedidos);
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
                            Pedido updatedPedido = convertSupabaseResponseToOrder(ordersResponse.get(0));
                            callback.onSuccess(updatedPedido);
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

    private Pedido convertSupabaseResponseToOrder(OrderSupabaseResponse response) {
        Pedido pedido = new Pedido();

        try {
            // Definir ID usando reflection
            java.lang.reflect.Field idField = Pedido.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(pedido, response.id);

            // Definir createdAt usando reflection
            java.lang.reflect.Field createdAtField = Pedido.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);

            // Converter string ISO 8601 para Date
            if (response.createdAt != null) {
                try {
                    SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                    Date date = isoFormat.parse(response.createdAt.replace("Z", "").substring(0, 19));
                    createdAtField.set(pedido, date);
                } catch (Exception e) {
                    Log.e(TAG, "Erro ao parsear data: " + response.createdAt, e);
                    createdAtField.set(pedido, new Date());
                }
            }

            // Definir code usando reflection
            java.lang.reflect.Field codeField = Pedido.class.getDeclaredField("code");
            codeField.setAccessible(true);
            codeField.set(pedido, response.code != null ? response.code : "ORD" + response.id.substring(0, Math.min(6, response.id.length())));

            // Definir total usando reflection
            java.lang.reflect.Field totalField = Pedido.class.getDeclaredField("total");
            totalField.setAccessible(true);
            totalField.set(pedido, response.totalAmount);

        } catch (Exception e) {
            Log.e(TAG, "Erro ao definir campos do pedido", e);
        }

        // Campos públicos normais
        pedido.setStudentId(String.valueOf(response.idUsuario));
        pedido.setStatus(response.status);

        return pedido;
    }

    // Classes para requisições e respostas
    private static class OrderSupabaseRequest {
        @SerializedName("id_usuario")
        public String idUsuario;

        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        public OrderSupabaseRequest(Pedido pedido) {
            this.idUsuario = pedido.getStudentId();
            this.status = pedido.getStatus();
            this.totalAmount = pedido.getTotal();
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

        public OrderItemSupabaseRequest(String idPedido, PedidoItem item) {
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
        void onSuccess(Pedido pedido);
        void onError(String error);
    }

    public interface OrdersCallback {
        void onSuccess(List<Pedido> pedidos);
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
                            Pedido pedido = convertSupabaseResponseToOrder(ordersResponse.get(0));
                            getOrderItems(orderId, accessToken, pedido, callback);
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

                        List<Pedido> pedidos = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                Pedido pedido = convertSupabaseResponseToOrder(orderResponse);
                                pedidos.add(pedido);
                            }
                        }

                        callback.onSuccess(pedidos);
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

                        List<Pedido> pedidos = new ArrayList<>();
                        if (ordersResponse != null) {
                            for (OrderSupabaseResponse orderResponse : ordersResponse) {
                                pedidos.add(convertSupabaseResponseToOrder(orderResponse));
                            }
                        }

                        callback.onSuccess(pedidos);
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

    private void getOrderItems(String orderId, String accessToken, Pedido pedido, OrderCallback callback) {
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
                                PedidoItem item = new PedidoItem(
                                        String.valueOf(itemResponse.idProduto),
                                        itemResponse.nomeProduto,
                                        itemResponse.quantidade,
                                        itemResponse.precoProduto
                                );
                                pedido.addItem(item);
                            }
                        }

                        callback.onSuccess(pedido);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar itens", e);
                        callback.onError("Erro ao processar itens do pedido");
                    }
                } else {
                    callback.onSuccess(pedido);
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