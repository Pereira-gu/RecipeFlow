package com.unicid.recipeflow.dto;

import java.util.List;

public class ReceitaDTO {
    private String titulo;
    private List<String> ingredientes;
    private String passoAPasso;
    private int classificacao;
    private List<String> tags;

    public ReceitaDTO(String titulo, List<String> ingredientes, String passoAPasso, int classificacao, List<String> tags) {
        this.titulo = titulo;
        this.ingredientes = ingredientes;
        this.passoAPasso = passoAPasso;
        this.classificacao = classificacao;
        this.tags = tags;
    }

    // Getters
    public String getTitulo() { return titulo; }
    public List<String> getIngredientes() { return ingredientes; }
    public String getPassoAPasso() { return passoAPasso; }
    public int getClassificacao() { return classificacao; }
    public List<String> getTags() { return tags; }
}
