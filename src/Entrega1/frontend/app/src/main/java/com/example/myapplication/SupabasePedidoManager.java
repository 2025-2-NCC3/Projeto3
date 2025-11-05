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
    private static final String TAG = "SupabasePedidoManager";
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

    // ✅ ATUALIZADO: createOrder → createPedido
    public Call createPedido(PedidoRequest pedidoRequest, String accessToken, PedidoCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Criar Pedido a partir do PedidoRequest
        Pedido pedido = new Pedido();
        pedido.setStudentId(pedidoRequest.getStudentId());
        pedido.setStudentName(pedidoRequest.getStudentName());

        // Adicionar itens ao pedido
        for (PedidoItemRequest itemRequest : pedidoRequest.getItems()) {
            Log.d(TAG, "========== ITEM REQUEST ==========");
            Log.d(TAG, "Produto ID: " + itemRequest.getProductId());
            Log.d(TAG, "Nome: " + itemRequest.getProductName());
            Log.d(TAG, "Preço: " + itemRequest.getPrice());
            Log.d(TAG, "Quantidade: " + itemRequest.getQuantity());

            PedidoItem item = new PedidoItem(
                    itemRequest.getProductId(),
                    itemRequest.getProductName(),
                    itemRequest.getQuantity(),
                    itemRequest.getPrice()
            );

            Log.d(TAG, "========== PEDIDO ITEM ==========");
            Log.d(TAG, "Preço item (getPrice): " + item.getPrice());
            Log.d(TAG, "Quantidade: " + item.getQuantity());
            Log.d(TAG, "Subtotal: " + item.getSubtotal());
            pedido.addItem(item);
        }

        // Converter para formato do Supabase
        PedidoSupabaseRequest supabaseRequest = new PedidoSupabaseRequest(pedido);
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        if (pedidosResponse != null && !pedidosResponse.isEmpty()) {
                            PedidoSupabaseResponse pedidoResponse = pedidosResponse.get(0);
                            Pedido createdPedido = convertSupabaseResponseToPedido(pedidoResponse);

                            Log.d(TAG, "Copiando " + pedido.getItems().size() + " itens para o pedido criado");
                            for (PedidoItem item : pedido.getItems()) {
                                createdPedido.addItem(item);
                            }

                            createPedidoItems(String.valueOf(createdPedido.getId()), createdPedido.getItems(), accessToken, callback, createdPedido);
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

    private void createPedidoItems(String pedidoId, List<PedidoItem> items, String accessToken,
                                   PedidoCallback callback, Pedido createdPedido) {

        List<PedidoItemSupabaseRequest> itemsRequest = new ArrayList<>();
        for (PedidoItem item : items) {
            Log.d(TAG, "========== ANTES DO REQUEST ==========");
            Log.d(TAG, "Item: " + item.getProductName());
            Log.d(TAG, "Preço (item.getPrice()): " + item.getPrice());

            PedidoItemSupabaseRequest itemRequest = new PedidoItemSupabaseRequest(pedidoId, item);

            Log.d(TAG, "========== DEPOIS DO REQUEST ==========");
            Log.d(TAG, "precoProduto: " + itemRequest.precoProduto);
            Log.d(TAG, "quantidade: " + itemRequest.quantidade);

            itemsRequest.add(itemRequest);
        }

        String json = gson.toJson(itemsRequest);
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
                Log.d(TAG, "========== RESPOSTA CRIAR ITENS ==========");
                Log.d(TAG, "Código: " + response.code());
                Log.d(TAG, "Body: " + responseBody);

                if (response.isSuccessful()) {
                    createdPedido.setStatus("CONFIRMED");
                    callback.onSuccess(createdPedido);
                } else {
                    callback.onError("Erro ao salvar itens do pedido: " + response.code());
                }
            }
        });
    }

    // ✅ ATUALIZADO: getStudentOrders → getStudentPedidos
    public Call getStudentPedidos(String studentId, String accessToken, PedidosCallback callback) {
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        List<Pedido> pedidos = new ArrayList<>();
                        if (pedidosResponse != null) {
                            for (PedidoSupabaseResponse pedidoResponse : pedidosResponse) {
                                pedidos.add(convertSupabaseResponseToPedido(pedidoResponse));
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

    // ✅ ATUALIZADO: updateOrderStatus → updatePedidoStatus
    public Call updatePedidoStatus(String pedidoId, String newStatus, String accessToken, PedidoCallback callback) {
        StatusUpdateRequest statusRequest = new StatusUpdateRequest(newStatus);
        String json = gson.toJson(statusRequest);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id=eq." + pedidoId)
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        if (pedidosResponse != null && !pedidosResponse.isEmpty()) {
                            Pedido updatedPedido = convertSupabaseResponseToPedido(pedidosResponse.get(0));
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

    private Pedido convertSupabaseResponseToPedido(PedidoSupabaseResponse response) {
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
            codeField.set(pedido, response.code != null ? response.code : "PED" + response.id.substring(0, Math.min(6, response.id.length())));

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
    private static class PedidoSupabaseRequest {
        @SerializedName("id_usuario")
        public String idUsuario;

        public String status;

        @SerializedName("total_amount")
        public double totalAmount;

        public PedidoSupabaseRequest(Pedido pedido) {
            this.idUsuario = pedido.getStudentId();
            this.status = pedido.getStatus();
            this.totalAmount = pedido.getTotal();
        }
    }

    private static class PedidoItemSupabaseRequest {
        @SerializedName("id_pedido")
        public String idPedido;
        @SerializedName("id_produto")
        public String idProduto;
        public int quantidade;
        @SerializedName("preco_produto")
        public double precoProduto;

        public PedidoItemSupabaseRequest(String idPedido, PedidoItem item) {
            this.idPedido = idPedido;
            this.idProduto = item.getProductId();
            this.quantidade = item.getQuantity();
            this.precoProduto = item.getPrice();
        }
    }

    private static class PedidoSupabaseResponse {
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

    // ✅ ATUALIZADO: Callbacks renomeados
    public interface PedidoCallback {
        void onSuccess(Pedido pedido);
        void onError(String error);
    }

    public interface PedidosCallback {
        void onSuccess(List<Pedido> pedidos);
        void onError(String error);
    }

    public Call getPedidoById(String pedidoId, String accessToken, PedidoCallback callback) {
        if (!supabaseClient.isConfigured()) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/pedidos?id=eq." + pedidoId)
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        if (pedidosResponse != null && !pedidosResponse.isEmpty()) {
                            Pedido pedido = convertSupabaseResponseToPedido(pedidosResponse.get(0));
                            getPedidoItems(pedidoId, accessToken, pedido, callback);
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

    // ✅ ATUALIZADO: getAllOrders → getAllPedidos
    public Call getAllPedidos(String accessToken, PedidosCallback callback) {
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        List<Pedido> pedidos = new ArrayList<>();
                        if (pedidosResponse != null) {
                            for (PedidoSupabaseResponse pedidoResponse : pedidosResponse) {
                                Pedido pedido = convertSupabaseResponseToPedido(pedidoResponse);
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

    // ✅ ATUALIZADO: confirmOrderPickup → confirmPedidoPickup
    public Call confirmPedidoPickup(String pedidoId, String accessToken, PedidoCallback callback) {
        return updatePedidoStatus(pedidoId, "RETIRADO", accessToken, callback);
    }

    // ✅ ATUALIZADO: getOrdersByStatus → getPedidosByStatus
    public Call getPedidosByStatus(String status, String accessToken, PedidosCallback callback) {
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
                        Type listType = new TypeToken<List<PedidoSupabaseResponse>>(){}.getType();
                        List<PedidoSupabaseResponse> pedidosResponse = gson.fromJson(responseBody, listType);

                        List<Pedido> pedidos = new ArrayList<>();
                        if (pedidosResponse != null) {
                            for (PedidoSupabaseResponse pedidoResponse : pedidosResponse) {
                                pedidos.add(convertSupabaseResponseToPedido(pedidoResponse));
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

    private void getPedidoItems(String pedidoId, String accessToken, Pedido pedido, PedidoCallback callback) {
        // ✅ ATUALIZADO: Query com JOIN para pegar o nome do produto
        Request request = new Request.Builder()
                .url(BuildConfig.SUPABASE_URL + "/rest/v1/itens_pedido?id_pedido=eq." + pedidoId + "&select=*,produtos(nome)")
                .get()
                .addHeader("apikey", BuildConfig.SUPABASE_ANON_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        Call call = supabaseClient.client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao buscar itens do pedido", e);
                    callback.onError("Erro ao buscar itens do pedido");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta itens do pedido: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<PedidoItemSupabaseResponse>>(){}.getType();
                        List<PedidoItemSupabaseResponse> itemsResponse = gson.fromJson(responseBody, listType);

                        if (itemsResponse != null) {
                            for (PedidoItemSupabaseResponse itemResponse : itemsResponse) {
                                // Pega o nome do produto do JOIN
                                String nomeProduto = itemResponse.produtos != null && itemResponse.produtos.nome != null
                                        ? itemResponse.produtos.nome
                                        : "Produto sem nome";

                                PedidoItem item = new PedidoItem(
                                        String.valueOf(itemResponse.idProduto),
                                        nomeProduto,
                                        itemResponse.quantidade,
                                        itemResponse.precoProduto
                                );
                                pedido.addItem(item);

                                Log.d(TAG, "Item adicionado: " + nomeProduto + " - Qtd: " + itemResponse.quantidade);
                            }
                        }

                        callback.onSuccess(pedido);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar itens", e);
                        callback.onError("Erro ao processar itens do pedido");
                    }
                } else {
                    Log.e(TAG, "Erro na resposta: " + response.code());
                    callback.onSuccess(pedido); // Retorna pedido sem itens
                }
            }
        });
    }

    public Call cancelPedido(String pedidoId, String accessToken, PedidoCallback callback) {
        return updatePedidoStatus(pedidoId, "CANCELADO", accessToken, callback);
    }

    private static class PedidoItemSupabaseResponse {
        public int id;

        @SerializedName("id_pedido")
        public int idPedido;

        @SerializedName("id_produto")
        public int idProduto;

        public int quantidade;

        @SerializedName("preco_produto")
        public double precoProduto;

        public ProdutoNome produtos;

        // Classe interna para o nome do produto
        public static class ProdutoNome {
            public String nome;
        }
    }
}