package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CarrinhoHelper {
    private static final String PREFS_NAME = "carrinho_prefs";
    private static final String KEY_ITENS = "itens_carrinho";

    private static CarrinhoHelper instance;
    private SharedPreferences prefs;
    private Gson gson;
    private List<ItemCarrinho> itens;

    private CarrinhoHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
        carregarItens();
    }

    public static synchronized CarrinhoHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CarrinhoHelper(context.getApplicationContext());
        }
        return instance;
    }

    // Adicionar produto ao carrinho
    public void adicionarProduto(Produto produto, int quantidade) {
        // Verificar se produto já existe no carrinho
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produto.getId())) {  // MUDADO: usando .equals()
                // Atualizar quantidade
                item.setQuantidade(item.getQuantidade() + quantidade);
                salvarItens();
                return;
            }
        }

        // Adicionar novo item
        itens.add(new ItemCarrinho(produto, quantidade));
        salvarItens();
    }

    // Remover produto do carrinho
    public void removerProduto(String produtoId) {  // MUDADO: String ao invés de int
        itens.removeIf(item -> item.getProduto().getId().equals(produtoId));  // MUDADO: .equals()
        salvarItens();
    }

    // Atualizar quantidade de um produto
    public void atualizarQuantidade(String produtoId, int novaQuantidade) {  // MUDADO: String
        if (novaQuantidade <= 0) {
            removerProduto(produtoId);
            return;
        }

        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produtoId)) {  // MUDADO: .equals()
                item.setQuantidade(novaQuantidade);
                salvarItens();
                return;
            }
        }
    }

    // Limpar carrinho
    public void limparCarrinho() {
        itens.clear();
        salvarItens();
    }

    // Obter todos os itens
    public List<ItemCarrinho> getItens() {
        return new ArrayList<>(itens);
    }

    // Verificar se carrinho está vazio
    public boolean isEmpty() {
        return itens.isEmpty();
    }

    // Obter quantidade total de itens
    public int getQuantidadeTotal() {
        int total = 0;
        for (ItemCarrinho item : itens) {
            total += item.getQuantidade();
        }
        return total;
    }

    // Calcular subtotal do carrinho
    public double getSubtotal() {
        double subtotal = 0;
        for (ItemCarrinho item : itens) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    // Obter quantidade de um produto específico no carrinho
    public int getQuantidadeProduto(String produtoId) {  // MUDADO: String
        for (ItemCarrinho item : itens) {
            if (item.getProduto().getId().equals(produtoId)) {  // MUDADO: .equals()
                return item.getQuantidade();
            }
        }
        return 0;
    }

    // Verificar se produto está no carrinho
    public boolean contemProduto(String produtoId) {  // MUDADO: String
        return getQuantidadeProduto(produtoId) > 0;
    }

    // Salvar itens no SharedPreferences
    private void salvarItens() {
        String json = gson.toJson(itens);
        prefs.edit().putString(KEY_ITENS, json).apply();
    }

    // Carregar itens do SharedPreferences
    private void carregarItens() {
        String json = prefs.getString(KEY_ITENS, null);
        if (json != null) {
            Type type = new TypeToken<List<ItemCarrinho>>(){}.getType();
            itens = gson.fromJson(json, type);
            if (itens == null) {
                itens = new ArrayList<>();
            }
        } else {
            itens = new ArrayList<>();
        }
    }

    // Converter carrinho para OrderRequest
    public OrderRequest criarOrderRequest(String studentId, String studentName) {
        OrderRequest request = new OrderRequest();
        request.setStudentId(studentId);
        request.setStudentName(studentName);

        List<OrderItemRequest> items = new ArrayList<>();
        for (ItemCarrinho itemCarrinho : itens) {
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(itemCarrinho.getProduto().getId());  // Já é String
            item.setQuantity(itemCarrinho.getQuantidade());
            items.add(item);
        }
        request.setItems(items);

        return request;
    }

    // Validar estoque do carrinho
    public String validarEstoque() {
        for (ItemCarrinho item : itens) {
            Produto produto = item.getProduto();
            if (produto.getEstoque() < item.getQuantidade()) {
                return "Estoque insuficiente para: " + produto.getNome() +
                        "\nDisponível: " + produto.getEstoque() +
                        "\nSolicitado: " + item.getQuantidade();
            }
        }
        return null;
    }
}