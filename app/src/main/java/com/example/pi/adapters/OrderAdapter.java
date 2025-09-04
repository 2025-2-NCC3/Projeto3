package com.example.pi.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pi.R;
import com.example.pi.models.Order;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {
    private List<Order> orders;

    public void setOrders(List<Order> orders) {
        this.orders = orders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orders != null ? orders.size() : 0;
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        private TextView textOrderCode, textOrderDate, textOrderStatus, textOrderTotal;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            textOrderCode = itemView.findViewById(R.id.textOrderCode);
            textOrderDate = itemView.findViewById(R.id.textOrderDate);
            textOrderStatus = itemView.findViewById(R.id.textOrderStatus);
            textOrderTotal = itemView.findViewById(R.id.textOrderTotal);
        }

        public void bind(Order order) {
            textOrderCode.setText(order.getUniqueCode());

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            textOrderDate.setText(sdf.format(order.getOrderDate()));

            textOrderStatus.setText(order.getStatus().getDescription());
            textOrderTotal.setText(String.format("R$ %.2f", order.getTotalAmount()));
        }
    }
}