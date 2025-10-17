package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Gerenciador de sessão para autenticação do usuário
 * Armazena e recupera informações de login de forma segura
 */
public class SessionManager {
    private static final String TAG = "SessionManager";
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_ACCESS_TOKEN = "accessToken";
    private static final String KEY_REFRESH_TOKEN = "refreshToken";
    private static final String KEY_TOKEN_EXPIRES_AT = "tokenExpiresAt";

    private static SessionManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Salva a sessão do usuário após login bem-sucedido
     * Versão customizada para tabela users
     */
    public void createLoginSession(String userId, String email, String role) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);

        // Define expiração para 30 dias (em milissegundos)
        long expiresAt = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000);
        editor.putLong(KEY_TOKEN_EXPIRES_AT, expiresAt);

        editor.apply();

        Log.d(TAG, "Sessão criada para: " + email + " (Role: " + role + ")");
    }

    /**
     * Salva a sessão completa com tokens do Supabase Auth
     * USE ESTE MÉTODO após login bem-sucedido
     */
    public void createLoginSessionWithTokens(SupabaseClient.UserData userData, String accessToken, String refreshToken, int expiresIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, String.valueOf(userData.id));
        editor.putString(KEY_USER_EMAIL, userData.email);
        editor.putString(KEY_USER_NAME, userData.nome);
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        // Define expiração baseada no expiresIn (geralmente 3600 segundos = 1 hora)
        long expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        editor.putLong(KEY_TOKEN_EXPIRES_AT, expiresAt);

        editor.apply();

        Log.d(TAG, "Sessão completa criada para: " + userData.email + " (Role: " + userData.role + ")");
    }

    /**
     * Salva apenas os tokens (útil para refresh)
     */
    public void saveTokens(String accessToken, String refreshToken, int expiresIn) {
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        long expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        editor.putLong(KEY_TOKEN_EXPIRES_AT, expiresAt);

        editor.apply();

        Log.d(TAG, "Tokens salvos/atualizados");
    }

    /**
     * Verifica se o usuário está logado
     */
    public boolean isLoggedIn() {
        boolean isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false);

        if (isLoggedIn) {
            // Verifica se o token não expirou
            long expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0);
            long currentTime = System.currentTimeMillis();

            if (currentTime >= expiresAt) {
                Log.d(TAG, "Token expirado, realizando logout");
                logout();
                return false;
            }
        }

        return isLoggedIn;
    }

    /**
     * Retorna o ID do usuário logado
     */
    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    /**
     * Retorna o email do usuário logado
     */
    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Retorna o nome do usuário logado
     */
    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    /**
     * Retorna o access token
     */
    public String getAccessToken() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);

        // Se não tiver token, usar a chave anônima do Supabase como fallback
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Access token não encontrado, usando chave anônima");
            return BuildConfig.SUPABASE_ANON_KEY;
        }

        return token;
    }

    /**
     * Retorna o refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Verifica se tem um access token válido
     */
    public boolean hasValidToken() {
        String token = prefs.getString(KEY_ACCESS_TOKEN, null);
        if (token == null || token.isEmpty()) {
            return false;
        }

        long expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0);
        long currentTime = System.currentTimeMillis();

        return currentTime < expiresAt;
    }

    /**
     * Verifica se o token está próximo de expirar (menos de 5 minutos)
     */
    public boolean shouldRefreshToken() {
        long expiresAt = prefs.getLong(KEY_TOKEN_EXPIRES_AT, 0);
        long currentTime = System.currentTimeMillis();
        long fiveMinutes = 5 * 60 * 1000; // 5 minutos em milissegundos

        return (expiresAt - currentTime) < fiveMinutes;
    }

    /**
     * Atualiza o access token após refresh
     */
    public void updateAccessToken(String newAccessToken, int expiresIn) {
        editor.putString(KEY_ACCESS_TOKEN, newAccessToken);

        long expiresAt = System.currentTimeMillis() + (expiresIn * 1000L);
        editor.putLong(KEY_TOKEN_EXPIRES_AT, expiresAt);

        editor.apply();

        Log.d(TAG, "Access token atualizado");
    }

    /**
     * Realiza logout e limpa todos os dados da sessão
     */
    public void logout() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Sessão encerrada");
    }

    /**
     * Retorna todos os dados da sessão (útil para debug)
     */
    public String getSessionInfo() {
        return "SessionInfo{" +
                "isLoggedIn=" + isLoggedIn() +
                ", userId='" + getUserId() + '\'' +
                ", userName='" + getUserName() + '\'' +
                ", email='" + getUserEmail() + '\'' +
                ", hasToken=" + (getAccessToken() != null) +
                ", tokenValid=" + hasValidToken() +
                '}';
    }
}