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
    private AdminManager adminManager;  // ← ADICIONAR

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar Managers
        sessionManager = SessionManager.getInstance(this);
        adminManager = AdminManager.getInstance(this);  // ← ADICIONAR

        // VERIFICAÇÃO DE ROTA PRIVADA
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuário já está autenticado, redirecionando");
            redirectToAppropriateScreen();  // ← MUDAR AQUI
            return;
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
     * Redireciona para a tela apropriada baseado no role do usuário
     */
    private void redirectToAppropriateScreen() {  // ← NOVO MÉTODO
        Intent intent;

        if (adminManager.isAdmin()) {
            Log.d(TAG, "Redirecionando admin para AdminHomeActivity");
            intent = new Intent(MainActivity.this, AdminHomeActivity.class);
        } else {
            Log.d(TAG, "Redirecionando aluno para CardapioAlunosActivity");
            intent = new Intent(MainActivity.this, CardapioAlunosActivity.class);
            intent.putExtra("user_email", sessionManager.getUserEmail());
            intent.putExtra("user_id", sessionManager.getUserId());
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuário autenticado detectado no onResume");
            redirectToAppropriateScreen();  // ← MUDAR AQUI TAMBÉM
        }
    }
}