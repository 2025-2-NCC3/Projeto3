package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class PerfilActivity extends AppCompatActivity {

    TextView nomeUsuario, emailUsuario;
    ConstraintLayout boxHistorico, boxDados, boxPagamento, boxConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        SessionManager sessionManager = SessionManager.getInstance(this);
        String userId = sessionManager.getUserId();

        if (userId != null) {
            buscarNomeUsuario(userId);
        }

        nomeUsuario = findViewById(R.id.nomeUsuario);
        emailUsuario = findViewById(R.id.emailUsuario);
        boxHistorico = findViewById(R.id.boxHistorico);
        boxDados = findViewById(R.id.boxDados);
        boxPagamento = findViewById(R.id.boxPagamento);
        boxConfig = findViewById(R.id.boxConfig);

        nomeUsuario.setText("Nome exemplo");
        emailUsuario.setText("EmailExemplo123@gmail.com");

        NavbarHelper.setupNavbar(this, "perfil");
        boxHistorico.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, HistoricoActivity.class);
            startActivity(intent);
        });

    }

    private void buscarNomeUsuario(String userId) {
        SupabaseClient supabaseClient = SupabaseClient.getInstance(this);

        int userIdInt = Integer.parseInt(userId);

        supabaseClient.getUserById(userIdInt, new SupabaseClient.SupabaseCallback<SupabaseClient.UserData>() {
            @Override
            public void onSuccess(SupabaseClient.UserData userData) {
                runOnUiThread(() -> {
                    String nome = userData.nome;
                    // Usar o nome aqui
                    nomeUsuario.setText("Olá, " + nome);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Log Log = null;
                    Log.e("BuscarNome", "Erro: " + error);
                });
            }
        });
    }
}


