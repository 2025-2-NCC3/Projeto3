// com/example/myapplication/AdminManager.java
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

    private AdminManager(Context context) {
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
}
