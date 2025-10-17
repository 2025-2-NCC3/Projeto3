package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPedidosAdapter extends RecyclerView.Adapter<AdminPedidosAdapter.PedidoViewHolder> {

    private Context context;
    private List<AdminPedidosActivity.OrderResponse> pedidos;
    private OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onConfirmarRetirada(AdminPedidosActivity.OrderResponse pedido);
        void onCancelarPedido(AdminPedidosActivity.OrderResponse pedido);
        void onVerDetalhes(AdminPedidosActivity.OrderResponse pedido);
    }

    public AdminPedidosAdapter(Context context, List<AdminPedidosActivity.OrderResponse> pedidos, OnPedidoClickListener listener) {
        this.context = context;
        this.pedidos = pedidos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_pedido_admin, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        AdminPedidosActivity.OrderResponse pedido = pedidos.get(position);

        holder.txtNomeCliente.setText(pedido.student_name);
        holder.txtCodigo.setText("Código: " + pedido.code);
        holder.txtStatus.setText(getStatusFormatado(pedido.status));
        holder.txtData.setText(formatarData(pedido.created_at));

        NumberFormat formato = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        holder.txtValor.setText(formato.format(pedido.total_amount));

        // Configurar visibilidade dos botões baseado no status
        configurarBotoes(holder, pedido);

        // Configurar cor do status
        int corStatus = getCorStatus(pedido.status);
        holder.txtStatus.setTextColor(corStatus);

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onVerDetalhes(pedido);
            }
        });

        holder.btnConfirmarRetirada.setOnClickListener(v -> {
            if (listener != null) {
                listener.onConfirmarRetirada(pedido);
            }
        });

        holder.btnCancelar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelarPedido(pedido);
            }
        });
    }

    private void configurarBotoes(PedidoViewHolder holder, AdminPedidosActivity.OrderResponse pedido) {
        switch (pedido.status) {
            case "PENDENTE":
                holder.btnConfirmarRetirada.setText("Marcar Pronto");
                holder.btnConfirmarRetirada.setVisibility(View.VISIBLE);
                holder.btnCancelar.setVisibility(View.VISIBLE);
                break;

            case "CONFIRMADO":
                holder.btnConfirmarRetirada.setText("Marcar Pronto");
                holder.btnConfirmarRetirada.setVisibility(View.VISIBLE);
                holder.btnCancelar.setVisibility(View.VISIBLE);
                break;

            case "PRONTO":
                holder.btnConfirmarRetirada.setText("Confirmar Retirada");
                holder.btnConfirmarRetirada.setVisibility(View.VISIBLE);
                holder.btnCancelar.setVisibility(View.VISIBLE);
                break;

            case "RETIRADO":
            case "CANCELADO":
                holder.btnConfirmarRetirada.setVisibility(View.GONE);
                holder.btnCancelar.setVisibility(View.GONE);
                break;

            default:
                holder.btnConfirmarRetirada.setVisibility(View.GONE);
                holder.btnCancelar.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void atualizarLista(List<AdminPedidosActivity.OrderResponse> novosPedidos) {
        this.pedidos = novosPedidos;
        notifyDataSetChanged();
    }

    private String formatarData(String dataISO) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date data = inputFormat.parse(dataISO);
            return outputFormat.format(data);
        } catch (Exception e) {
            return dataISO;
        }
    }

    private String getStatusFormatado(String status) {
        switch (status) {
            case "PENDENTE":
                return "⏳ Pendente";
            case "CONFIRMADO":
                return "✓ Confirmado";
            case "PRONTO":
                return "🔔 Pronto";
            case "RETIRADO":
                return "✅ Retirado";
            case "CANCELADO":
                return "❌ Cancelado";
            default:
                return status;
        }
    }

    private int getCorStatus(String status) {
        switch (status) {
            case "PENDENTE":
                return context.getResources().getColor(android.R.color.holo_orange_dark);
            case "CONFIRMADO":
                return context.getResources().getColor(android.R.color.holo_blue_dark);
            case "PRONTO":
                return context.getResources().getColor(android.R.color.holo_purple);
            case "RETIRADO":
                return context.getResources().getColor(android.R.color.holo_green_dark);
            case "CANCELADO":
                return context.getResources().getColor(android.R.color.holo_red_dark);
            default:
                return context.getResources().getColor(android.R.color.black);
        }
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNomeCliente;
        TextView txtCodigo;
        TextView txtStatus;
        TextView txtData;
        TextView txtValor;
        Button btnConfirmarRetirada;
        Button btnCancelar;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNomeCliente = itemView.findViewById(R.id.txtNomeCliente);
            txtCodigo = itemView.findViewById(R.id.txtCodigo);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtData = itemView.findViewById(R.id.txtData);
            txtValor = itemView.findViewById(R.id.txtValor);
            btnConfirmarRetirada = itemView.findViewById(R.id.btnConfirmarRetirada);
            btnCancelar = itemView.findViewById(R.id.btnCancelar);
        }
    }
}