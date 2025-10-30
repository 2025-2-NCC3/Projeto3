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

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ItemViewHolder> {

    private List<OrderItem> itens;
    private Context context;

    public OrderItemAdapter(Context context) {
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
        OrderItem item = itens.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return itens.size();
    }

    public void atualizarItens(List<OrderItem> novoItens) {
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

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imagemProduto = itemView.findViewById(R.id.imagemProduto);
            txtNomeProduto = itemView.findViewById(R.id.txtNomeProduto);
            txtPrecoUnitario = itemView.findViewById(R.id.txtPrecoUnitario);
            txtSubtotalItem = itemView.findViewById(R.id.txtSubtotalItem);
            txtQuantidade = itemView.findViewById(R.id.txtQuantidade);
        }

        public void bind(OrderItem item) {
            // Nome do produto - com fallback
            String nomeProduto = item.getProductName();
            if (nomeProduto == null || nomeProduto.isEmpty()) {
                nomeProduto = "Produto sem nome";
                android.util.Log.e("OrderItemAdapter", "Produto sem nome! Item: " + item.toString());
            }
            txtNomeProduto.setText(nomeProduto);

            // Quantidade no formato "X x"
            int quantidade = item.getQuantity();
            txtQuantidade.setText(String.format(Locale.getDefault(), "%d x", quantidade));

            // Preço unitário
            double preco = item.getPrice();
            txtPrecoUnitario.setText(String.format(Locale.getDefault(), "R$ %.2f", preco));

            // Subtotal (quantidade * preço)
            double subtotal = item.getSubtotal();
            txtSubtotalItem.setText(String.format(Locale.getDefault(), "R$ %.2f", subtotal));

            // Log para debug
            android.util.Log.d("OrderItemAdapter", "Binding item: " + nomeProduto +
                    " | Qtd: " + quantidade + " | Preço: " + preco + " | Subtotal: " + subtotal);

            // Imagem do produto
            imagemProduto.setImageResource(R.drawable.logo_tia);
        }
    }
}