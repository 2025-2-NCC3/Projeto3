// com/example/myapplication/ui/RegisterActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import okhttp3.Call;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private ProgressBar progressBar;
    private SupabaseClient supabaseClient;
    private Call currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        buttonRegister = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> {
            hideKeyboard();
            registerUser();
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (!validateInputs(email, password, confirmPassword)) {
            return;
        }

        showLoading(true);

        cancelCurrentCall();

        currentCall = supabaseClient.signUp(email, password, new SupabaseClient.SupabaseCallback<SupabaseClient.AuthResponse>() {
            @Override
            public void onSuccess(SupabaseClient.AuthResponse response) {
                runOnUiThread(() -> handleRegistrationSuccess(response));
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> handleRegistrationError(error));
            }
        });
    }

    private boolean validateInputs(String email, String password, String confirmPassword) {
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConfirmPassword.setError(null);

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
            editTextPassword.setError("Senha deve ter pelo menos 6 caracteres");
            editTextPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            editTextConfirmPassword.setError("Senhas não coincidem");
            editTextConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void handleRegistrationSuccess(SupabaseClient.AuthResponse response) {
        showLoading(false);
        Log.d(TAG, "Registro bem-sucedido para: " + response.user.email);

        Toast.makeText(RegisterActivity.this, "Registro realizado com sucesso!", Toast.LENGTH_SHORT).show();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("email", response.user.email);
        setResult(RESULT_OK, resultIntent);

        finish();
    }

    private void handleRegistrationError(String error) {
        showLoading(false);
        Log.e(TAG, "Erro no registro: " + error);
        Toast.makeText(RegisterActivity.this, error, Toast.LENGTH_LONG).show();
    }

    private void showLoading(boolean loading) {
        if (loading) {
            progressBar.setVisibility(View.VISIBLE);
            buttonRegister.setEnabled(false);
            buttonRegister.setText("Registrando...");
        } else {
            progressBar.setVisibility(View.GONE);
            buttonRegister.setEnabled(true);
            buttonRegister.setText("Registrar");
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