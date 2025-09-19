// com/example/myapplication/SupabaseClient.java
package com.example.myapplication;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.security.cert.CertificateException;
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
    private final OkHttpClient client;
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
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Instalar o all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

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
}