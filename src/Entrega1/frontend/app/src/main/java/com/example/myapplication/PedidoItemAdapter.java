package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PedidoItemAdapter extends RecyclerView.Adapter<PedidoItemAdapter.ItemViewHolder> {

    private List<PedidoItem> itens;
    private Context context;

    public PedidoItemAdapter(Context context) {
        this.context = context;
        this.itens = new ArrayList<>();
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_detalhe, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        PedidoItem item = itens.get(position);
        holder.bind(item);

        // Esconde o divider do último item
        if (holder.divider != null) {
            holder.divider.setVisibility(position == getItemCount() - 1 ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public void atualizarItens(List<PedidoItem> novoItens) {
        if (novoItens != null) {
            this.itens = new ArrayList<>(novoItens);
        } else {
            this.itens = new ArrayList<>();
        }
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private ImageView imagemProduto;
        private TextView txtNomeProduto;
        private TextView txtPrecoUnitario;
        private TextView txtSubtotalItem;
        private TextView txtQuantidade;
        private View divider;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            txtNomeProduto = itemView.findViewById(R.id.txtNomeProduto);
            txtPrecoUnitario = itemView.findViewById(R.id.txtPrecoUnitario);
            txtSubtotalItem = itemView.findViewById(R.id.txtSubtotalItem);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
            divider = itemView.findViewById(R.id.divider);
        }


        public void bind(PedidoItem item) {
            // Nome do produto - com fallback
            String nomeProduto = item.getProductName();
            if (nomeProduto == null || nomeProduto.isEmpty()) {
                nomeProduto = "Produto sem nome";
                android.util.Log.e("OrderItemAdapter", "Produto sem nome! Item: " + item.toString());
            }
            txtNomeProduto.setText(nomeProduto);

            // Quantidade no formato "X x"
            int quantidade = item.getQuantity();
            txtQuantidade.setText(String.format(new Locale("pt", "BR"), "%d x", quantidade));

            // Preço unitário - usando formatação brasileira
            double preco = item.getPrice();
            txtPrecoUnitario.setText(PedidoUtils.formatarPreco(preco));

            // Subtotal (quantidade * preço) - usando formatação brasileira
            double subtotal = item.getSubtotal();
            txtSubtotalItem.setText(PedidoUtils.formatarPreco(subtotal));

            // Log para debug
            android.util.Log.d("OrderItemAdapter", "Binding item: " + nomeProduto +
                    " | Qtd: " + quantidade + " | Preço: " + preco + " | Subtotal: " + subtotal);

            // Imagem do produto
            imagemProduto.setImageResource(R.drawable.logo_tia);
        }
    }
}