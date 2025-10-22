package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private EditText editTextNome, editTextEmail, editTextPassword, editTextConfirmPassword;
    private Button buttonRegister;
    private TextView textViewLogin;
    private ProgressBar progressBar;
    private SupabaseClient supabaseClient;
    private Call currentCall;

    private SessionManager sessionManager;
    private AdminManager adminManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        supabaseClient = SupabaseClient.getInstance(getApplicationContext());
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
        buttonRegister.setOnClickListener(v -> registerUser());
        textViewLogin.setOnClickListener(v -> finish());
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

        // PASSO 1: Criar usuário no Supabase Auth
        Log.d(TAG, "Iniciando signup no Supabase Auth para: " + email);
        currentCall = supabaseClient.signUp(email, password, new SupabaseClient.SupabaseCallback<SupabaseClient.AuthResponse>() {
            @Override
            public void onSuccess(SupabaseClient.AuthResponse authResponse) {
                Log.d(TAG, "Signup Auth bem-sucedido. Auth User ID: " + authResponse.user.id);
                // PASSO 2: Criar registro na tabela users com auth_user_id
                createUserRecord(nome, email, password, authResponse.user.id, authResponse);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Erro no signup Auth: " + error);

                    String errorMessage = "Erro ao criar conta";
                    if (error.contains("already registered")) {
                        errorMessage = "Este email já está registrado";
                    } else if (error.contains("invalid email")) {
                        errorMessage = "Email inválido";
                    } else if (error.contains("password")) {
                        errorMessage = "Senha deve ter pelo menos 6 caracteres";
                    }

                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void createUserRecord(String nome, String email, String senha, String authUserId, SupabaseClient.AuthResponse authResponse) {
        Log.d(TAG, "Criando registro na tabela users com auth_user_id: " + authUserId);

        // Criar JSON com auth_user_id
        String json = String.format(
                "{\"nome\":\"%s\",\"email\":\"%s\",\"senha\":\"%s\",\"role\":\"user\",\"auth_user_id\":\"%s\"}",
                nome, email, senha, authUserId
        );

        supabaseClient.createUserWithAuthId(json, new SupabaseClient.SupabaseCallback<SupabaseClient.UserData>() {
            @Override
            public void onSuccess(SupabaseClient.UserData userData) {
                runOnUiThread(() -> {
                    Log.d(TAG, "Registro completo! User ID: " + userData.id);
                    handleRegisterSuccess(userData, authResponse);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Log.e(TAG, "Erro ao criar registro na tabela users: " + error);
                    Toast.makeText(RegisterActivity.this,
                            "Erro ao criar perfil: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void handleRegisterSuccess(SupabaseClient.UserData userData, SupabaseClient.AuthResponse authResponse) {
        showLoading(false);
        Log.d(TAG, "Registro bem-sucedido para: " + userData.email);

        // Salvar sessão com TODOS os dados
        sessionManager.createLoginSession(
                userData.id.toString(),
                userData.email,
                "user"
        );

        // Salvar o access token REAL do Supabase
        sessionManager.saveAccessToken(authResponse.accessToken, authResponse.expiresIn);
        Log.d(TAG, "Access token salvo: " + authResponse.accessToken.substring(0, 20) + "...");

        // Define como usuário comum
        adminManager.setUserRole("user");

        Toast.makeText(this, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show();

        // Retornar para LoginActivity com o email preenchido
        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", userData.email);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private boolean validateInputs(String nome, String email, String password, String confirmPassword) {
        if (nome.isEmpty()) {
            editTextNome.setError("Nome é obrigatório");
            return false;
        }

        if (email.isEmpty()) {
            editTextEmail.setError("Email é obrigatório");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            editTextEmail.setError("Email inválido");
            return false;
        }

        if (password.isEmpty()) {
            editTextPassword.setError("Senha é obrigatória");
            return false;
        }

        if (password.length() < 6) {
            editTextPassword.setError("Senha deve ter pelo menos 6 caracteres");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Senhas não coincidem");
            return false;
        }

        return true;
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

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentCall();
    }
}