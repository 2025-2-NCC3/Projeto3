package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;
import java.util.Locale;

public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder> {
    private static final String TAG = "ProdutoAdapter";

    private Context context;
    private List<Produto> produtos;
    private OnProdutoClickListener listener;
    private OnAdicionarClickListener adicionarListener;
    private CarrinhoHelper carrinhoHelper;

    public interface OnProdutoClickListener {
        void onProdutoClick(Produto produto);
    }

    public interface OnAdicionarClickListener {
        void onAdicionarClick(Produto produto);
    }

    public ProdutoAdapter(Context context, List<Produto> produtos, OnProdutoClickListener listener) {
        this.context = context;
        this.produtos = produtos;
        this.listener = listener;
        this.carrinhoHelper = CarrinhoHelper.getInstance(context);
    }

    @NonNull
    @Override
    public ProdutoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.produto, parent, false);
        return new ProdutoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProdutoViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        boolean temEstoque = produto.getEstoque() > 0;

        // Configurar informações do produto
        holder.tituloProduto.setText(produto.getNome());
        holder.precoProduto.setText(String.format(Locale.getDefault(), "R$ %.2f", produto.getPreco()));

        // Carregar imagem
        String caminhoImagem = produto.getCaminhoImagem();
        if (caminhoImagem != null && !caminhoImagem.isEmpty()) {
            if (caminhoImagem.startsWith("http://") || caminhoImagem.startsWith("https://")) {
                Log.d(TAG, "Carregando imagem do Supabase: " + caminhoImagem);
                Glide.with(context)
                        .load(caminhoImagem)
                        .placeholder(R.drawable.sem_imagem)
                        .error(R.drawable.sem_imagem)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.imagemProduto);
            } else {
                String nomeImagem = caminhoImagem.replace("/", "_").replace(".", "_");
                int imageResId = context.getResources().getIdentifier(nomeImagem, "drawable", context.getPackageName());
                if (imageResId != 0) {
                    holder.imagemProduto.setImageResource(imageResId);
                } else {
                    holder.imagemProduto.setImageResource(R.drawable.sem_imagem);
                }
            }
        } else {
            holder.imagemProduto.setImageResource(R.drawable.sem_imagem);
        }

        // Alterar aparência se produto sem estoque
        if (!temEstoque) {
            holder.itemView.setAlpha(0.6f);
            holder.btnAdicionar.setVisibility(View.GONE);
        } else {
            holder.itemView.setAlpha(1.0f);
            holder.btnAdicionar.setVisibility(View.VISIBLE);
        }

        // Click no card inteiro - abre detalhes
        holder.boxProduto.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProdutoClick(produto);
            }
        });

        // ⭐ CORRIGIDO: Click no botão adicionar - adiciona ao carrinho
        holder.btnAdicionar.setOnClickListener(v -> {
            if (!temEstoque) {
                Toast.makeText(context, "❌ Produto sem estoque", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar se já atingiu o limite do estoque no carrinho
            int quantidadeNoCarrinho = carrinhoHelper.getQuantidadeProduto(produto.getId());
            if (quantidadeNoCarrinho >= produto.getEstoque()) {
                Toast.makeText(context,
                        "❌ Estoque máximo atingido (" + produto.getEstoque() + " un.)",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Adicionar ao carrinho
            carrinhoHelper.adicionarItem(produto);

            // Mostrar feedback de sucesso
            int novaQuantidade = carrinhoHelper.getQuantidadeProduto(produto.getId());
            Toast.makeText(context,
                    "✅ " + produto.getNome() + " adicionado (" + novaQuantidade + ")",
                    Toast.LENGTH_SHORT).show();

            Log.d(TAG, "Produto adicionado: " + produto.getNome() + " | Quantidade no carrinho: " + novaQuantidade);
        });
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    static class ProdutoViewHolder extends RecyclerView.ViewHolder {
        CardView boxProduto;
        ImageView imagemProduto;
        TextView tituloProduto;
        TextView precoProduto;
        ImageView btnAdicionar;

        public ProdutoViewHolder(@NonNull View itemView) {
            super(itemView);
            boxProduto = itemView.findViewById(R.id.boxProduto);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            tituloProduto = itemView.findViewById(R.id.tituloProduto);
            precoProduto = itemView.findViewById(R.id.precoProduto);
            btnAdicionar = itemView.findViewById(R.id.btnAdicionar);
        }
    }
}