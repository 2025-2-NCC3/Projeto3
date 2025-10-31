package com.example.myapplication;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PedidoAdminAdapter extends RecyclerView.Adapter<PedidoAdminAdapter.ViewHolder> {

    private List<Pedido> pedidos;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public interface OnOrderClickListener {
        void onOrderClick(Pedido pedido);
    }

    public PedidoAdminAdapter(List<Pedido> pedidos, OnOrderClickListener listener) {
        this.pedidos = pedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_admin, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);

        holder.tvOrderId.setText("Pedido #" + pedido.getId());
        holder.tvStudentName.setText(pedido.getStudentName() != null ? pedido.getStudentName() : "Aluno ID: " + pedido.getStudentId());
        holder.tvOrderDate.setText(dateFormat.format(pedido.getCreatedAt()));
        holder.tvOrderTotal.setText(currencyFormat.format(pedido.getTotal()));
        holder.tvOrderCode.setText("Código: " + (pedido.getCode() != null ? pedido.getCode() : "N/A"));

        // Configurar status visual
        String status = pedido.getStatus();
        holder.tvOrderStatus.setText(getStatusText(status));
        holder.tvOrderStatus.setTextColor(getStatusColor(status));
        holder.cardView.setCardBackgroundColor(getStatusBackgroundColor(status));

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(pedido);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void updateOrders(List<Pedido> newPedidos) {
        this.pedidos = newPedidos;
        notifyDataSetChanged();
    }

    private String getStatusText(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE":
                return "⏳ Pendente";
            case "PREPARANDO":
                return "👨‍🍳 Preparando";
            case "PRONTO":
                return "✅ Pronto";
            case "CONFIRMADO":
                return "✅ Confirmado";
            case "ENTREGUE":
                return "🎉 Entregue";
            case "RETIRADO":
                return "🎉 Retirado";
            case "CANCELADO":
                return "❌ Cancelado";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE":
                return Color.parseColor("#FF9800"); // Laranja
            case "PREPARANDO":
                return Color.parseColor("#2196F3"); // Azul
            case "PRONTO":
                return Color.parseColor("#4CAF50"); // Verde
            case "CONFIRMADO":
                return Color.parseColor("#4CAF50"); // Verde
            case "ENTREGUE":
            case "RETIRADO":
                return Color.parseColor("#009688"); // Verde-azulado
            case "CANCELADO":
                return Color.parseColor("#F44336"); // Vermelho
            default:
                return Color.parseColor("#757575"); // Cinza
        }
    }

    private int getStatusBackgroundColor(String status) {
        String statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case "PENDENTE":
                return Color.parseColor("#FFF3E0"); // Laranja claro
            case "PREPARANDO":
                return Color.parseColor("#E3F2FD"); // Azul claro
            case "PRONTO":
                return Color.parseColor("#E8F5E9"); // Verde claro
            case "CONFIRMADO":
                return Color.parseColor("#E8F5E9"); // Verde claro
            case "ENTREGUE":
            case "RETIRADO":
                return Color.parseColor("#E0F2F1"); // Verde-azulado claro
            case "CANCELADO":
                return Color.parseColor("#FFEBEE"); // Vermelho claro
            default:
                return Color.WHITE;
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvOrderId;
        TextView tvStudentName;
        TextView tvOrderDate;
        TextView tvOrderTotal;
        TextView tvOrderStatus;
        TextView tvOrderCode;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvOrderId = itemView.findViewById(R.id.tvOrderId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvOrderDate = itemView.findViewById(R.id.tvOrderDate);
            tvOrderTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvOrderStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvOrderCode = itemView.findViewById(R.id.tvOrderCode);
        }
    }
}