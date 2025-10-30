package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REGISTER_REQUEST_CODE = 100;

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private ImageView togglePassword;
    private boolean isPasswordVisible = false;

    private SupabaseClient supabaseClient;
    private SessionManager sessionManager;
    private AdminManager adminManager;
    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        supabaseClient = SupabaseClient.getInstance(getApplicationContext());
        sessionManager = SessionManager.getInstance(getApplicationContext());
        adminManager = AdminManager.getInstance(getApplicationContext());

        if (sessionManager.isLoggedIn()) {
            redirectToAppropriateScreen();
            return;
        }

        if (!supabaseClient.isConfigured()) {
            Toast.makeText(this, "Erro de configuração", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);
        togglePassword = findViewById(R.id.togglePassword);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> loginUser());
        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST_CODE);
        });

        // Configurar toggle de senha
        togglePassword.setOnClickListener(v -> togglePasswordVisibility());
    }

    private void togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Ocultar senha
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye_off);
            isPasswordVisible = false;
        } else {
            // Mostrar senha
            editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            togglePassword.setImageResource(R.drawable.ic_eye);
            isPasswordVisible = true;
        }
        // Mover cursor para o final do texto
        editTextPassword.setSelection(editTextPassword.getText().length());
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);
        cancelCurrentCall();

        // PASSO 1: Autenticar com Supabase Auth
        currentCall = supabaseClient.signIn(email, password, new SupabaseClient.SupabaseCallback<SupabaseClient.AuthResponse>() {
            @Override
            public void onSuccess(SupabaseClient.AuthResponse authResponse) {
                // PASSO 2: Buscar dados do usuário na tabela users
                getUserData(authResponse);
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Erro no login: " + error);
                });
            }
        });
    }

    private void getUserData(SupabaseClient.AuthResponse authResponse) {
        supabaseClient.getUserByAuthId(authResponse.user.id, new SupabaseClient.SupabaseCallback<SupabaseClient.UserData>() {
            @Override
            public void onSuccess(SupabaseClient.UserData userData) {
                runOnUiThread(() -> {
                    handleLoginSuccess(authResponse, userData);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(LoginActivity.this,
                            "Erro ao carregar perfil: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private boolean validateInputs(String email, String password) {
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

        return true;
    }

    private void handleLoginSuccess(SupabaseClient.AuthResponse authResponse, SupabaseClient.UserData userData) {
        showLoading(false);
        Log.d(TAG, "Login bem-sucedido para: " + userData.email);

        // Salvar sessão com TODOS os dados
        sessionManager.createLoginSession(
                userData.id.toString(),
                userData.email,
                userData.role != null ? userData.role : "user"
        );

        // Salvar o access token REAL do Supabase
        sessionManager.saveAccessToken(authResponse.accessToken, authResponse.expiresIn);

        // Definir role
        String userRole = userData.role != null ? userData.role : "user";
        adminManager.setUserRole(userRole);

        boolean isAdmin = "admin".equals(userRole);
        String message = isAdmin ? "Bem-vindo, Administrador!" : "Login realizado com sucesso!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        redirectToAppropriateScreen();
    }

    private void redirectToAppropriateScreen() {
        Intent intent;

        if (adminManager.isAdmin()) {
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else {
            intent = new Intent(LoginActivity.this, CardapioAlunosActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonLogin.setEnabled(false);
            buttonLogin.setText("Entrando...");
        } else {
            progressBar.setVisibility(View.GONE);
            buttonLogin.setEnabled(true);
            buttonLogin.setText("Entrar");
        }
    }

    private void cancelCurrentCall() {
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String email = data.getStringExtra("email");
                if (email != null) {
                    editTextEmail.setText(email);
                    Toast.makeText(this, "Conta criada! Faça login.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelCurrentCall();
    }
}