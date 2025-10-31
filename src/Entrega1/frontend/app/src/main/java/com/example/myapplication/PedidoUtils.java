package com.example.myapplication;

import android.content.Context;
import android.content.Intent;

public class PedidoUtils {

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

    // ========================================
    // FORMATAÇÃO
    // ========================================

    public static String formatarPreco(double preco) {
        return String.format("R$ %.2f", preco);
    }

    public static String formatarData(java.util.Date data) {
        java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault());
        return formatter.format(data);
    }

    // ========================================
    // STATUS DO PEDIDO
    // ========================================

    public static int getStatusColor(String status) {
        switch (status) {
            case "PENDENTE":
                return 0xFFFFF9C4; // Amarelo claro
            case "CONFIRMADO":
                return 0xFFC8E6C9; // Verde claro
            case "PREPARANDO":
                return 0xFFFFE0B2; // Laranja claro
            case "PRONTO":
                return 0xFF81C784; // Verde
            case "ENTREGUE":
                return 0xFFB2DFDB; // Azul claro
            case "CANCELADO":
                return 0xFFFFCDD2; // Vermelho claro
            default:
                return 0xFFE0E0E0; // Cinza
        }
    }

    public static String getStatusText(String status) {
        switch (status) {
            case "PENDENTE":
                return "Pendente";
            case "CONFIRMADO":
                return "Confirmado";
            case "PREPARANDO":
                return "Preparando";
            case "PRONTO":
                return "Pronto";
            case "ENTREGUE":
                return "Entregue";
            case "CANCELADO":
                return "Cancelado";
            default:
                return status;
        }
    }

    public static String getStatusIcon(String status) {
        switch (status) {
            case "PENDENTE":
                return "⏳";
            case "CONFIRMADO":
                return "✅";
            case "PREPARANDO":
                return "👨‍🍳";
            case "PRONTO":
                return "🔔";
            case "ENTREGUE":
                return "✨";
            case "CANCELADO":
                return "❌";
            default:
                return "📦";
        }
    }

    public static String getStatusDescricao(String status) {
        switch (status) {
            case "PENDENTE":
                return "Aguardando confirmação";
            case "CONFIRMADO":
                return "Pedido confirmado";
            case "PREPARANDO":
                return "Sendo preparado";
            case "PRONTO":
                return "Pronto para retirada";
            case "ENTREGUE":
                return "Pedido entregue";
            case "CANCELADO":
                return "Pedido cancelado";
            default:
                return "Status desconhecido";
        }
    }

    // ========================================
    // VALIDAÇÕES
    // ========================================

    public static boolean podeCancelarPedido(Pedido pedido) {
        if (pedido == null) return false;
        String status = pedido.getStatus();
        return status.equals("PENDENTE") || status.equals("CONFIRMADO");
    }

    public static boolean podeAlterarStatus(Pedido pedido) {
        if (pedido == null) return false;
        String status = pedido.getStatus();
        return !status.equals("ENTREGUE") && !status.equals("CANCELADO");
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
    // COMPARTILHAMENTO
    // ========================================

    public static void compartilharPedido(Context context, Pedido pedido) {
        String texto = "📦 Meu Pedido\n\n" +
                "Código: " + pedido.getCode() + "\n" +
                "Total: " + formatarPreco(pedido.getTotal()) + "\n" +
                "Status: " + getStatusText(pedido.getStatus()) + "\n" +
                "Data: " + formatarData(pedido.getCreatedAt());

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, texto);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, "Compartilhar Pedido");
        context.startActivity(shareIntent);
    }

    // ========================================
    // CONSTANTES ÚTEIS
    // ========================================

    public static final String EXTRA_ORDER_ID = "order_id";
    public static final String EXTRA_ORDER_CODE = "order_code";
    public static final String EXTRA_PRODUTO = "produto";
    public static final String EXTRA_QUANTIDADE = "quantidade";

    // Status
    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_CONFIRMADO = "CONFIRMADO";
    public static final String STATUS_PREPARANDO = "PREPARANDO";
    public static final String STATUS_PRONTO = "PRONTO";
    public static final String STATUS_ENTREGUE = "ENTREGUE";
    public static final String STATUS_CANCELADO = "CANCELADO";
}