package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class CarrinhoManager {
    private static final String TAG = "CarrinhoManager";
    private static final String PREFS_NAME = "carrinho_prefs";
    private static final String CARRINHO_KEY = "carrinho_atual";

    private static CarrinhoManager instance;
    private final SharedPreferences preferences;
    private final Gson gson;
    private List<OrderItem> itensCarrinho;

    private CarrinhoManager(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        carregarCarrinho();
    }

    public static synchronized CarrinhoManager getInstance(Context context) {
        if (instance == null) {
            instance = new CarrinhoManager(context.getApplicationContext());
        }
        return instance;
    }

    // Obter itens do carrinho
    public List<OrderItem> getItensCarrinho() {
        if (itensCarrinho == null) {
            itensCarrinho = new ArrayList<>();
        }
        return new ArrayList<>(itensCarrinho); // Retorna cópia
    }

    // Adicionar produto ao carrinho
    public void adicionarProduto(Produto produto, int quantidade) {
        if (itensCarrinho == null) {
            itensCarrinho = new ArrayList<>();
        }

        // Verificar se produto já existe no carrinho
        for (int i = 0; i < itensCarrinho.size(); i++) {
            OrderItem item = itensCarrinho.get(i);
            if (item.getProductId() == produto.getId()) {
                // Atualizar quantidade do item existente
                OrderItem novoItem = new OrderItem(
                        produto.getId(),
                        produto.getNome(),
                        item.getQuantity() + quantidade,
                        produto.getPreco()
                );
                itensCarrinho.set(i, novoItem);
                salvarCarrinho();
                notificarMudanca();
                return;
            }
        }

        // Se não existe, criar novo item
        OrderItem novoItem = new OrderItem(
                produto.getId(),
                produto.getNome(),
                quantidade,
                produto.getPreco()
        );
        itensCarrinho.add(novoItem);
        salvarCarrinho();
        notificarMudanca();
    }

    // Remover produto do carrinho
    public void removerProduto(int produtoId) {
        if (itensCarrinho == null) return;

        itensCarrinho.removeIf(item -> item.getProductId() == produtoId);
        salvarCarrinho();
        notificarMudanca();
    }

    // Atualizar quantidade
    public void atualizarQuantidade(int produtoId, int novaQuantidade) {
        if (itensCarrinho == null) return;

        if (novaQuantidade <= 0) {
            removerProduto(produtoId);
            return;
        }

        for (int i = 0; i < itensCarrinho.size(); i++) {
            OrderItem item = itensCarrinho.get(i);
            if (item.getProductId() == produtoId) {
                OrderItem itemAtualizado = new OrderItem(
                        item.getProductId(),
                        item.getProductName(),
                        novaQuantidade,
                        item.getPrice()
                );
                itensCarrinho.set(i, itemAtualizado);
                salvarCarrinho();
                notificarMudanca();
                return;
            }
        }
    }

    // Limpar carrinho
    public void limparCarrinho() {
        if (itensCarrinho != null) {
            itensCarrinho.clear();
            salvarCarrinho();
            notificarMudanca();
        }
    }

    // Converter carrinho para OrderRequest
    public OrderRequest criarOrderRequest(String studentId, String studentName) {
        OrderRequest request = new OrderRequest();
        request.setStudentId(studentId);
        request.setStudentName(studentName);

        List<OrderItemRequest> itemRequests = new ArrayList<>();
        if (itensCarrinho != null) {
            for (OrderItem item : itensCarrinho) {
                OrderItemRequest itemRequest = new OrderItemRequest();
                itemRequest.setProductId(String.valueOf(item.getProductId()));
                itemRequest.setQuantity(item.getQuantity());
                itemRequests.add(itemRequest);
            }
        }
        request.setItems(itemRequests);

        return request;
    }

    // Calcular valor total do carrinho
    public double getValorTotal() {
        if (itensCarrinho == null) return 0.0;

        return itensCarrinho.stream()
                .mapToDouble(OrderItem::getSubtotal)
                .sum();
    }

    // Obter quantidade total de itens
    public int getQuantidadeTotal() {
        if (itensCarrinho == null) return 0;

        return itensCarrinho.stream()
                .mapToInt(OrderItem::getQuantity)
                .sum();
    }

    // Verificar se carrinho está vazio
    public boolean isEmpty() {
        return itensCarrinho == null || itensCarrinho.isEmpty();
    }

    // Salvar carrinho no SharedPreferences
    private void salvarCarrinho() {
        String carrinhoJson = gson.toJson(itensCarrinho);
        preferences.edit()
                .putString(CARRINHO_KEY, carrinhoJson)
                .apply();
    }

    // Carregar carrinho do SharedPreferences
    private void carregarCarrinho() {
        String carrinhoJson = preferences.getString(CARRINHO_KEY, null);
        if (carrinhoJson != null) {
            try {
                java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<OrderItem>>(){}.getType();
                itensCarrinho = gson.fromJson(carrinhoJson, listType);
            } catch (Exception e) {
                itensCarrinho = new ArrayList<>();
            }
        } else {
            itensCarrinho = new ArrayList<>();
        }
    }

    // Interface para listener de mudanças
    public interface CarrinhoListener {
        void onCarrinhoAtualizado();
    }

    private CarrinhoListener listener;

    public void setCarrinhoListener(CarrinhoListener listener) {
        this.listener = listener;
    }

    private void notificarMudanca() {
        if (listener != null) {
            listener.onCarrinhoAtualizado();
        }
    }

    // Obter informações do carrinho como string
    public String getResumoCarrinho() {
        if (isEmpty()) {
            return "Carrinho vazio";
        }

        return String.format(
                "Carrinho: %d itens - R$ %.2f",
                itensCarrinho.size(),
                getValorTotal()
        );
    }
}