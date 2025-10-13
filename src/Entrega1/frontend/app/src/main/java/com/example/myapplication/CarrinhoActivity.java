package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class CarrinhoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCarrinho;
    private LinearLayout layoutCarrinhoVazio;
    private TextView tvSubtotal, tvQuantidadeTotal;
    private Button btnFinalizarPedido, btnLimparCarrinho;
    private ImageButton btnVoltar;
    private CarrinhoAdapter adapter;
    private CarrinhoHelper carrinhoHelper;
    private String studentId = "2023001";
    private String studentName = "Cliente Cantina";
    private String accessToken = "";
    private SupabaseOrderManager supabaseOrderManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        carrinhoHelper = CarrinhoHelper.getInstance(this);
        supabaseOrderManager = SupabaseOrderManager.getInstance(this);

        inicializarViews();
        configurarListeners();
        atualizarInterface();
    }

    private void inicializarViews() {
        recyclerViewCarrinho = findViewById(R.id.recyclerViewCarrinho);
        layoutCarrinhoVazio = findViewById(R.id.layoutCarrinhoVazio);
        tvSubtotal = findViewById(R.id.tvSubtotal);
        tvQuantidadeTotal = findViewById(R.id.tvQuantidadeTotal);
        btnFinalizarPedido = findViewById(R.id.btnFinalizarPedido);
        btnLimparCarrinho = findViewById(R.id.btnLimparCarrinho);
        btnVoltar = findViewById(R.id.btnVoltar);

        recyclerViewCarrinho.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarrinhoAdapter();
        recyclerViewCarrinho.setAdapter(adapter);
    }

    private void configurarListeners() {
        btnVoltar.setOnClickListener(v -> finish());
        btnLimparCarrinho.setOnClickListener(v -> mostrarDialogoLimpar());
        btnFinalizarPedido.setOnClickListener(v -> {
            Intent intent = new Intent(this, CriarPedidoActivity.class);
            startActivity(intent);
        });
    }

    private void atualizarInterface() {
        List<ItemCarrinho> itens = carrinhoHelper.getItens();

        if (itens.isEmpty()) {
            recyclerViewCarrinho.setVisibility(View.GONE);
            layoutCarrinhoVazio.setVisibility(View.VISIBLE);
            btnFinalizarPedido.setEnabled(false);
            btnLimparCarrinho.setEnabled(false);
            tvSubtotal.setText("R$ 0,00");
            tvQuantidadeTotal.setText("0 itens");
        } else {
            recyclerViewCarrinho.setVisibility(View.VISIBLE);
            layoutCarrinhoVazio.setVisibility(View.GONE);
            btnFinalizarPedido.setEnabled(true);
            btnLimparCarrinho.setEnabled(true);

            double subtotal = carrinhoHelper.getSubtotal();
            int quantidadeTotal = carrinhoHelper.getQuantidadeTotal();

            tvSubtotal.setText(String.format(Locale.getDefault(), "R$ %.2f", subtotal));
            tvQuantidadeTotal.setText(quantidadeTotal + " " + (quantidadeTotal == 1 ? "item" : "itens"));

            adapter.notifyDataSetChanged();
        }
    }

    private void mostrarDialogoLimpar() {
        new AlertDialog.Builder(this)
                .setTitle("Limpar Carrinho")
                .setMessage("Deseja remover todos os itens?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    carrinhoHelper.limparCarrinho();
                    atualizarInterface();
                    Toast.makeText(this, "Carrinho limpo", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarInterface();
    }

    private class CarrinhoAdapter extends RecyclerView.Adapter<CarrinhoAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_carrinho, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            List<ItemCarrinho> itens = carrinhoHelper.getItens();
            ItemCarrinho item = itens.get(position);
            Produto produto = item.getProduto();

            holder.tvNomeProduto.setText(produto.getNome());
            holder.tvPrecoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
            holder.tvQuantidade.setText(String.valueOf(item.getQuantidade()));
            holder.tvSubtotal.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getSubtotal()));

            holder.btnDiminuir.setOnClickListener(v -> {
                if (item.getQuantidade() > 1) {
                    carrinhoHelper.atualizarQuantidade(produto.getId(), item.getQuantidade() - 1);
                    atualizarInterface();
                } else {
                    mostrarDialogoRemover(produto.getId(), produto.getNome());
                }
            });

            holder.btnAumentar.setOnClickListener(v -> {
                if (item.getQuantidade() < produto.getEstoque()) {
                    carrinhoHelper.atualizarQuantidade(produto.getId(), item.getQuantidade() + 1);
                    atualizarInterface();
                } else {
                    Toast.makeText(CarrinhoActivity.this, "Estoque máximo atingido", Toast.LENGTH_SHORT).show();
                }
            });

            holder.btnRemover.setOnClickListener(v -> {
                mostrarDialogoRemover(produto.getId(), produto.getNome());
            });
        }

        @Override
        public int getItemCount() {
            return carrinhoHelper.getItens().size();
        }

        private void mostrarDialogoRemover(int produtoId, String nomeProduto) {
            new AlertDialog.Builder(CarrinhoActivity.this)
                    .setTitle("Remover Item")
                    .setMessage("Deseja remover " + nomeProduto + " do carrinho?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        carrinhoHelper.removerProduto(produtoId);
                        atualizarInterface();
                        Toast.makeText(CarrinhoActivity.this, "Item removido", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Não", null)
                    .show();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvNomeProduto, tvPrecoProduto, tvQuantidade, tvSubtotal;
            Button btnDiminuir, btnAumentar;
            ImageButton btnRemover;

            public ViewHolder(View itemView) {
                super(itemView);
                tvNomeProduto = itemView.findViewById(R.id.tvNomeProduto);
                tvPrecoProduto = itemView.findViewById(R.id.tvPrecoProduto);
                tvQuantidade = itemView.findViewById(R.id.tvQuantidade);
                tvSubtotal = itemView.findViewById(R.id.tvSubtotal);
                btnDiminuir = itemView.findViewById(R.id.btnDiminuir);
                btnAumentar = itemView.findViewById(R.id.btnAumentar);
                btnRemover = itemView.findViewById(R.id.btnRemover);
            }
        }
    }
}