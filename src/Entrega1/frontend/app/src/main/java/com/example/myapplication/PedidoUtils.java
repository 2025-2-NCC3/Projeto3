package com.example.myapplication;

import android.graphics.Color;
import android.util.Log;

public class PedidoUtils {
    private static final String TAG = "PedidoUtils";

    /**
     * Retorna o texto formatado do status
     */
    public static String getStatusText(String status) {
        // CRÍTICO: Verificar se status é null
        if (status == null || status.isEmpty()) {
            Log.w(TAG, "Status está null ou vazio, retornando 'Desconhecido'");
            return "Desconhecido";
        }

        switch (status.toLowerCase()) {
            case "pendente":
                return "Pendente";
            case "preparando":
                return "Em Preparo";
            case "pronto":
                return "Pronto";
            case "entregue":
                return "Entregue";
            case "cancelado":
                return "Cancelado";
            default:
                Log.w(TAG, "Status desconhecido: " + status);
                return "Desconhecido";
        }
    }

    /**
     * Retorna a cor do status
     */
    public static int getStatusColor(String status) {
        // CRÍTICO: Verificar se status é null
        if (status == null || status.isEmpty()) {
            return Color.parseColor("#808080"); // Cinza
        }

        switch (status.toLowerCase()) {
            case "pendente":
                return Color.parseColor("#FF9800"); // Laranja
            case "preparando":
                return Color.parseColor("#2196F3"); // Azul
            case "pronto":
                return Color.parseColor("#4CAF50"); // Verde
            case "entregue":
                return Color.parseColor("#9E9E9E"); // Cinza
            case "cancelado":
                return Color.parseColor("#F44336"); // Vermelho
            default:
                return Color.parseColor("#808080"); // Cinza
        }
    }

    /**
     * Retorna o ícone emoji do status
     */
    public static String getStatusIcon(String status) {
        // CRÍTICO: Verificar se status é null
        if (status == null || status.isEmpty()) {
            return "❓";
        }

        switch (status.toLowerCase()) {
            case "pendente":
                return "⏳";
            case "preparando":
                return "👨‍🍳";
            case "pronto":
                return "✅";
            case "entregue":
                return "🎉";
            case "cancelado":
                return "❌";
            default:
                return "❓";
        }
    }

    /**
     * Formata data do pedido
     */
    public static String formatarData(String createdAt) {
        if (createdAt == null || createdAt.isEmpty()) {
            return "Data não disponível";
        }

        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                    java.util.Locale.US
            );
            inputFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Date date = inputFormat.parse(createdAt);

            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat(
                    "dd/MM/yyyy 'às' HH:mm",
                    java.util.Locale.getDefault()
            );

            return outputFormat.format(date);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao formatar data: " + createdAt, e);
            return "Data inválida";
        }
    }
}