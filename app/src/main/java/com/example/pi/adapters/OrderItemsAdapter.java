package com.example.pi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi.R;
import com.example.pi.models.OrderItem;

import java.util.List;
import java.util.Locale;

public class OrderItemsAdapter extends RecyclerView.Adapter<OrderItemsAdapter.OrderItemViewHolder> {

    private final List<OrderItem> orderItems;

    public OrderItemsAdapter(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    @NonNull
    @Override
    public OrderItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_product, parent, false);
        return new OrderItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderItemViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    public static class OrderItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView textProductName;
        private final TextView textQuantity;
        private final TextView textUnitPrice;
        private final TextView textSubtotal;

        public OrderItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textProductName = itemView.findViewById(R.id.textProductName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            textUnitPrice = itemView.findViewById(R.id.textUnitPrice);
            textSubtotal = itemView.findViewById(R.id.textSubtotal);
        }

        public void bind(OrderItem item) {
            textProductName.setText(item.getProductName());
            textQuantity.setText(String.format(Locale.getDefault(), "Qtd: %d", item.getQuantity()));
            textUnitPrice.setText(String.format(Locale.getDefault(), "Un: R$ %.2f", item.getUnitPrice()));
            textSubtotal.setText(String.format(Locale.getDefault(), "Subtotal: R$ %.2f", item.getSubtotal()));
        }
    }

    // Método para atualizar os dados
    public void updateData(List<OrderItem> newItems) {
        orderItems.clear();
        orderItems.addAll(newItems);
        notifyDataSetChanged();
    }
}