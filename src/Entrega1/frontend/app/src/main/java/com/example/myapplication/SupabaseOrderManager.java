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

        // Adicionar itens ao pedido
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Produto produto = OrderManager.getProduct(itemRequest.getProductId());
            if (produto != null) {
                OrderItem item = new OrderItem(
                        produto.getId(),
                        produto.getNome(),
                        itemRequest.getQuantity(),
                        produto.getPreco()
                );
                order.addItem(item);
            }
        }

        // Validar estoque antes de enviar
        String stockError = OrderManager.validateStock(order);
        if (stockError != null) {
            callback.onError(stockError);
            return null;
        }

        // Converter para formato do Supabase - SEM produtos
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

                            // Criar itens do pedido
                            createOrderItems(String.valueOf(createdOrder.getId()), order.getItems(), accessToken, callback, createdOrder);
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
            itemsRequest.add(new OrderItemSupabaseRequest(orderId, item));
        }

        String json = gson.toJson(itemsRequest);
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
                    callback.onError("Erro ao salvar itens do pedido");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                if (response.isSuccessful()) {
                    OrderManager.updateStock(createdOrder);
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
            java.lang.reflect.Field idField = Order.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(order, response.id);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao definir ID do pedido", e);
        }

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