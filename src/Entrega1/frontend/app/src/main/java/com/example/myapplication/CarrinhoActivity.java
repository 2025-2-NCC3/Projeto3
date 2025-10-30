package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.util.List;
import java.util.Locale;

public class CarrinhoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewCarrinho;
    private LinearLayout layoutCarrinhoVazio;
    private TextView txtSubtotal, txtSeusItens;
    private MaterialButton btnFinalizarPedido, btnLimparCarrinho;
    private ImageButton botaoVoltar;
    private CarrinhoAdapter adapter;
    private CarrinhoHelper carrinhoHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrinho);

        carrinhoHelper = CarrinhoHelper.getInstance(this);

        inicializarViews();
        configurarListeners();
        atualizarInterface();
    }

    private void inicializarViews() {
        recyclerViewCarrinho = findViewById(R.id.recyclerViewCarrinho);
        layoutCarrinhoVazio = findViewById(R.id.layoutCarrinhoVazio);
        txtSubtotal = findViewById(R.id.txtSubtotal);
        txtSeusItens = findViewById(R.id.txtSeusItens);
        btnFinalizarPedido = findViewById(R.id.btnFinalizarPedido);
        btnLimparCarrinho = findViewById(R.id.btnLimparCarrinho);
        botaoVoltar = findViewById(R.id.botaoVoltar);

        recyclerViewCarrinho.setLayoutManager(new LinearLayoutManager(this));
        adapter = new CarrinhoAdapter();
        recyclerViewCarrinho.setAdapter(adapter);
    }

    private void configurarListeners() {
        botaoVoltar.setOnClickListener(v -> finish());

        btnLimparCarrinho.setOnClickListener(v -> mostrarDialogoLimpar());

        btnFinalizarPedido.setOnClickListener(v -> {
            if (carrinhoHelper.getItens().isEmpty()) {
                Toast.makeText(this, "Carrinho vazio", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(this, CriarPedidoActivity.class);
            startActivity(intent);
        });
    }

    private void atualizarInterface() {
        List<ItemCarrinho> itens = carrinhoHelper.getItens();
        int quantidadeTotal = carrinhoHelper.getQuantidadeTotal();

        // Atualizar texto "Seus Itens"
        txtSeusItens.setText("Seus Itens (" + quantidadeTotal + ")");

        if (itens.isEmpty()) {
            recyclerViewCarrinho.setVisibility(View.GONE);
            layoutCarrinhoVazio.setVisibility(View.VISIBLE);
            btnFinalizarPedido.setEnabled(false);
            btnLimparCarrinho.setEnabled(false);
            txtSubtotal.setText("R$ 0,00");
        } else {
            recyclerViewCarrinho.setVisibility(View.VISIBLE);
            layoutCarrinhoVazio.setVisibility(View.GONE);
            btnFinalizarPedido.setEnabled(true);
            btnLimparCarrinho.setEnabled(true);

            double subtotal = carrinhoHelper.getSubtotal();
            txtSubtotal.setText(String.format(Locale.getDefault(), "R$ %.2f", subtotal));

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

            // Nome e descrição
            holder.txtNomeProduto.setText(produto.getNome());
            holder.txtDescricaoProduto.setText(produto.getDescricao());

            // Preços
            holder.txtPrecoUnitario.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));
            holder.txtPrecoTotalItem.setText(String.format(Locale.getDefault(), "R$ %.2f", item.getSubtotal()));

            // Quantidade
            holder.txtQuantidade.setText(String.valueOf(item.getQuantidade()));

            // Carregar imagem
            String caminhoImagem = produto.getCaminhoImagem();
            if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
                if (caminhoImagem.startsWith("http://") || caminhoImagem.startsWith("https://")) {
                    Glide.with(CarrinhoActivity.this)
                            .load(caminhoImagem)
                            .placeholder(R.drawable.sem_imagem)
                            .error(R.drawable.sem_imagem)
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(holder.imagemProduto);
                } else {
                    String nomeImagem = caminhoImagem.replace("/", "_").replace(".", "_");
                    int imageResId = getResources().getIdentifier(nomeImagem, "drawable", getPackageName());
                    if (imageResId != 0) {
                        holder.imagemProduto.setImageResource(imageResId);
                    } else {
                        holder.imagemProduto.setImageResource(R.drawable.sem_imagem);
                    }
                }
            } else {
                holder.imagemProduto.setImageResource(R.drawable.sem_imagem);
            }

            // Botão diminuir
            holder.btnDiminuir.setOnClickListener(v -> {
                if (item.getQuantidade() > 1) {
                    carrinhoHelper.atualizarQuantidade(produto.getId(), item.getQuantidade() - 1);
                    atualizarInterface();
                } else {
                    mostrarDialogoRemover(produto.getId(), produto.getNome());
                }
            });

            // Botão aumentar
            holder.btnAumentar.setOnClickListener(v -> {
                if (item.getQuantidade() < produto.getEstoque()) {
                    carrinhoHelper.atualizarQuantidade(produto.getId(), item.getQuantidade() + 1);
                    atualizarInterface();
                } else {
                    Toast.makeText(CarrinhoActivity.this, "Estoque máximo atingido", Toast.LENGTH_SHORT).show();
                }
            });

            // Botão remover
            holder.btnRemover.setOnClickListener(v -> {
                mostrarDialogoRemover(produto.getId(), produto.getNome());
            });
        }

        @Override
        public int getItemCount() {
            return carrinhoHelper.getItens().size();
        }

        private void mostrarDialogoRemover(String produtoId, String nomeProduto) {
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
            ImageView imagemProduto;
            TextView txtNomeProduto, txtDescricaoProduto, txtPrecoUnitario;
            TextView txtQuantidade, txtPrecoTotalItem;
            ImageButton btnDiminuir, btnAumentar, btnRemover;

            public ViewHolder(View itemView) {
                super(itemView);
                imagemProduto = itemView.findViewById(R.id.imagemProduto);
                txtNomeProduto = itemView.findViewById(R.id.txtNomeProduto);
                txtDescricaoProduto = itemView.findViewById(R.id.txtDescricaoProduto);
                txtPrecoUnitario = itemView.findViewById(R.id.txtPrecoUnitario);
                txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
                txtPrecoTotalItem = itemView.findViewById(R.id.txtPrecoTotalItem);
                btnDiminuir = itemView.findViewById(R.id.btnDiminuir);
                btnAumentar = itemView.findViewById(R.id.btnAumentar);
                btnRemover = itemView.findViewById(R.id.btnRemover);
            }
        }
    }
}