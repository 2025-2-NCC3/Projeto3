package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.List;
import java.util.Locale;

public class CriarPedidoActivity extends AppCompatActivity {

    // Views
    private ImageButton btnVoltar;
    private TextView tvNomeCliente;
    private TextView tvIdCliente;
    private TextView tvQuantidadeItens;
    private TextView tvListaProdutos;
    private TextView tvValorTotal;
    private EditText etObservacoes;
    private CardView cardStatus;
    private ProgressBar progressBar;
    private TextView tvStatus;
    private Button btnCriarPedido;

    // Managers
    private CarrinhoHelper carrinhoHelper;
    private SupabaseOrderManager orderManager;

    // Dados do usuário
    private String studentId;
    private String studentName;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_criar_pedido);

        // Inicializar
        inicializarViews();
        inicializarDados();
        carregarDadosCarrinho();
        configurarListeners();
    }

    private void inicializarViews() {
        btnVoltar = findViewById(R.id.btnVoltar);
        tvNomeCliente = findViewById(R.id.tvNomeCliente);
        tvIdCliente = findViewById(R.id.tvIdCliente);
        tvQuantidadeItens = findViewById(R.id.tvQuantidadeItens);
        tvListaProdutos = findViewById(R.id.tvListaProdutos);
        tvValorTotal = findViewById(R.id.tvValorTotal);
        etObservacoes = findViewById(R.id.etObservacoes);
        cardStatus = findViewById(R.id.cardStatus);
        progressBar = findViewById(R.id.progressBar);
        tvStatus = findViewById(R.id.tvStatus);
        btnCriarPedido = findViewById(R.id.btnCriarPedido);
    }

    private void inicializarDados() {
        // Inicializar managers
        carrinhoHelper = CarrinhoHelper.getInstance(this);
        orderManager = SupabaseOrderManager.getInstance(this);

        // Obter dados do usuário do SharedPreferences
        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        studentId = prefs.getString("student_id", "2023001");
        studentName = prefs.getString("student_name", "Cliente Cantina");
        accessToken = prefs.getString("access_token", "");

        // Atualizar UI com dados do cliente
        tvNomeCliente.setText(studentName);
        tvIdCliente.setText(studentId);

        // Verificar se está logado
        if (accessToken.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Não Autenticado")
                    .setMessage("Você precisa fazer login para criar pedidos.")
                    .setPositiveButton("Fazer Login", (dialog, which) -> {
                        // Ir para tela de login
                        Intent intent = new Intent(this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancelar", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void carregarDadosCarrinho() {
        // Verificar se carrinho está vazio
        if (carrinhoHelper.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Carrinho Vazio")
                    .setMessage("Adicione produtos ao carrinho antes de criar um pedido.")
                    .setPositiveButton("OK", (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
            return;
        }

        // Obter dados do carrinho
        List<ItemCarrinho> itens = carrinhoHelper.getItens();
        int quantidadeTotal = carrinhoHelper.getQuantidadeTotal();
        double valorTotal = carrinhoHelper.getSubtotal();

        // Atualizar UI
        tvQuantidadeItens.setText(String.valueOf(itens.size()));
        tvValorTotal.setText(String.format(Locale.getDefault(), "R$ %.2f", valorTotal));

        // Montar lista de produtos
        StringBuilder listaProdutos = new StringBuilder();
        for (ItemCarrinho item : itens) {
            listaProdutos.append("• ")
                    .append(item.getProduto().getNome())
                    .append(" x")
                    .append(item.getQuantidade())
                    .append("\n");
        }
        tvListaProdutos.setText(listaProdutos.toString().trim());
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());

        btnCriarPedido.setOnClickListener(v -> {
            // Mostrar diálogo de confirmação
            mostrarDialogoConfirmacao();
        });
    }

    private void mostrarDialogoConfirmacao() {
        String mensagem = "Deseja confirmar o pedido?\n\n" +
                "Valor Total: " + tvValorTotal.getText() + "\n" +
                "Quantidade: " + tvQuantidadeItens.getText() + " itens";

        new AlertDialog.Builder(this)
                .setTitle("Confirmar Pedido")
                .setMessage(mensagem)
                .setPositiveButton("Sim, Confirmar", (dialog, which) -> {
                    criarPedido();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarPedido() {
        // 1. Validar estoque
        String erroEstoque = carrinhoHelper.validarEstoque();
        if (erroEstoque != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Estoque Insuficiente")
                    .setMessage(erroEstoque)
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        // 2. Mostrar loading
        mostrarLoading(true, "Criando pedido...");

        // 3. Criar OrderRequest do carrinho
        OrderRequest request = carrinhoHelper.criarOrderRequest(studentId, studentName);

        // 4. Adicionar observações se houver
        String observacoes = etObservacoes.getText().toString().trim();
        // Note: OrderRequest não tem campo observações, mas você pode adicionar

        // 5. Criar pedido no Supabase
        orderManager.createOrder(request, accessToken, new SupabaseOrderManager.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                runOnUiThread(() -> {
                    // Esconder loading
                    mostrarLoading(false, "");

                    // Limpar carrinho
                    carrinhoHelper.limparCarrinho();

                    // Mostrar diálogo de sucesso
                    mostrarDialogoSucesso(order);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    // Esconder loading
                    mostrarLoading(false, "");

                    // Mostrar diálogo de erro
                    mostrarDialogoErro(error);
                });
            }
        });
    }

    private void mostrarLoading(boolean mostrar, String mensagem) {
        if (mostrar) {
            cardStatus.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            tvStatus.setText(mensagem);
            btnCriarPedido.setEnabled(false);
            btnCriarPedido.setText("Processando...");
        } else {
            cardStatus.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            btnCriarPedido.setEnabled(true);
            btnCriarPedido.setText("✅ Confirmar Pedido");
        }
    }

    private void mostrarDialogoSucesso(Order order) {
        String mensagem = "🎉 Pedido realizado com sucesso!\n\n" +
                "━━━━━━━━━━━━━━━━━━━━\n" +
                "📝 Código: " + order.getCode() + "\n" +
                "💰 Total: R$ " + String.format(Locale.getDefault(), "%.2f", order.getTotal()) + "\n" +
                "📊 Status: " + order.getStatus() + "\n" +
                "━━━━━━━━━━━━━━━━━━━━\n\n" +
                "Você pode acompanhar seu pedido na tela 'Meus Pedidos'.";

        new AlertDialog.Builder(this)
                .setTitle("✅ Pedido Criado!")
                .setMessage(mensagem)
                .setPositiveButton("Ver Meus Pedidos", (dialog, which) -> {
                    // Ir para tela de Meus Pedidos
                    Intent intent = new Intent(this, MeusPedidosActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Voltar ao Início", (dialog, which) -> {
                    // Voltar para tela principal
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void mostrarDialogoErro(String erro) {
        String mensagem;
        String titulo;

        // Tratar diferentes tipos de erro
        if (erro.contains("Estoque insuficiente")) {
            titulo = "❌ Produto Esgotado";
            mensagem = erro + "\n\nPor favor, ajuste as quantidades no carrinho.";
        } else if (erro.contains("conexão") || erro.contains("network")) {
            titulo = "🌐 Sem Conexão";
            mensagem = "Não foi possível conectar ao servidor.\n\nVerifique sua conexão com a internet e tente novamente.";
        } else if (erro.contains("401") || erro.contains("403") || erro.contains("não está configurado")) {
            titulo = "🔒 Erro de Autenticação";
            mensagem = "Sua sessão expirou.\n\nPor favor, faça login novamente.";
        } else {
            titulo = "❌ Erro ao Criar Pedido";
            mensagem = erro;
        }

        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensagem)
                .setPositiveButton("Tentar Novamente", (dialog, which) -> {
                    criarPedido();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar dados do carrinho caso tenha voltado de outra tela
        if (!carrinhoHelper.isEmpty()) {
            carregarDadosCarrinho();
        }
    }

    @Override
    public void onBackPressed() {
        // Confirmar antes de sair
        new AlertDialog.Builder(this)
                .setTitle("Cancelar Pedido?")
                .setMessage("Deseja cancelar a criação do pedido?")
                .setPositiveButton("Sim, Cancelar", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Não, Continuar", null)
                .show();
    }
}