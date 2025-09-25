package com.example.myapplication;

import java.util.HashMap;
import java.util.Map;

public class OrderManagerExtensions {

    // Método para buscar produto por ID - ADICIONAR ao OrderManager existente
    public static Produto getProduct(String productId) {
        // Acessa o Map products do OrderManager
        // Como o products é private static, você precisará torná-lo public ou criar este getter
        Map<String, Produto> products = getProductsMap();
        return products.get(productId);
    }

    // Método para obter todos os produtos
    public static Map<String, Produto> getAllProducts() {
        return new HashMap<>(getProductsMap());
    }

    // Este método deve ser adicionado ao OrderManager original
    // ou tornar o Map products público
    private static Map<String, Produto> getProductsMap() {
        // Retorna uma cópia do Map products do OrderManager
        Map<String, Produto> products = new HashMap<>();
        products.put("1", new Produto(1, "Café", "Café quente", "sem_imagem", 5.0, 50, 3));
        products.put("2", new Produto(2, "Sanduíche", "Sanduíche natural", "sem_imagem", 2.0, 30, 2));
        products.put("3", new Produto(3, "Suco Natural", "Suco de laranja", "sem_imagem", 3.0, 25, 3));
        products.put("4", new Produto(4, "Salgado", "Coxinha", "coxinha_exemplo", 2.5, 40, 1));
        products.put("5", new Produto(5, "Água", "Água mineral", "sem_imagem", 4.0, 60, 3));
        return products;
    }
}