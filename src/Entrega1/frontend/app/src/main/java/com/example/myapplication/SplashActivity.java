// com/example/myapplication/SplashActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Tela de splash que verifica o estado de autenticação
 * e redireciona para a tela apropriada
 */
public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "SplashActivity";
    private static final long SPLASH_DELAY = 1500; // 1.5 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Aguarda um momento antes de verificar a autenticação
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            checkAuthentication();
        }, SPLASH_DELAY);
    }

    private void checkAuthentication() {
        SessionManager sessionManager = SessionManager.getInstance(this);

        Log.d(TAG, sessionManager.getSessionInfo());

        if (sessionManager.isLoggedIn()) {
            // Usuário está logado, vai para o cardápio
            Log.d(TAG, "Usuário autenticado, redirecionando para CardapioAlunosActivity");
            goToCardapio();
        } else {
            // Usuário não está logado, vai para login
            Log.d(TAG, "Usuário não autenticado, redirecionando para LoginActivity");
            goToLogin();
        }
    }

    private void goToCardapio() {
        SessionManager sessionManager = SessionManager.getInstance(this);

        Intent intent = new Intent(SplashActivity.this, CardapioAlunosActivity.class);
        intent.putExtra("user_email", sessionManager.getUserEmail());
        intent.putExtra("user_id", sessionManager.getUserId());

        // Remove todas as activities anteriores da pilha
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);

        // Remove todas as activities anteriores da pilha
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }
}