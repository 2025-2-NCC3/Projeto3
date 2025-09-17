package com.example.myapplication;

public class Produto {
    private int id;
    private String nome;
    private String descricao;
    private String imagemDescricao;
    private double preco;
    private int estoque;
    private int categoria;
    private String caminhoImagem;

    public Produto(int id, String nome, String descricao, String imagemDescricao, double preco, int estoque, int categoria, String caminhoImagem){
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.imagemDescricao = imagemDescricao;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.caminhoImagem = caminhoImagem;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
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

    public String getImagemDescricao() {
        return caminhoImagem;
    }

    public void setImagemDescricao(String imagemDescricao) {
        this.imagemDescricao = imagemDescricao;
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

    public String getCaminhoImagem() {
        return caminhoImagem;
    }

    public void setCaminhoImagem(String caminhoImagem) {
        this.caminhoImagem = caminhoImagem;
    }

}
