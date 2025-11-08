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
    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializar SupabaseClient
        supabaseClient = SupabaseClient.getInstance(getApplicationContext());

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
                runOnUiThread(() -> handleLoginSuccess(response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> handleLoginError(error));
            }
        });
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

    private void handleLoginSuccess(SupabaseClient.AuthResponse response) {
        showLoading(false);
        Log.d(TAG, "Login bem-sucedido para: " + response.user.email);

        Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

        // Navegar para CardapioAlunosActivity
        Intent intent = new Intent(LoginActivity.this, CardapioAlunosActivity.class);
        intent.putExtra("user_email", response.user.email);
        intent.putExtra("user_id", response.user.id);
        startActivity(intent);
        finish(); // Finaliza a LoginActivity para que o usuário não possa voltar com o botão "voltar"
    }

    private void handleLoginError(String error) {
        showLoading(false);
        Log.e(TAG, "Erro no login: " + error);
        Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
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