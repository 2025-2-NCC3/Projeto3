// com/example/myapplication/LoginActivity.java
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

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final int REGISTER_REQUEST_CODE = 100;

    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private TextView textViewRegister;
    private ProgressBar progressBar;
    private SupabaseClient supabaseClient;
    private SessionManager sessionManager;
    private AdminManager adminManager;
    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar clientes
        supabaseClient = SupabaseClient.getInstance(getApplicationContext());
        sessionManager = SessionManager.getInstance(getApplicationContext());
        adminManager = AdminManager.getInstance(getApplicationContext());

        // Verificar se já está logado
        if (sessionManager.isLoggedIn()) {
            Log.d(TAG, "Usuário já está logado, redirecionando");
            redirectToAppropriateScreen();
            return;
        }

        // Verificar configuração do Supabase
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
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewRegister = findViewById(R.id.textViewRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        buttonLogin.setOnClickListener(v -> {
            hideKeyboard();
            loginUser();
        });

        textViewRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivityForResult(intent, REGISTER_REQUEST_CODE);
        });
    }

    private void loginUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (!validateInputs(email, password)) {
            return;
        }

        showLoading(true);
        cancelCurrentCall();

        currentCall = supabaseClient.signIn(email, password, new SupabaseClient.SupabaseCallback<SupabaseClient.AuthResponse>() {
            @Override
            public void onSuccess(SupabaseClient.AuthResponse response) {
                runOnUiThread(() -> {
                    // Salvar a sessão
                    sessionManager.createLoginSession(response);

                    // Verificar se é admin
                    checkUserRole(response.user.email, response.user.id);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> handleLoginError(error));
            }
        });
    }

    private void checkUserRole(String email, String userId) {
        // Verificar se o email é de admin (solução temporária)
        if (AdminManager.isAdminEmail(email)) {
            adminManager.setUserRole(AdminManager.ROLE_ADMIN);
            handleLoginSuccess(email, true);
            return;
        }

        // Opção avançada: Buscar role do banco de dados
        // Descomente se você tiver a tabela profiles configurada
        /*
        supabaseClient.getUserProfile(userId, new SupabaseClient.SupabaseCallback<SupabaseClient.UserProfile>() {
            @Override
            public void onSuccess(SupabaseClient.UserProfile profile) {
                runOnUiThread(() -> {
                    adminManager.setUserRole(profile.role);
                    boolean isAdmin = AdminManager.ROLE_ADMIN.equals(profile.role);
                    handleLoginSuccess(email, isAdmin);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Se der erro ao buscar perfil, assume que é usuário comum
                    Log.w(TAG, "Erro ao buscar perfil, assumindo role 'user': " + error);
                    adminManager.setUserRole(AdminManager.ROLE_USER);
                    handleLoginSuccess(email, false);
                });
            }
        });
        */

        // Por padrão, usuário comum
        adminManager.setUserRole(AdminManager.ROLE_USER);
        handleLoginSuccess(email, false);
    }

    private boolean validateInputs(String email, String password) {
        editTextEmail.setError(null);
        editTextPassword.setError(null);

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

        return true;
    }

    private void handleLoginSuccess(String email, boolean isAdmin) {
        showLoading(false);
        Log.d(TAG, "Login bem-sucedido para: " + email + " (Admin: " + isAdmin + ")");

        String message = isAdmin ? "Bem-vindo, Administrador!" : "Login realizado com sucesso!";
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();

        // Redirecionar para tela apropriada
        redirectToAppropriateScreen();
    }

    private void handleLoginError(String error) {
        showLoading(false);
        Log.e(TAG, "Erro no login: " + error);
        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
    }

    private void redirectToAppropriateScreen() {
        Intent intent;

        if (adminManager.isAdmin()) {
            // Redirecionar para tela de admin
            intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
        } else {
            // Redirecionar para tela de usuário comum
            intent = new Intent(LoginActivity.this, CardapioAlunosActivity.class);
        }

        intent.putExtra("user_email", sessionManager.getUserEmail());
        intent.putExtra("user_id", sessionManager.getUserId());
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REGISTER_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                String email = data.getStringExtra("email");
                if (email != null) {
                    editTextEmail.setText(email);
                    Toast.makeText(this, "Conta criada! Agora faça login.", Toast.LENGTH_LONG).show();
                }
            }
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