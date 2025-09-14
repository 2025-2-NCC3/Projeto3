package com.example.pi.activities;



import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.pi.databinding.ActivityRegisterBinding;
import com.example.pi.viewmodels.RegisterViewModel;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Infla o layout usando ViewBinding
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializa o ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        setupListeners();
        setupObservers();
    }

    private void setupListeners() {
        binding.buttonRegister.setOnClickListener(view -> {
            String nome = binding.editTextNome.getText().toString().trim();
            String email = binding.editTextEmail.getText().toString().trim();
            String senha = binding.editTextSenha.getText().toString();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostra a barra de progresso e chama o ViewModel
            binding.progressBar.setVisibility(View.VISIBLE);
            viewModel.registerNewUser(nome, email, senha);
        });
    }

    private void setupObservers() {
        // Observa o LiveData de sucesso
        viewModel.getUserRegistrationSuccess().observe(this, user -> {
            // O this (LifecycleOwner) garante que o observer só funciona quando a activity está ativa
            // e é removido automaticamente para evitar memory leaks.
            binding.progressBar.setVisibility(View.GONE);
            if (user != null) {
                Toast.makeText(this, "Usuário " + user.getNome() + " registrado com sucesso!", Toast.LENGTH_LONG).show();
                // Aqui você pode navegar para a próxima tela
            }
        });

        // Observa o LiveData de erro
        viewModel.getUserRegistrationError().observe(this, errorMessage -> {
            binding.progressBar.setVisibility(View.GONE);
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }
}