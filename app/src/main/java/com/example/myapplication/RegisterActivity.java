package com.example.myapplication;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    // 1. Variáveis para os componentes da UI e para o Firestore
    private EditText editTextNome, editTextEmail, editTextSenha;
    private Button btnRegister;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // 2. Inicializar o Firestore
        db = FirebaseFirestore.getInstance();


        mAuth = FirebaseAuth.getInstance(); // Inicializar o Auth

        // 3. Conectar as variáveis com os componentes do XML
        editTextNome = findViewById(R.id.editNomeRegister); // Substitua pelo ID real
        editTextEmail = findViewById(R.id.editEmailRegister); // Substitua pelo ID real
        editTextSenha = findViewById(R.id.editSenhaRegister); // Substitua pelo ID real
        btnRegister = findViewById(R.id.btnRegistrar); // Substitua pelo ID real

        // 4. Configurar o clique do botão
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registrarUsuario();
            }
        });
    }

    private void registrarUsuario() {
        String nome = editTextNome.getText().toString().trim();
        String email = editTextEmail.getText().toString().trim();
        String senha = editTextSenha.getText().toString().trim();

        if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Por favor, preencha todos os campos.", Toast.LENGTH_SHORT).show();
            return;
        }

        // PASSO 1: Criar o usuário no Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Usuário criado com sucesso no Auth
                            FirebaseUser firebaseUser = mAuth.getCurrentUser();
                            String uid = firebaseUser.getUid();

                            // PASSO 2: Salvar informações extras (nome) no Firestore
                            Map<String, Object> user = new HashMap<>();
                            user.put("uid", uid); // Salva o ID único do usuário
                            user.put("nome", nome);
                            user.put("email", email);

                            db.collection("users").document(uid).set(user)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(RegisterActivity.this, "Registro bem-sucedido!", Toast.LENGTH_SHORT).show();
                                        finish(); // Volta para a tela de login
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(RegisterActivity.this, "Erro ao salvar dados do usuário.", Toast.LENGTH_SHORT).show();
                                    });

                        } else {
                            // Se o registro falhar (ex: email já existe), mostre uma mensagem
                            Toast.makeText(RegisterActivity.this, "Falha no registro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}