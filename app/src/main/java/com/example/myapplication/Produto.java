package com.example.myapplication;

public class Produto {
    private String id;
    private String nome;
    private String descricao;
    private double preco;
    private int estoque;
    private int categoria;
    private static int numeroInstancias;

    public Produto(String id, String nome, String descricao, double preco, int estoque, int categoria){
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        numeroInstancias++;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public int getCategoria() {
        return categoria;
    }

    public void setCategoria(int categoria) {
        this.categoria = categoria;
    }

    public static int getNumeroInstancias() {
        return numeroInstancias;
    }


    public boolean hasStock(int quantity) {
        return estoque >= quantity;
    }

    public void reduceStock(int quantity) {
        if (hasStock(quantity)) {
            estoque -= quantity;
        }
    }

    public void increaseStock(int quantity) {
        estoque += quantity;
    }

    @Override
    public String toString() {
        return nome + " - R$ " + preco + " (Estoque: " + estoque + ")";
    }
}
