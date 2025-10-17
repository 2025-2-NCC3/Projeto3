package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Gerenciador de permissões administrativas
 */
public class AdminManager {
    private static final String TAG = "AdminManager";
    private static final String PREF_NAME = "AdminPrefs";
    private static final String KEY_IS_ADMIN = "isAdmin";
    private static final String KEY_USER_ROLE = "userRole";

    // Tipos de usuário
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    private static AdminManager instance;
    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;
    private final Context context;

    private AdminManager(Context context) {
        this.context = context.getApplicationContext();
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public static synchronized AdminManager getInstance(Context context) {
        if (instance == null) {
            instance = new AdminManager(context.getApplicationContext());
        }
        return instance;
    }

    /**
     * Define o papel do usuário após login
     */
    public void setUserRole(String role) {
        editor.putString(KEY_USER_ROLE, role);
        editor.putBoolean(KEY_IS_ADMIN, ROLE_ADMIN.equals(role));
        editor.apply();

        Log.d(TAG, "Papel do usuário definido: " + role);
    }

    /**
     * Verifica se o usuário atual é admin
     */
    public boolean isAdmin() {
        return prefs.getBoolean(KEY_IS_ADMIN, false);
    }

    /**
     * Retorna o papel do usuário
     */
    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, ROLE_USER);
    }

    /**
     * Limpa as permissões de admin (usado no logout)
     */
    public void clearAdminData() {
        editor.clear();
        editor.apply();
        Log.d(TAG, "Dados de admin limpos");
    }

    /**
     * Verifica se o email é de um administrador
     * (Esta é uma solução temporária - idealmente deveria vir do banco de dados)
     */
    public static boolean isAdminEmail(String email) {
        if (email == null) return false;

        // Lista de emails de administradores
        // IMPORTANTE: Em produção, isso deve vir do banco de dados
        String[] adminEmails = {
                "admin@example.com",
                "admin@cardapio.com",
                "eric@gmail.com"
                // Adicione mais emails de admin aqui
        };

        for (String adminEmail : adminEmails) {
            if (email.equalsIgnoreCase(adminEmail)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Verifica as permissões do usuário atual no banco de dados
     * e atualiza localmente
     */
    public void verificarPermissoes(AdminCheckCallback callback) {
        SessionManager sessionManager = SessionManager.getInstance(context);
        String email = sessionManager.getUserEmail();

        if (email == null) {
            callback.onResult(false, "Usuário não autenticado");
            return;
        }

        SupabaseClient supabaseClient = SupabaseClient.getInstance(context);
        supabaseClient.getUserByEmail(email, new SupabaseClient.SupabaseCallback<SupabaseClient.UserData>() {
            @Override
            public void onSuccess(SupabaseClient.UserData userData) {
                boolean isAdmin = ROLE_ADMIN.equals(userData.role);
                setUserRole(userData.role);

                String message = isAdmin ?
                        "Usuário é administrador" :
                        "Usuário não tem permissões de admin";

                callback.onResult(isAdmin, message);
            }

            @Override
            public void onError(String error) {
                callback.onResult(false, "Erro ao verificar permissões: " + error);
            }
        });
    }

    /**
     * Promove um usuário a admin (apenas outro admin pode fazer isso)
     */
    public void promoverParaAdmin(int userId, AdminActionCallback callback) {
        if (!isAdmin()) {
            callback.onError("Você não tem permissão para esta ação");
            return;
        }

        SupabaseClient supabaseClient = SupabaseClient.getInstance(context);
        SessionManager sessionManager = SessionManager.getInstance(context);
        String accessToken = sessionManager.getAccessToken();

        supabaseClient.updateUserRole(userId, ROLE_ADMIN, new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                callback.onSuccess("Usuário promovido a administrador");
            }

            @Override
            public void onError(String error) {
                callback.onError("Erro ao promover usuário: " + error);
            }
        });
    }

    /**
     * Remove privilégios de admin de um usuário
     */
    public void removerAdmin(int userId, AdminActionCallback callback) {
        if (!isAdmin()) {
            callback.onError("Você não tem permissão para esta ação");
            return;
        }

        SupabaseClient supabaseClient = SupabaseClient.getInstance(context);

        supabaseClient.updateUserRole(userId, ROLE_USER, new SupabaseClient.SupabaseCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                callback.onSuccess("Privilégios de admin removidos");
            }

            @Override
            public void onError(String error) {
                callback.onError("Erro ao remover privilégios: " + error);
            }
        });
    }

    // Interfaces de callback
    public interface AdminCheckCallback {
        void onResult(boolean isAdmin, String message);
    }

    public interface AdminActionCallback {
        void onSuccess(String message);
        void onError(String error);
    }
}