// com/example/myapplication/SupabaseClient.java
package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SupabaseClient {
    private static final String TAG = "SupabaseClient";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static SupabaseClient instance;
    final OkHttpClient client;
    private final Gson gson;
    private final String supabaseUrl;
    private final String supabaseKey;
    private final boolean isConfigured;

    private SupabaseClient(Context context) {
        // Configurar OkHttpClient com configurações SSL mais permissivas para desenvolvimento
        client = createDevelopmentClient();
        gson = new Gson();

        // Obtém as configurações do BuildConfig

        supabaseUrl = BuildConfig.SUPABASE_URL;
        supabaseKey = BuildConfig.SUPABASE_ANON_KEY;

        // Verifica se as configurações estão válidas
        isConfigured = supabaseUrl != null && !supabaseUrl.isEmpty() &&
                supabaseKey != null && !supabaseKey.isEmpty();

        if (isConfigured) {
            Log.d(TAG, "SupabaseClient configurado com sucesso");
            Log.d(TAG, "URL: " + supabaseUrl);
        } else {
            Log.e(TAG, "Erro na configuração do SupabaseClient");
        }
    }

    private OkHttpClient createDevelopmentClient() {
        try {
            // Criar um trust manager que aceita todos os certificados (APENAS PARA DESENVOLVIMENTO)
            final TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public X509Certificate[] getAcceptedIssuers() {
                            return new X509Certificate[]{};
                        }
                    }
            };

            // Instalar o all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());

            // Criar um ssl socket factory com nosso all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .sslSocketFactory(sslSocketFactory, (X509TrustManager)trustAllCerts[0])
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                            return true; // Aceitar todos os hostnames em desenvolvimento
                        }
                    });

            Log.d(TAG, "OkHttpClient configurado com SSL permissivo para desenvolvimento");
            return builder.build();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar SSL client, usando configuração padrão", e);
            // Fallback para configuração padrão
            return new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
        }
    }

    public static synchronized SupabaseClient getInstance(Context context) {
        if (instance == null) {
            instance = new SupabaseClient(context.getApplicationContext());
        }
        return instance;
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public Call signUp(String email, String password, SupabaseCallback<AuthResponse> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        SignUpRequest signUpRequest = new SignUpRequest(email, password);
        String json = gson.toJson(signUpRequest);
        Log.d(TAG, "Enviando requisição de signup: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/auth/v1/signup")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "URL da requisição: " + request.url());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro na requisição de signup", e);
                    String errorMessage = "Erro de conexão: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("Trust anchor")) {
                        errorMessage = "Erro de certificado SSL. Verifique sua conexão de internet.";
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta signup - Código: " + response.code());
                Log.d(TAG, "Resposta signup - Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);
                        if (authResponse != null && authResponse.user != null) {
                            callback.onSuccess(authResponse);
                        } else {
                            callback.onError("Resposta inválida do servidor");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar resposta de signup", e);
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    try {
                        ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
                        String errorMessage = "Erro no registro";

                        if (errorResponse != null) {
                            if (errorResponse.msg != null) {
                                errorMessage = errorResponse.msg;
                            } else if (errorResponse.error_description != null) {
                                errorMessage = errorResponse.error_description;
                            } else if (errorResponse.error != null) {
                                errorMessage = errorResponse.error;
                            }
                        }

                        // Traduzir mensagens comuns do Supabase
                        if (errorMessage.contains("already registered")) {
                            errorMessage = "Este email já está registrado";
                        } else if (errorMessage.contains("invalid email")) {
                            errorMessage = "Email inválido";
                        } else if (errorMessage.contains("password")) {
                            errorMessage = "Senha deve ter pelo menos 6 caracteres";
                        }

                        callback.onError(errorMessage);
                    } catch (Exception e) {
                        callback.onError("Erro no servidor (Código: " + response.code() + ")");
                    }
                }
            }
        });

        return call;
    }

    public Call signIn(String email, String password, SupabaseCallback<AuthResponse> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        SignInRequest signInRequest = new SignInRequest(email, password);
        String json = gson.toJson(signInRequest);
        Log.d(TAG, "Enviando requisição de signin: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/auth/v1/token?grant_type=password")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "URL da requisição: " + request.url());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro na requisição de signin", e);
                    String errorMessage = "Erro de conexão: " + e.getMessage();
                    if (e.getMessage() != null && e.getMessage().contains("Trust anchor")) {
                        errorMessage = "Erro de certificado SSL. Verifique sua conexão de internet.";
                    }
                    callback.onError(errorMessage);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta signin - Código: " + response.code());
                Log.d(TAG, "Resposta signin - Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);
                        if (authResponse != null && authResponse.user != null) {
                            callback.onSuccess(authResponse);
                        } else {
                            callback.onError("Resposta inválida do servidor");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar resposta de signin", e);
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    try {
                        ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
                        String errorMessage = "Email ou senha incorretos";

                        if (errorResponse != null && errorResponse.error_description != null) {
                            if (errorResponse.error_description.contains("Invalid login credentials")) {
                                errorMessage = "Email ou senha incorretos";
                            } else {
                                errorMessage = errorResponse.error_description;
                            }
                        }

                        callback.onError(errorMessage);
                    } catch (Exception e) {
                        callback.onError("Email ou senha incorretos");
                    }
                }
            }
        });

        return call;
    }

    // ===== MÉTODOS PARA PRODUTOS DO CARDÁPIO =====

    // Buscar todos os produtos do cardápio
    public Call getAllProducts(SupabaseCallback<List<Produto>> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "Buscando todos os produtos: " + request.url());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao buscar produtos", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta buscar produtos - Código: " + response.code());
                Log.d(TAG, "Resposta buscar produtos - Body: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);
                        callback.onSuccess(produtos);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar lista de produtos", e);
                        callback.onError("Erro ao processar dados dos produtos");
                    }
                } else {
                    callback.onError("Erro ao buscar produtos (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // Buscar produtos por categoria
    public Call getProductsByCategory(int categoria, SupabaseCallback<List<Produto>> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?categoria=eq." + categoria + "&select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "Buscando produtos por categoria: " + request.url());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao buscar produtos por categoria", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta buscar por categoria - Código: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);
                        callback.onSuccess(produtos);
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar produtos por categoria", e);
                        callback.onError("Erro ao processar dados dos produtos");
                    }
                } else {
                    callback.onError("Erro ao buscar produtos por categoria (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // Buscar produto por ID
    public Call getProductById(int id, SupabaseCallback<Produto> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?id=eq." + id + "&select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "Buscando produto por ID: " + request.url());

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao buscar produto por ID", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta buscar produto por ID - Código: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);

                        if (produtos != null && !produtos.isEmpty()) {
                            callback.onSuccess(produtos.get(0));
                        } else {
                            callback.onError("Produto não encontrado");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar produto", e);
                        callback.onError("Erro ao processar dados do produto");
                    }
                } else {
                    callback.onError("Produto não encontrado (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // Atualizar estoque de um produto (útil para pedidos)
    public Call updateProductStock(int id, int novoEstoque, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        String json = "{\"estoque\":" + novoEstoque + "}";
        Log.d(TAG, "Atualizando estoque do produto ID " + id + ": " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?id=eq." + id)
                .patch(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao atualizar estoque", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                Log.d(TAG, "Resposta atualizar estoque - Código: " + response.code());

                if (response.isSuccessful()) {
                    callback.onSuccess(true);
                } else {
                    callback.onError("Erro ao atualizar estoque (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // Classes de modelo para requisições
    private static class SignUpRequest {
        private final String email;
        private final String password;

        public SignUpRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    private static class SignInRequest {
        private final String email;
        private final String password;

        public SignInRequest(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    // Classes de modelo para respostas
    public static class AuthResponse {
        @SerializedName("access_token")
        public String accessToken;

        @SerializedName("refresh_token")
        public String refreshToken;

        @SerializedName("expires_in")
        public int expiresIn;

        @SerializedName("token_type")
        public String tokenType;

        public User user;
    }

    public static class User {
        public String id;
        public String email;

        @SerializedName("email_confirmed_at")
        public String emailConfirmedAt;

        @SerializedName("created_at")
        public String createdAt;

        @SerializedName("updated_at")
        public String updatedAt;
    }

    private static class ErrorResponse {
        public String msg;
        public String error;
        public String error_description;
    }

    // Interface de callback
    public interface SupabaseCallback<T> {
        void onSuccess(T response);
        void onError(String error);
    }
    // ADICIONE ESTES MÉTODOS AO SEU SupabaseClient.java
// Coloque-os após os métodos existentes, antes das classes de modelo

// ===== MÉTODOS PARA UPLOAD DE IMAGENS =====

    // Upload de imagem para o Storage do Supabase
    public Call uploadProductImage(byte[] imageBytes, String fileName, String bucketName, SupabaseCallback<ImageUploadResponse> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Gerar nome único para a imagem
        long timestamp = System.currentTimeMillis();
        String extension = getFileExtension(fileName);
        String uniqueFileName = "produto_" + timestamp + "." + extension;

        Log.d(TAG, "Fazendo upload da imagem: " + uniqueFileName + " para bucket: " + bucketName);

        RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/*"));

        Request request = new Request.Builder()
                .url(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uniqueFileName)
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "image/*")
                .addHeader("x-upsert", "true")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro no upload da imagem", e);
                    callback.onError("Erro ao fazer upload: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta upload - Código: " + response.code());
                Log.d(TAG, "Resposta upload - Body: " + responseBody);

                if (response.isSuccessful()) {
                    // Construir a URL pública da imagem
                    String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uniqueFileName;

                    ImageUploadResponse uploadResponse = new ImageUploadResponse();
                    uploadResponse.fileName = uniqueFileName;
                    uploadResponse.publicUrl = publicUrl;
                    uploadResponse.bucketName = bucketName;

                    callback.onSuccess(uploadResponse);
                } else {
                    try {
                        ErrorResponse errorResponse = gson.fromJson(responseBody, ErrorResponse.class);
                        String errorMessage = "Erro no upload da imagem";

                        if (errorResponse != null && errorResponse.error != null) {
                            errorMessage = errorResponse.error;
                        }

                        callback.onError(errorMessage + " (Código: " + response.code() + ")");
                    } catch (Exception e) {
                        callback.onError("Erro no upload da imagem (Código: " + response.code() + ")");
                    }
                }
            }
        });

        return call;
    }

    // Verificar se o bucket existe e criar se necessário
    public Call createBucketIfNotExists(String bucketName, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Primeiro, tentar listar buckets para ver se já existe
        Request request = new Request.Builder()
                .url(supabaseUrl + "/storage/v1/bucket")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao verificar buckets", e);
                    callback.onError("Erro ao verificar buckets: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Buckets existentes: " + responseBody);

                // Se o bucket já existe, retorna sucesso
                if (responseBody.contains("\"name\":\"" + bucketName + "\"")) {
                    Log.d(TAG, "Bucket " + bucketName + " já existe");
                    callback.onSuccess(true);
                    return;
                }

                // Se não existe, criar o bucket
                createBucket(bucketName, callback);
            }
        });

        return call;
    }

    private void createBucket(String bucketName, SupabaseCallback<Boolean> callback) {
        String bucketConfig = "{\"name\":\"" + bucketName + "\",\"public\":true}";

        RequestBody body = RequestBody.create(bucketConfig, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/storage/v1/bucket")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao criar bucket", e);
                    callback.onError("Erro ao criar bucket: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) {
                    return;
                }

                Log.d(TAG, "Resposta criar bucket - Código: " + response.code());

                if (response.isSuccessful()) {
                    Log.d(TAG, "Bucket " + bucketName + " criado com sucesso");
                    callback.onSuccess(true);
                } else {
                    callback.onError("Erro ao criar bucket (Código: " + response.code() + ")");
                }
            }
        });
    }

    // Método utilitário para obter extensão do arquivo
    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "jpg"; // extensão padrão
    }

// ADICIONE ESTA CLASSE NO FINAL DO ARQUIVO, ANTES DO FECHAMENTO DA CLASSE SupabaseClient

    // Classe para resposta do upload de imagem
    public static class ImageUploadResponse {
        public String fileName;
        public String publicUrl;
        public String bucketName;

        @Override
        public String toString() {
            return "ImageUploadResponse{" +
                    "fileName='" + fileName + '\'' +
                    ", publicUrl='" + publicUrl + '\'' +
                    ", bucketName='" + bucketName + '\'' +
                    '}';
        }
    }
}