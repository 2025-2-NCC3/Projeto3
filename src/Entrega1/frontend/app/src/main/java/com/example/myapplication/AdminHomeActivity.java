// com/example/myapplication/AdminHomeActivity.java
package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class AdminHomeActivity extends AppCompatActivity {

    private static final String TAG = "AdminHomeActivity";

    private SessionManager sessionManager;
    private AdminManager adminManager;

    private TextView textWelcome;
    private Button btnGerenciarProdutos;
    private Button btnGerenciarPedidos;
    private Button btnGerenciarUsuarios;
    private Button btnRelatorios;
    private Button btnVerCardapio;

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
            // Não é admin, redirecionar para tela de usuário
            Toast.makeText(this, "Acesso negado. Você não tem permissões de administrador.", Toast.LENGTH_LONG).show();
            goToCardapio();
            return;
        }

        setContentView(R.layout.activity_admin_home);

        setupToolbar();
        initializeViews();
        setupListeners();
    }

    private void goToCardapio() {
        Intent intent = new Intent(this, CardapioAlunosActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            setTitle("Painel Administrativo");
        }
    }

    private void initializeViews() {
        textWelcome = findViewById(R.id.textWelcome);
        btnGerenciarProdutos = findViewById(R.id.btnGerenciarProdutos);
        btnGerenciarPedidos = findViewById(R.id.btnGerenciarPedidos);
        btnGerenciarUsuarios = findViewById(R.id.btnGerenciarUsuarios);
        btnRelatorios = findViewById(R.id.btnRelatorios);
        btnVerCardapio = findViewById(R.id.btnVerCardapio);

        // Exibir nome do admin
        String userEmail = sessionManager.getUserEmail();
        if (userEmail != null) {
            textWelcome.setText("Bem-vindo, " + userEmail.split("@")[0]);
        }
    }

    private void setupListeners() {
        btnGerenciarProdutos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminCardapioActivity.class);
            startActivity(intent);
        });
    /*
        btnGerenciarPedidos.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, GerenciarPedidosActivity.class);
            startActivity(intent);
        });

        btnGerenciarUsuarios.setOnClickListener(v -> {
            Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(AdminHomeActivity.this, GerenciarUsuariosActivity.class);
            // startActivity(intent);
        });

        btnRelatorios.setOnClickListener(v -> {
            Toast.makeText(this, "Em desenvolvimento", Toast.LENGTH_SHORT).show();
            // Intent intent = new Intent(AdminHomeActivity.this, RelatoriosActivity.class);
            // startActivity(intent);
        });

        btnVerCardapio.setOnClickListener(v -> {
            // Admin pode ver o cardápio como usuário
            Intent intent = new Intent(AdminHomeActivity.this, CardapioAlunosActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_admin, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_perfil) {
            Toast.makeText(this, "Perfil em desenvolvimento", Toast.LENGTH_SHORT).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
    }
    private void logout() {
        // Limpar sessões
        sessionManager.logout();
        adminManager.clearAdminData();

        Toast.makeText(this, "Logout realizado com sucesso", Toast.LENGTH_SHORT).show();
        goToMain();
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

    @Override
    public void onBackPressed() {
        // Não permitir voltar com botão "voltar"
        // Ou pode mostrar dialog de confirmação
        super.onBackPressed();
        Toast.makeText(this, "Use o botão Sair para fazer logout", Toast.LENGTH_SHORT).show();
    }
}

    private void goToMain() {

    }
