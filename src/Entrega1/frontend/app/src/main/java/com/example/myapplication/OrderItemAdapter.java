package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.OrderItem;
import java.util.ArrayList;
import java.util.List;

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
        this.itens = novoItens;
        notifyDataSetChanged();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView tvNomeProduto;
        private TextView tvQuantidade;
        private TextView tvPrecoProduto;
        private TextView tvSubtotal;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeProduto = itemView.findViewById(R.id.tv_nome_produto);
            tvQuantidade = itemView.findViewById(R.id.tv_quantidade);
            tvPrecoProduto = itemView.findViewById(R.id.tv_preco_produto);
            tvSubtotal = itemView.findViewById(R.id.tv_subtotal);
        }

        public void bind(OrderItem item) {
            tvNomeProduto.setText(item.getProductName());
            tvQuantidade.setText("Qtd: " + item.getQuantity());
            tvPrecoProduto.setText("R$ " + String.format("%.2f", item.getPrice()));

            double subtotal = item.getSubtotal();
            tvSubtotal.setText("Subtotal: R$ " + String.format("%.2f", subtotal));
        }
    }
}