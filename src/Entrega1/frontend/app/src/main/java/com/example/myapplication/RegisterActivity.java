// com/example/myapplication/RegisterActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextNome;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ProgressBar progressBar;
    private SupabaseClient supabaseClient;
    private SessionManager sessionManager;
    private AdminManager adminManager;
    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializar clients
        supabaseClient = SupabaseClient.getInstance(getApplicationContext());
        sessionManager = SessionManager.getInstance(getApplicationContext());
        adminManager = AdminManager.getInstance(getApplicationContext());

        // Verificar configuração
        if (!supabaseClient.isConfigured()) {
            Toast.makeText(this, "Erro de configuração do Supabase", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Supabase não está configurado corretamente");
            finish();
            return;
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        editTextNome = findViewById(R.id.editTextNome);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        textViewLogin = findViewById(R.id.textViewLogin);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> {
            hideKeyboard();
            registerUser();
        });

        textViewLogin.setOnClickListener(v -> {
            finish(); // Volta para LoginActivity
        });
    }

    private void registerUser() {
        String nome = editTextNome.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (!validateInputs(nome, email, password, confirmPassword)) {
            return;
        }

        showLoading(true);
        cancelCurrentCall();

        // Criar novo usuário na tabela users
        currentCall = supabaseClient.createUser(nome, email, password, new SupabaseClient.SupabaseCallback<SupabaseClient.UserData>() {
            @Override
            public void onSuccess(SupabaseClient.UserData userData) {
                runOnUiThread(() -> handleRegisterSuccess(userData));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> handleRegisterError(error));
            }
        });
    }

    private boolean validateInputs(String nome, String email, String password, String confirmPassword) {
        editTextNome.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

        if (nome.isEmpty()) {
            editTextNome.setError("Nome é obrigatório");
            editTextNome.requestFocus();
            return false;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            editTextEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email inválido");
            editTextEmail.requestFocus();
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Senha é obrigatória");
            editTextPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("A senha deve ter pelo menos 6 caracteres");
            editTextPassword.requestFocus();
            return false;
        }

        if (confirmPassword.isEmpty()) {
            editTextConfirmPassword.setError("Confirmação de senha é obrigatória");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("As senhas não correspondem");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void handleRegisterSuccess(SupabaseClient.UserData userData) {
        showLoading(false);
        Log.d(TAG, "Registro bem-sucedido para: " + userData.email);

        // Salvar sessão
        sessionManager.createLoginSession(
                userData.id.toString(),
                userData.email,
                "user"
        );

        // Define como usuário comum
        adminManager.setUserRole("user");

        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

        // Ir direto para o cardápio
        goToCardapio();
    }

    private void handleRegisterError(String error) {
        showLoading(false);
        Log.e(TAG, "Erro no registro: " + error);

        // Mensagens de erro amigáveis
        String errorMessage = "Erro ao criar conta";

        if (error.contains("duplicate") || error.contains("unique")) {
            errorMessage = "Este email já está registrado";
        } else if (error.contains("email")) {
            errorMessage = "Email inválido";
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
    }

    private void goToCardapio() {
        Intent intent = new Intent(RegisterActivity.this, CardapioAlunosActivity.class);
        intent.putExtra("user_email", sessionManager.getUserEmail());
        intent.putExtra("user_id", sessionManager.getUserId());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(intent);
        finish();
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);
            buttonRegister.setText("Criando conta...");
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
            buttonRegister.setText("Criar Conta");
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cancelCurrentCall();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentCall();
    }
}