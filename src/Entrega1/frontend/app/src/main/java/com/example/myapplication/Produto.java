package com.example.myapplication;

import java.io.Serializable;

public class Produto implements Serializable {
    private String id;
    private String nome;
    private String descricao;
    private String caminhoImagem;
    private double preco;
    private int estoque;
    private int categoria;

    // Construtor vazio - IMPORTANTE
    public Produto() {
    }

    // Construtor completo (ordem: id, nome, preco, descricao, caminhoImagem, estoque, categoria)
    public Produto(String id, String nome, double preco, String descricao, String caminhoImagem, int estoque, int categoria) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    // Construtor antigo para compatibilidade
    public Produto(String id, String nome, String descricao, String caminhoImagem, double preco, int estoque, int categoria) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    // Construtor sem ID (útil para inserir novos produtos)
    public Produto(String nome, String descricao, String caminhoImagem, double preco, int estoque, int categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setId(int id) {
        this.id = String.valueOf(id);
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

    // Método adicional para compatibilidade com código antigo
    public String getDetalhes() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    // Método adicional para compatibilidade
    public void setDetalhes(String detalhes) {
        this.descricao = detalhes;
    }

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
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

    // Funções úteis
    public void reduceStock(int quantidade) {
        if (estoque >= quantidade) {
            estoque -= quantidade;
        }
    }

    public void increaseStock(int quantidade) {
        estoque += quantidade;
    }

    @Override
    public String toString() {
        return "Produto{" +
                "id=" + id +
                ", nome='" + nome + '\'' +
                ", preco=" + preco +
                '}';
    }
}