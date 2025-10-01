// com/example/myapplication/SessionManager.java
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
     * Salva a sessão do usuário após login/registro bem-sucedido
     */
    public void createLoginSession(SupabaseClient.AuthResponse authResponse) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, authResponse.user.id);
        editor.putString(KEY_USER_EMAIL, authResponse.user.email);
        editor.putString(KEY_ACCESS_TOKEN, authResponse.accessToken);
        editor.putString(KEY_REFRESH_TOKEN, authResponse.refreshToken);

        // Calcula quando o token expira (timestamp atual + expiresIn em segundos)
        long expiresAt = System.currentTimeMillis() + (authResponse.expiresIn * 1000L);
        editor.putLong(KEY_TOKEN_EXPIRES_AT, expiresAt);

        editor.apply();

        Log.d(TAG, "Sessão criada para: " + authResponse.user.email);
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
     * Retorna o access token
     */
    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Retorna o refresh token
     */
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
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
                ", email='" + getUserEmail() + '\'' +
                ", hasToken=" + (getAccessToken() != null) +
                '}';
    }
}