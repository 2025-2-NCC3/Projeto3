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
import java.util.Locale;
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
    private final Context context;
    private String userAccessToken = null;

    private SupabaseClient(Context context) {
        this.context = context.getApplicationContext();
        client = createDevelopmentClient();
        gson = new Gson();

        supabaseUrl = SupabaseConfig.SUPABASE_URL;
        supabaseKey = SupabaseConfig.SUPABASE_ANON_KEY;

        isConfigured = supabaseUrl != null && !supabaseUrl.isEmpty() && !supabaseKey.isEmpty();

        if (isConfigured) {
            Log.d(TAG, "SupabaseClient configurado com sucesso");
            Log.d(TAG, "URL: " + supabaseUrl);
        } else {
            Log.e(TAG, "Erro na configuração do SupabaseClient");
        }
    }

    private String getAuthToken() {
        SessionManager sessionManager = SessionManager.getInstance(context);
        String token = sessionManager.getAccessToken();

        if (token != null && !token.isEmpty()) {
            Log.d(TAG, "✓ Usando token do usuário autenticado");
            return token;
        }

        Log.w(TAG, "⚠ Nenhum token disponível - usando ANON_KEY");
        return supabaseKey;
    }

    private OkHttpClient createDevelopmentClient() {
        try {
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

            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new SecureRandom());
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
                            return true;
                        }
                    });

            Log.d(TAG, "OkHttpClient configurado com SSL permissivo para desenvolvimento");
            return builder.build();

        } catch (Exception e) {
            Log.e(TAG, "Erro ao configurar SSL client, usando configuração padrão", e);
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

    // ===== MÉTODOS DE AUTENTICAÇÃO =====

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
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta signup - Código: " + response.code());

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

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro na requisição de signin", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta signin - Código: " + response.code());

                if (response.isSuccessful()) {
                    try {
                        AuthResponse authResponse = gson.fromJson(responseBody, AuthResponse.class);
                        if (authResponse != null && authResponse.user != null) {
                            SessionManager sessionManager = SessionManager.getInstance(context);
                            sessionManager.saveAccessToken(
                                    authResponse.accessToken,
                                    authResponse.expiresIn
                            );

                            Log.d(TAG, "✓ Token salvo no SessionManager");
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

    public void setUserAccessToken(String token) {
        this.userAccessToken = token;
        Log.d(TAG, "Token de acesso do usuário definido");
    }

    public void clearUserAccessToken() {
        this.userAccessToken = null;
        Log.d(TAG, "Token de acesso do usuário limpo");
    }

    public String getUserAccessToken() {
        return userAccessToken;
    }

    // ===== MÉTODOS PARA USUÁRIOS =====

    public Call getUserByAuthId(String authUserId, SupabaseCallback<UserData> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users?auth_user_id=eq." + authUserId + "&select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Log.d(TAG, "Buscando usuário por auth_user_id: " + authUserId);

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<UserData>>(){}.getType();
                        List<UserData> users = gson.fromJson(responseBody, listType);

                        if (users != null && !users.isEmpty()) {
                            callback.onSuccess(users.get(0));
                        } else {
                            callback.onError("Perfil não encontrado");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados");
                    }
                } else {
                    callback.onError("Erro ao buscar perfil");
                }
            }
        });

        return call;
    }

    public Call createUserWithAuthId(String json, SupabaseCallback<UserData> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = client.newCall(request);
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

                if (response.isSuccessful() || response.code() == 201) {
                    try {
                        Type listType = new TypeToken<List<UserData>>(){}.getType();
                        List<UserData> users = gson.fromJson(responseBody, listType);

                        if (users != null && !users.isEmpty()) {
                            callback.onSuccess(users.get(0));
                        } else {
                            callback.onError("Erro ao criar perfil");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    callback.onError("Erro ao criar perfil (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call getUserById(int userId, SupabaseCallback<UserData> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users?id=eq." + userId + "&select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<UserData>>(){}.getType();
                        List<UserData> users = gson.fromJson(responseBody, listType);

                        if (users != null && !users.isEmpty()) {
                            callback.onSuccess(users.get(0));
                        } else {
                            callback.onError("Usuário não encontrado");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados do usuário");
                    }
                } else {
                    callback.onError("Erro ao buscar usuário (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call getUserByEmail(String email, SupabaseCallback<UserData> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users?email=eq." + email + "&select=*")
                .get()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<UserData>>(){}.getType();
                        List<UserData> users = gson.fromJson(responseBody, listType);

                        if (users != null && !users.isEmpty()) {
                            callback.onSuccess(users.get(0));
                        } else {
                            callback.onError("Usuário não encontrado");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados do usuário");
                    }
                } else {
                    callback.onError("Erro ao buscar usuário (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call createUser(String nome, String email, String senha, SupabaseCallback<UserData> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        String json = String.format(
                "{\"nome\":\"%s\",\"email\":\"%s\",\"senha\":\"%s\",\"role\":\"user\"}",
                nome, email, senha
        );

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + supabaseKey)
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = client.newCall(request);
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

                if (response.isSuccessful() || response.code() == 201) {
                    try {
                        Type listType = new TypeToken<List<UserData>>(){}.getType();
                        List<UserData> users = gson.fromJson(responseBody, listType);

                        if (users != null && !users.isEmpty()) {
                            callback.onSuccess(users.get(0));
                        } else {
                            UserData userData = new UserData();
                            userData.nome = nome;
                            userData.email = email;
                            userData.role = "user";
                            callback.onSuccess(userData);
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar resposta");
                    }
                } else {
                    callback.onError("Erro ao criar usuário (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call updateUserRole(int userId, String newRole, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        String json = "{\"role\":\"" + newRole + "\"}";

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/users?id=eq." + userId)
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
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                if (response.isSuccessful()) {
                    callback.onSuccess(true);
                } else {
                    callback.onError("Erro ao atualizar role (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // ===== MÉTODOS PARA PRODUTOS =====

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

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);
                        callback.onSuccess(produtos);
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados dos produtos");
                    }
                } else {
                    callback.onError("Erro ao buscar produtos (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

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

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);
                        callback.onSuccess(produtos);
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados dos produtos");
                    }
                } else {
                    callback.onError("Erro ao buscar produtos por categoria (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

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

        Call call = client.newCall(request);
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
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);

                        if (produtos != null && !produtos.isEmpty()) {
                            callback.onSuccess(produtos.get(0));
                        } else {
                            callback.onError("Produto não encontrado");
                        }
                    } catch (Exception e) {
                        callback.onError("Erro ao processar dados do produto");
                    }
                } else {
                    callback.onError("Produto não encontrado (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call createProduct(Produto produto, SupabaseCallback<Produto> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // CORREÇÃO: Usar "caminhoimagem" (como está no banco)
        String json = String.format(Locale.US,
                "{\"nome\":\"%s\",\"preco\":%.2f,\"descricao\":\"%s\",\"caminho_imagem\":\"%s\",\"estoque\":%d,\"categoria\":%d}",
                produto.getNome().replace("\"", "\\\""),
                produto.getPreco(),
                produto.getDescricao() != null ? produto.getDescricao().replace("\"", "\\\"") : "",
                produto.getCaminhoImagem() != null ? produto.getCaminhoImagem().replace("\"", "\\\"") : "",
                produto.getEstoque(),
                produto.getCategoria()
        );

        Log.d(TAG, "Criando produto: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos")
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao criar produto", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta criar produto - Código: " + response.code());
                Log.d(TAG, "Resposta criar produto - Body: " + responseBody);

                if (response.isSuccessful() || response.code() == 201) {
                    try {
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);

                        if (produtos != null && !produtos.isEmpty()) {
                            callback.onSuccess(produtos.get(0));
                        } else {
                            callback.onError("Erro ao processar resposta");
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Erro ao processar resposta", e);
                        callback.onError("Erro ao processar dados do produto");
                    }
                } else {
                    callback.onError("Erro ao criar produto (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call updateProduct(Produto produto, SupabaseCallback<Produto> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        // Construir JSON com escape correto
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("{");

        // Campos obrigatórios
        jsonBuilder.append("\"nome\":\"").append(produto.getNome().replace("\"", "\\\"")).append("\",");
        jsonBuilder.append("\"preco\":").append(String.format(Locale.US, "%.2f", produto.getPreco())).append(",");
        jsonBuilder.append("\"estoque\":").append(produto.getEstoque()).append(",");
        jsonBuilder.append("\"categoria\":").append(produto.getCategoria());

        // Descrição (opcional)
        if (produto.getDescricao() != null && !produto.getDescricao().trim().isEmpty()) {
            jsonBuilder.append(",\"descricao\":\"").append(produto.getDescricao().replace("\"", "\\\"")).append("\"");
        }

        // CORREÇÃO: Usar "caminhoimagem" (sem underscore) como está no banco
        String imagemPath = produto.getCaminhoImagem();
        if (imagemPath != null && !imagemPath.trim().isEmpty() && !imagemPath.equals("EMPTY")) {
            jsonBuilder.append(",\"caminho_imagem\":\"").append(imagemPath.replace("\"", "\\\"")).append("\"");
        }

        jsonBuilder.append("}");
        String json = jsonBuilder.toString();

        Log.d(TAG, "=== ATUALIZANDO PRODUTO ===");
        Log.d(TAG, "ID: " + produto.getId());
        Log.d(TAG, "JSON enviado: " + json);

        RequestBody body = RequestBody.create(json, JSON);
        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?id=eq." + produto.getId())
                .patch(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "❌ Erro de conexão", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Código HTTP: " + response.code());
                Log.d(TAG, "Resposta completa: " + responseBody);

                if (response.isSuccessful()) {
                    try {
                        Type listType = new TypeToken<List<Produto>>(){}.getType();
                        List<Produto> produtos = gson.fromJson(responseBody, listType);

                        if (produtos != null && !produtos.isEmpty()) {
                            Log.d(TAG, "✓ Produto atualizado com sucesso!");
                            callback.onSuccess(produtos.get(0));
                        } else {
                            Log.d(TAG, "✓ Atualização concluída (204 No Content)");
                            callback.onSuccess(produto);
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Resposta vazia mas atualização bem-sucedida", e);
                        callback.onSuccess(produto);
                    }
                } else {
                    Log.e(TAG, "❌ Erro HTTP " + response.code());
                    Log.e(TAG, "Resposta de erro: " + responseBody);

                    String errorMsg = "Erro ao atualizar produto (Código: " + response.code() + ")";
                    if (responseBody.contains("does not exist")) {
                        errorMsg = "Erro: Verifique os nomes das colunas no banco";
                    } else if (responseBody.contains("violates")) {
                        errorMsg = "Erro de validação nos dados";
                    }

                    callback.onError(errorMsg);
                }
            }
        });

        return call;
    }

    public Call deleteProduct(int produtoId, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        Log.d(TAG, "Deletando produto ID: " + produtoId);

        Request request = new Request.Builder()
                .url(supabaseUrl + "/rest/v1/produtos?id=eq." + produtoId)
                .delete()
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + getAuthToken())
                .addHeader("Content-Type", "application/json")
                .build();

        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (!call.isCanceled()) {
                    Log.e(TAG, "Erro ao deletar produto", e);
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                Log.d(TAG, "Resposta deletar produto - Código: " + response.code());

                if (response.isSuccessful() || response.code() == 204) {
                    Log.d(TAG, "Produto deletado com sucesso");
                    callback.onSuccess(true);
                } else {
                    String responseBody = response.body() != null ? response.body().string() : "";
                    Log.e(TAG, "Erro ao deletar: " + responseBody);
                    callback.onError("Erro ao deletar produto (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    public Call updateProductStock(int id, int novoEstoque, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        String json = "{\"estoque\":" + novoEstoque + "}";

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
                    callback.onError("Erro de conexão: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                if (response.isSuccessful()) {
                    callback.onSuccess(true);
                } else {
                    callback.onError("Erro ao atualizar estoque (Código: " + response.code() + ")");
                }
            }
        });

        return call;
    }

    // ===== MÉTODOS PARA UPLOAD DE IMAGENS =====

    public Call uploadProductImage(byte[] imageBytes, String fileName, String bucketName, SupabaseCallback<ImageUploadResponse> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

        SessionManager sessionManager = SessionManager.getInstance(context);
        String userToken = sessionManager.getAccessToken();

        if (userToken == null || userToken.isEmpty()) {
            Log.e(TAG, "❌ ERRO: Nenhum token de acesso encontrado!");
            callback.onError("Você precisa estar logado para fazer upload de imagens");
            return null;
        }

        long timestamp = System.currentTimeMillis();
        String extension = getFileExtension(fileName);
        String uniqueFileName = "produto_" + timestamp + "." + extension;

        Log.d(TAG, "=== UPLOAD DE IMAGEM ===");
        Log.d(TAG, "Arquivo: " + uniqueFileName);
        Log.d(TAG, "Bucket: " + bucketName);

        RequestBody body = RequestBody.create(imageBytes, MediaType.parse("image/*"));

        Request request = new Request.Builder()
                .url(supabaseUrl + "/storage/v1/object/" + bucketName + "/" + uniqueFileName)
                .post(body)
                .addHeader("apikey", supabaseKey)
                .addHeader("Authorization", "Bearer " + userToken)
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
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";
                Log.d(TAG, "Resposta upload - Código: " + response.code());
                Log.d(TAG, "Resposta upload - Body: " + responseBody);

                if (response.isSuccessful()) {
                    String publicUrl = supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + uniqueFileName;

                    ImageUploadResponse uploadResponse = new ImageUploadResponse();
                    uploadResponse.fileName = uniqueFileName;
                    uploadResponse.publicUrl = publicUrl;
                    uploadResponse.bucketName = bucketName;

                    Log.d(TAG, "✓ Upload realizado com sucesso!");
                    Log.d(TAG, "URL pública: " + publicUrl);

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

    public void uploadImageToBucket(android.net.Uri imageUri, String bucketName, SupabaseCallback<String> callback) {
        try {
            android.content.ContentResolver resolver = context.getContentResolver();
            java.io.InputStream inputStream = resolver.openInputStream(imageUri);

            if (inputStream == null) {
                callback.onError("Não foi possível ler a imagem");
                return;
            }

            byte[] bytes = getBytesFromInputStream(inputStream);
            inputStream.close();

            String fileName = getFileNameFromUri(imageUri);

            Log.d(TAG, "Upload para bucket: " + bucketName);
            Log.d(TAG, "Nome do arquivo: " + fileName);

            uploadProductImage(bytes, fileName, bucketName, new SupabaseCallback<ImageUploadResponse>() {
                @Override
                public void onSuccess(ImageUploadResponse response) {
                    Log.d(TAG, "✓ Upload bem-sucedido! URL: " + response.publicUrl);
                    callback.onSuccess(response.publicUrl);
                }

                @Override
                public void onError(String error) {
                    Log.e(TAG, "❌ Erro no upload: " + error);
                    callback.onError(error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            callback.onError("Erro ao processar imagem: " + e.getMessage());
        }
    }

    public void uploadImage(android.net.Uri imageUri, SupabaseCallback<String> callback) {
        try {
            android.content.ContentResolver resolver = context.getContentResolver();
            java.io.InputStream inputStream = resolver.openInputStream(imageUri);

            if (inputStream == null) {
                callback.onError("Não foi possível ler a imagem");
                return;
            }

            byte[] bytes = getBytesFromInputStream(inputStream);
            inputStream.close();

            String fileName = getFileNameFromUri(imageUri);

            uploadProductImage(bytes, fileName, "produtos", new SupabaseCallback<ImageUploadResponse>() {
                @Override
                public void onSuccess(ImageUploadResponse response) {
                    callback.onSuccess(response.publicUrl);
                }

                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Erro ao processar imagem", e);
            callback.onError("Erro ao processar imagem: " + e.getMessage());
        }
    }

    public Call createBucketIfNotExists(String bucketName, SupabaseCallback<Boolean> callback) {
        if (!isConfigured) {
            callback.onError("SupabaseClient não está configurado");
            return null;
        }

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
                    callback.onError("Erro ao verificar buckets: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                String responseBody = response.body() != null ? response.body().string() : "";

                if (responseBody.contains("\"name\":\"" + bucketName + "\"")) {
                    Log.d(TAG, "Bucket " + bucketName + " já existe");
                    callback.onSuccess(true);
                    return;
                }

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
                    callback.onError("Erro ao criar bucket: " + e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (call.isCanceled()) return;

                if (response.isSuccessful()) {
                    Log.d(TAG, "Bucket " + bucketName + " criado com sucesso");
                    callback.onSuccess(true);
                } else {
                    callback.onError("Erro ao criar bucket (Código: " + response.code() + ")");
                }
            }
        });
    }

    // ===== MÉTODOS AUXILIARES =====

    private String getFileExtension(String fileName) {
        if (fileName != null && fileName.contains(".")) {
            return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        }
        return "jpg";
    }

    private byte[] getBytesFromInputStream(java.io.InputStream inputStream) throws IOException {
        java.io.ByteArrayOutputStream byteBuffer = new java.io.ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private String getFileNameFromUri(android.net.Uri uri) {
        String result = "image.jpg";

        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }

        return result != null ? result : "image.jpg";
    }

    // ===== CLASSES DE MODELO =====

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

    public static class UserData {
        @SerializedName("id")
        public Integer id;

        @SerializedName("nome")
        public String nome;

        @SerializedName("email")
        public String email;

        @SerializedName("senha")
        public String senha;

        @SerializedName("role")
        public String role;
    }

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

    public interface SupabaseCallback<T> {
        void onSuccess(T response);
        void onError(String error);
    }
}