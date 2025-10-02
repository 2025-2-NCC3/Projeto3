package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class PerfilActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        TextView nomeUsuario = findViewById(R.id.nomeUsuario);
        TextView emailUsuario = findViewById(R.id.emailUsuario);

        // Adicionar intent recebendo as informações do usuário

        nomeUsuario.setText("Nome exemplo");
        emailUsuario.setText("EmailExemplo123@gmail.com");

    }
}
