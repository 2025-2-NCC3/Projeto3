package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminHomeActivity";

    private SessionManager sessionManager;
    private AdminManager adminManager;

    private TextView tituloPagina;
    private ImageButton btnLogout;
    private MaterialCardView cardGerenciarPedidos;
    private MaterialCardView cardEditarCardapio;
    private MaterialCardView cardEstoque;
    private MaterialCardView cardRelatorios;
    private MaterialCardView cardGerenciarUsuarios;
    private MaterialCardView cardVerCardapio;

    private OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            showLogoutDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inicializar managers
        sessionManager = SessionManager.getInstance(this);
        adminManager = AdminManager.getInstance(this);

        // VERIFICAÇÃO DE ROTA PRIVADA ADMIN
        if (!sessionManager.isLoggedIn()) {
            goToMain();
            return;
        }

        if (!adminManager.isAdmin()) {
            Toast.makeText(this, "Acesso negado. Você não tem permissões de administrador.", Toast.LENGTH_LONG).show();
            goToCardapio();
            return;
        }

        setContentView(R.layout.activity_admin_home);

        initializeViews();
        setupListeners();
        updateWelcomeMessage();

        // Adicionar callback do back button
        getOnBackPressedDispatcher().addCallback(this, backPressedCallback);
    }

    private void initializeViews() {
        tituloPagina = findViewById(R.id.tituloPagina);
        btnLogout = findViewById(R.id.btnLogout);
        cardGerenciarPedidos = findViewById(R.id.cardGerenciarPedidos);
        cardEditarCardapio = findViewById(R.id.cardEditarCardapio);
        cardEstoque = findViewById(R.id.cardEstoque);
        cardRelatorios = findViewById(R.id.cardRelatorios);
        cardGerenciarUsuarios = findViewById(R.id.cardGerenciarUsuarios);
        cardVerCardapio = findViewById(R.id.cardVerCardapio);
    }

    private void updateWelcomeMessage() {
        String userEmail = sessionManager.getUserEmail();
        if (userEmail != null && !userEmail.isEmpty()) {
            String nome = userEmail.split("@")[0];
            nome = nome.substring(0, 1).toUpperCase() + nome.substring(1);
            tituloPagina.setText("Bem-vinda, " + nome + "!");
        } else {
            tituloPagina.setText("Bem-vinda, Tia!");
        }
    }

    private void setupListeners() {
        // Botão Logout
        btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Gerenciar Pedidos
        cardGerenciarPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminPedidosActivity.class);
            startActivity(intent);
        });

        // Editar Cardápio (vai para a tela de adicionar produtos)
        cardEditarCardapio.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminListaProdutosActivity.class);
            startActivity(intent);
        });

        // Estoque
        cardEstoque.setOnClickListener(v -> {
            Toast.makeText(this, "Estoque em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        // Relatórios
        cardRelatorios.setOnClickListener(v -> {
            Toast.makeText(this, "Relatórios em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        // Gerenciar Usuários
        cardGerenciarUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Gerenciar Usuários em desenvolvimento", Toast.LENGTH_SHORT).show();
        });

        // Ver Cardápio
        cardVerCardapio.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, CardapioAlunosActivity.class);
            startActivity(intent);
        });
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Sair")
                .setMessage("Deseja fazer logout?")
                .setPositiveButton("Sim", (dialog, which) -> logout())
                .setNegativeButton("Não", null)
                .show();
    }

    private void goToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToCardapio() {
        Intent intent = new Intent(this, CardapioAlunosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logout() {
        sessionManager.logout();
        adminManager.clearAdminData();
        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        goToMain();
    }
}