package com.example.myapplication;

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
    private OnPedidoClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public interface OnPedidoClickListener {
        void onPedidoClick(Pedido pedido);
    }

    public PedidoAdminAdapter(List<Pedido> pedidos, OnPedidoClickListener listener) {
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

        holder.tvPedidoId.setText("Pedido #" + pedido.getId());
        holder.tvStudentName.setText(pedido.getStudentName() != null ? pedido.getStudentName() : "Aluno ID: " + pedido.getStudentId());
        holder.tvPedidoDate.setText(dateFormat.format(pedido.getCreatedAt()));
        holder.tvPedidoTotal.setText(currencyFormat.format(pedido.getTotal()));
        holder.tvPedidoCode.setText("Código: " + (pedido.getCode() != null ? pedido.getCode() : "N/A"));

        // USANDO PEDIDOUTILS COM COLORS.XML
        String status = pedido.getStatus();
        String statusIcon = PedidoUtils.getStatusIcon(status);
        String statusText = PedidoUtils.getStatusText(status);

        holder.tvPedidoStatus.setText(statusIcon + " " + statusText);
        holder.tvPedidoStatus.setTextColor(PedidoUtils.getStatusColor(holder.itemView.getContext(), status));
        holder.cardView.setCardBackgroundColor(PedidoUtils.getStatusBackgroundColor(holder.itemView.getContext(), status));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPedidoClick(pedido);
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void updatePedidos(List<Pedido> newPedidos) {
        this.pedidos = newPedidos;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvPedidoId;
        TextView tvStudentName;
        TextView tvPedidoDate;
        TextView tvPedidoTotal;
        TextView tvPedidoStatus;
        TextView tvPedidoCode;

        ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvPedidoId = itemView.findViewById(R.id.tvOrderId);
            tvStudentName = itemView.findViewById(R.id.tvStudentName);
            tvPedidoDate = itemView.findViewById(R.id.tvOrderDate);
            tvPedidoTotal = itemView.findViewById(R.id.tvOrderTotal);
            tvPedidoStatus = itemView.findViewById(R.id.tvOrderStatus);
            tvPedidoCode = itemView.findViewById(R.id.tvOrderCode);
        }
    }
}