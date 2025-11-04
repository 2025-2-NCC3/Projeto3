package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    TextView nomeUsuario, emailUsuario;
    
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

        nomeUsuario.setText("Nome exemplo");
        emailUsuario.setText("EmailExemplo123@gmail.com");

        NavbarHelper.setupNavbar(this, "perfil");
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


