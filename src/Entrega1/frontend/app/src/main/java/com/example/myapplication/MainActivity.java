// com/example/myapplication/MainActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private CardView btnMainLogin;
    private CardView btnMainRegister;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar SessionManager
        sessionManager = SessionManager.getInstance(this);

        // VERIFICAÇÃO DE ROTA PRIVADA
        // Se o usuário já estiver logado, redirecionar para o CardapioAlunosActivity
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuário já está autenticado, redirecionando para CardapioAlunosActivity");
            goToCardapio();
            return; // Importante: retorna aqui para não continuar a execução
        }

        // Se não estiver logado, mostra a tela de boas-vindas
        setContentView(R.layout.activity_main);

        btnMainLogin = findViewById(R.id.btnMainLogin);
        btnMainRegister = findViewById(R.id.btnMainRegister);

        btnMainLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btnMainRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Redireciona para o cardápio (área logada)
     */
    private void goToCardapio() {
        Intent intent = new Intent(MainActivity.this, CardapioAlunosActivity.class);
        intent.putExtra("user_email", sessionManager.getUserEmail());
        intent.putExtra("user_id", sessionManager.getUserId());

        // Remove todas as activities anteriores da pilha
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifica novamente quando a activity volta ao foco
        // Útil caso o usuário faça logout e volte
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuário autenticado detectado no onResume");
            goToCardapio();
        }
    }
}