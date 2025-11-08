package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import java.util.Locale;

public class PedidoUtils {

    // ========================================
    // CONFIGURAÇÃO DE STATUS
    // ========================================

    public static class StatusConfig {
        public final int corTexto;
        public final int corFundo;
        public final String texto;
        public final String icone;
        public final String descricao;

        public StatusConfig(int corTexto, int corFundo, String texto, String icone, String descricao) {
            this.corTexto = corTexto;
            this.corFundo = corFundo;
            this.texto = texto;
            this.icone = icone;
            this.descricao = descricao;
        }
    }

    /**
     * Método principal para obter todas as configurações de um status
     * @param context Contexto da aplicação
     * @param status Status do pedido (PENDING, CONFIRMED, COMPLETED, CANCELLED)
     * @return StatusConfig com todas as configurações
     */
    public static StatusConfig getStatusConfig(Context context, String status) {
        if (status == null) {
            return new StatusConfig(
                    ContextCompat.getColor(context, R.color.status_default),
                    ContextCompat.getColor(context, R.color.status_default_bg),
                    "DESCONHECIDO",
                    "❓",
                    "Status desconhecido"
            );
        }

        String statusNormalizado = normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING":
                return new StatusConfig(
                        ContextCompat.getColor(context, R.color.status_pendente),
                        ContextCompat.getColor(context, R.color.status_pendente_bg),
                        "PENDENTE",
                        "⏱",
                        "Aguardando confirmação"
                );

            case "CONFIRMED":
                return new StatusConfig(
                        ContextCompat.getColor(context, R.color.status_confirmado),
                        ContextCompat.getColor(context, R.color.status_confirmado_bg),
                        "CONFIRMADO",
                        "✓",
                        "Pedido confirmado"
                );

            case "COMPLETED":
                return new StatusConfig(
                        ContextCompat.getColor(context, R.color.status_concluido),
                        ContextCompat.getColor(context, R.color.status_concluido_bg),
                        "CONCLUÍDO",
                        "✓✓",
                        "Pedido concluído"
                );

            case "CANCELLED":
                return new StatusConfig(
                        ContextCompat.getColor(context, R.color.status_cancelado),
                        ContextCompat.getColor(context, R.color.status_cancelado_bg),
                        "CANCELADO",
                        "✕",
                        "Pedido cancelado"
                );

            default:
                return new StatusConfig(
                        ContextCompat.getColor(context, R.color.status_default),
                        ContextCompat.getColor(context, R.color.status_default_bg),
                        status.toUpperCase(),
                        "❓",
                        "Status: " + status
                );
        }
    }

    // ========================================
    // MÉTODOS INDIVIDUAIS (mantidos para compatibilidade)
    // ========================================

    public static int getStatusColor(Context context, String status) {
        return getStatusConfig(context, status).corTexto;
    }

    public static int getStatusBackgroundColor(Context context, String status) {
        return getStatusConfig(context, status).corFundo;
    }

    public static String getStatusText(String status) {
        if (status == null) return "DESCONHECIDO";

        String statusNormalizado = normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING": return "PENDENTE";
            case "CONFIRMED": return "CONFIRMADO";
            case "COMPLETED": return "CONCLUÍDO";
            case "CANCELLED": return "CANCELADO";
            default: return status.toUpperCase();
        }
    }

    public static String getStatusIcon(String status) {
        if (status == null) return "❓";

        String statusNormalizado = normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING": return "⏱";
            case "CONFIRMED": return "✓";
            case "COMPLETED": return "✓✓";
            case "CANCELLED": return "✕";
            default: return "❓";
        }
    }

    public static String getStatusDescricao(String status) {
        if (status == null) return "Status desconhecido";

        String statusNormalizado = normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING": return "Aguardando confirmação";
            case "CONFIRMED": return "Pedido confirmado";
            case "COMPLETED": return "Pedido concluído";
            case "CANCELLED": return "Pedido cancelado";
            default: return "Status desconhecido";
        }
    }

    // ========================================
    // NAVEGAÇÃO ENTRE TELAS
    // ========================================

    public static void abrirCriarPedido(Context context) {
        Intent intent = new Intent(context, CriarPedidoActivity.class);
        context.startActivity(intent);
    }

    public static void abrirMeusPedidos(Context context) {
        Intent intent = new Intent(context, MeusPedidosActivity.class);
        context.startActivity(intent);
    }

    public static void abrirCarrinho(Context context) {
        Intent intent = new Intent(context, CarrinhoActivity.class);
        context.startActivity(intent);
    }

    public static void abrirDetalhesPedido(Context context, String pedidoId, String accessToken) {
        Intent intent = new Intent(context, DetalhesPedidoActivity.class);
        intent.putExtra("pedido_id", pedidoId);
        intent.putExtra("access_token", accessToken);
        context.startActivity(intent);
    }

    public static void abrirDetalhesPedidoAdmin(Context context, String pedidoId) {
        Intent intent = new Intent(context, AdminPedidoDetalhesActivity.class);
        intent.putExtra("ORDER_ID", pedidoId);
        context.startActivity(intent);
    }

    // ========================================
    // FORMATAÇÃO
    // ========================================

    public static String formatarPreco(double preco) {
        return String.format(new Locale("pt", "BR"), "R$ %.2f", preco);
    }

    public static String formatarData(java.util.Date data) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy 'às' HH:mm", java.util.Locale.getDefault());
        return formatter.format(data);
    }

    public static String formatarDataCurta(java.util.Date data) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault());
        return formatter.format(data);
    }

    public static String formatarHora(java.util.Date data) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
        return formatter.format(data);
    }

    // ========================================
    // VALIDAÇÕES
    // ========================================

    public static boolean podeCancelarPedido(Pedido pedido) {
        if (pedido == null) return false;
        String statusNormalizado = normalizarStatus(pedido.getStatus());
        return statusNormalizado.equals("PENDING") || statusNormalizado.equals("CONFIRMED");
    }

    public static boolean podeAlterarStatus(Pedido pedido) {
        if (pedido == null) return false;
        String statusNormalizado = normalizarStatus(pedido.getStatus());
        return !statusNormalizado.equals("COMPLETED") && !statusNormalizado.equals("CANCELLED");
    }

    public static boolean podeConfirmarPedido(String status) {
        if (status == null) return false;
        String statusNormalizado = normalizarStatus(status);
        return statusNormalizado.equals("PENDING");
    }

    public static boolean podeConcluirPedido(String status) {
        if (status == null) return false;
        String statusNormalizado = normalizarStatus(status);
        return statusNormalizado.equals("CONFIRMED");
    }

    public static boolean podeSerAvaliado(String status) {
        if (status == null) return false;
        String statusNormalizado = normalizarStatus(status);
        return statusNormalizado.equals("COMPLETED");
    }

    public static boolean isEmailValido(String email) {
        if (email == null || email.isEmpty()) return false;
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isConectado(Context context) {
        android.net.ConnectivityManager cm =
                (android.net.ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            android.net.NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    // ========================================
    // NORMALIZAÇÃO DE STATUS
    // ========================================

    /**
     * Converte qualquer variação de status para o padrão inglês
     * 4 status: PENDING, CONFIRMED, COMPLETED, CANCELLED
     */
    public static String normalizarStatus(String status) {
        if (status == null) return "PENDING";

        String statusUpper = status.toUpperCase().trim();

        switch (statusUpper) {
            case "PENDENTE":
            case "PENDING":
                return "PENDING";

            case "CONFIRMADO":
            case "CONFIRMED":
                return "CONFIRMED";

            case "CONCLUÍDO":
            case "CONCLUIDO":
            case "COMPLETED":
            case "ENTREGUE":
            case "RETIRADO":
                return "COMPLETED";

            case "CANCELADO":
            case "CANCELLED":
                return "CANCELLED";

            // Status antigos que não usamos mais viram CONFIRMED
            case "PREPARANDO":
            case "PREPARING":
            case "PRONTO":
            case "READY":
                return "CONFIRMED";

            default:
                return "PENDING";
        }
    }

    // ========================================
    // CÁLCULOS
    // ========================================

    public static double calcularSubtotal(int quantidade, double precoUnitario) {
        return quantidade * precoUnitario;
    }

    public static double calcularDesconto(double valorTotal, double percentualDesconto) {
        return valorTotal * (percentualDesconto / 100.0);
    }

    public static double aplicarDesconto(double valorTotal, double percentualDesconto) {
        double desconto = calcularDesconto(valorTotal, percentualDesconto);
        return valorTotal - desconto;
    }

    // ========================================
    // MENSAGENS
    // ========================================

    public static String getMensagemSucesso(Pedido pedido) {
        return "🎉 Pedido criado com sucesso!\n\n" +
                "Código: " + pedido.getCode() + "\n" +
                "Total: " + formatarPreco(pedido.getTotal()) + "\n" +
                "Status: " + getStatusText(pedido.getStatus());
    }

    public static String getMensagemErro(String erro) {
        if (erro == null) return "Erro desconhecido";

        if (erro.contains("Estoque insuficiente")) {
            return "😔 Ops! Alguns produtos estão sem estoque.\n\nPor favor, ajuste as quantidades.";
        } else if (erro.contains("conexão") || erro.contains("network")) {
            return "🌐 Sem conexão com a internet.\n\nVerifique sua conexão e tente novamente.";
        } else if (erro.contains("401") || erro.contains("403")) {
            return "🔒 Sua sessão expirou.\n\nFaça login novamente.";
        } else {
            return "❌ " + erro;
        }
    }

    // ========================================
    // FORMATAÇÃO DE ITENS
    // ========================================

    public static String formatarListaItens(Pedido pedido) {
        if (pedido == null || pedido.getItems() == null || pedido.getItems().isEmpty()) {
            return "Sem itens";
        }

        StringBuilder itensText = new StringBuilder();
        for (int i = 0; i < pedido.getItems().size(); i++) {
            PedidoItem item = pedido.getItems().get(i);
            itensText.append("• ")
                    .append(item.getQuantity())
                    .append("x ")
                    .append(item.getProductName() != null ? item.getProductName() : "Item");

            if (i < pedido.getItems().size() - 1) {
                itensText.append("\n");
            }
        }
        return itensText.toString();
    }

    public static String formatarResumoItens(Pedido pedido) {
        if (pedido == null || pedido.getItems() == null || pedido.getItems().isEmpty()) {
            return "0 itens";
        }

        int totalItens = 0;
        for (PedidoItem item : pedido.getItems()) {
            totalItens += item.getQuantity();
        }

        return totalItens + (totalItens == 1 ? " item" : " itens");
    }

    // ========================================
    // COMPARTILHAMENTO
    // ========================================

    public static void compartilharPedido(Context context, Pedido pedido) {
        String texto = "📦 Meu Pedido - Cantina\n\n" +
                "Código: " + pedido.getCode() + "\n" +
                "Total: " + formatarPreco(pedido.getTotal()) + "\n" +
                "Status: " + getStatusText(pedido.getStatus()) + "\n" +
                "Data: " + formatarData(pedido.getCreatedAt()) + "\n\n" +
                "Itens:\n" + formatarListaItens(pedido);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartilhar Pedido");
        context.startActivity(shareIntent);
    }

    // ========================================
    // HELPERS PARA ADMIN
    // ========================================

    public static int getStatusPrioridade(String status) {
        if (status == null) return 99;

        String statusNormalizado = normalizarStatus(status);

        switch (statusNormalizado) {
            case "PENDING": return 1;
            case "CONFIRMED": return 2;
            case "COMPLETED": return 3;
            case "CANCELLED": return 4;
            default: return 99;
        }
    }

    // ========================================
    // CONSTANTES ÚTEIS
    // ========================================

    public static final String EXTRA_PEDIDO_ID = "pedido_id";
    public static final String EXTRA_PEDIDO_CODE = "pedido_code";
    public static final String EXTRA_PRODUTO = "produto";
    public static final String EXTRA_QUANTIDADE = "quantidade";

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}