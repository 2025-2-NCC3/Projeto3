package com.example.myapplication;
import java.io.Serializable;

public class Produto implements Serializable {
    private int id;
    private String nome;
    private String descricao;
    private String caminhoImagem;
    private double preco;
    private int estoque;
    private int categoria;

<<<<<<< HEAD
    @com.google.gson.annotations.SerializedName("ativo")
    private boolean ativo = true; // NOVO CAMPO

    // Construtor vazio - IMPORTANTE
=======
    // Construtor vazio - IMPORTANTE PARA RESOLVER O ERRO
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
    public Produto() {
        // Construtor vazio para criar produto e depois setar os valores
    }

<<<<<<< HEAD
    // Construtor completo (ordem: id, nome, preco, descricao, caminhoImagem, estoque, categoria)
    public Produto(String id, String nome, double preco, String descricao, String caminhoImagem, int estoque, int categoria) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.estoque = estoque;
        this.categoria = categoria;
        this.ativo = true;
    }

    // Construtor com campo ativo
    public Produto(String id, String nome, double preco, String descricao, String caminhoImagem, int estoque, int categoria, boolean ativo) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.estoque = estoque;
        this.categoria = categoria;
        this.ativo = ativo;
    }

    // Construtor antigo para compatibilidade
    public Produto(String id, String nome, String descricao, String caminhoImagem, double preco, int estoque, int categoria) {
=======
    // Construtor completo (sem imagemId)
    public Produto(int id, String nome, String descricao, String caminhoImagem, double preco, int estoque, int categoria){
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.ativo = true;
    }

    // Construtor sem ID (útil para inserir novos produtos)
    public Produto(String nome, String descricao, String caminhoImagem, double preco, int estoque, int categoria) {
        this.nome = nome;
        this.descricao = descricao;
        this.caminhoImagem = caminhoImagem;
        this.preco = preco;
        this.estoque = estoque;
        this.categoria = categoria;
        this.ativo = true;
    }

    // Getters e Setters
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

<<<<<<< HEAD
    public String getDetalhes() {
        return descricao;
    }

=======
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

<<<<<<< HEAD
    public void setDetalhes(String detalhes) {
        this.descricao = detalhes;
    }

=======
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
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

    // NOVOS MÉTODOS PARA O CAMPO ATIVO
    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
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
                ", ativo=" + ativo +
                '}';
    }
<<<<<<< HEAD

    public int getIdPrimitivo() {
        if (id != null && !id.isEmpty()) {
            try {
                return Integer.parseInt(id);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
=======
>>>>>>> 1943653 ([ERIC] Feito ajustes nas pastas e realizado entregas de PI e POO)
}