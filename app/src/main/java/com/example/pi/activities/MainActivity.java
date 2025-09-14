package com.example.pi.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pi.databinding.ActivityMainBinding; // Import da classe de binding gerada

public class MainActivity extends AppCompatActivity {

    // 1. Declaração da variável de binding
    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. Infla o layout usando ViewBinding e o associa a esta activity
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 3. Chama o método que configura os cliques dos botões
        setupClickListeners();
    }

    /**
     * Método para organizar a configuração dos listeners de clique.
     */
    private void setupClickListeners() {
        // Listener para o botão de Login
        binding.buttonGoToLogin.setOnClickListener(v -> {
            // Cria uma Intent para navegar da MainActivity para a LoginActivity
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Listener para o botão de Registro
        binding.buttonGoToRegister.setOnClickListener(v -> {
            // Cria uma Intent para navegar da MainActivity para a RegisterActivity
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }
}