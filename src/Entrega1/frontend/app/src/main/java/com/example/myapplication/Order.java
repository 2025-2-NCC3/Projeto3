package com.example.myapplication;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import android.util.Log;

public class Order implements Serializable {
    private static final String TAG = "Order";

    @SerializedName("id")
    private String id;

    @SerializedName("id_usuario")  // CORRIGIDO: nome correto da coluna
    private String studentId;

    // Campo não existe no banco, será preenchido via JOIN ou localmente
    private String studentName;

    @SerializedName("items")
    private List<OrderItem> items;

    @SerializedName("total_amount")  // CORRIGIDO: nome correto da coluna
    private double total;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    // Code não existe no banco, será gerado localmente
    private String code;

    // Construtor vazio para Gson
    public Order() {
        this.items = new ArrayList<>();
        this.status = "PENDENTE"; // Valor padrão
    }

    // Construtor para criar novo pedido localmente
    public Order(boolean isNewOrder) {
        this();
        if (isNewOrder) {
            this.status = "PENDENTE";
            this.createdAt = getCurrentISODateTime();
            this.code = OrderManager.generateOrderCode();
        }
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public List<OrderItem> getItems() {
        if (items == null) {
            items = new ArrayList<>();
        }
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getStatus() {
        // Garantir que nunca retorna null
        return status != null ? status : "PENDENTE";
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Converte a string ISO 8601 do Supabase para Date
     */
    public Date getCreatedAtDate() {
        if (createdAt == null || createdAt.isEmpty()) {
            Log.w(TAG, "createdAt está null, retornando data atual");
            return new Date();
        }

        try {
            // Remover microssegundos extras (manter apenas 3 dígitos após o ponto)
            String normalized = createdAt.replaceAll("(\\.[0-9]{3})[0-9]+", "$1");
            Log.d(TAG, "Data original: " + createdAt);
            Log.d(TAG, "Data normalizada: " + normalized);

            // Parse com milissegundos
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.parse(normalized);

        } catch (Exception e) {
            try {
                // Fallback: sem milissegundos
                SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
                sdf2.setTimeZone(TimeZone.getTimeZone("UTC"));
                return sdf2.parse(createdAt);
            } catch (Exception e2) {
                Log.e(TAG, "Erro ao parsear data: " + createdAt, e2);
                return new Date();
            }
        }
    }

    public String getCode() {
        // Gerar código baseado no ID se não existir
        if (code == null || code.isEmpty()) {
            if (id != null) {
                code = "PED" + String.format("%06d", Integer.parseInt(id));
            } else {
                code = "PED000000";
            }
        }
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    // Métodos auxiliares
    public void addItem(OrderItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.add(item);
        calculateTotal();
    }

    public void calculateTotal() {
        total = 0;
        if (items != null) {
            for (OrderItem item : items) {
                total += item.getQuantity() * item.getPrice();
            }
        }
    }

    private String getCurrentISODateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date());
    }

    @Override
    public String toString() {
        return "Order{" +
                "id='" + id + '\'' +
                ", studentId='" + studentId + '\'' +
                ", studentName='" + studentName + '\'' +
                ", total=" + total +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", code='" + code + '\'' +
                ", items=" + (items != null ? items.size() : 0) +
                '}';
    }
}