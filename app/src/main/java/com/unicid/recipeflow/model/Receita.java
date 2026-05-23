package com.unicid.recipeflow.model;

import java.io.Serializable;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "receitas")
public class Receita implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String titulo;
    private String fotoUrl;
    private String videoUrl;
    private String passoAPasso;
    private boolean isDeleted;
    private int classificacao;
    private String notasPessoais;
    private long createdAt;

    @Ignore
    private List<Ingrediente> ingredientes;
    @Ignore
    private List<String> tags;
    @Ignore
    private boolean traducaoFalhou;

    public Receita() {
        this.ingredientes = new ArrayList<>();
        this.tags = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
    }

    public Receita(Long id, String titulo, String passoAPasso) {
        this();
        this.id = id;
        this.titulo = titulo;
        this.passoAPasso = passoAPasso;
        this.isDeleted = false;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public List<Ingrediente> getIngredientes() { return ingredientes; }
    public void setIngredientes(List<Ingrediente> ingredientes) { this.ingredientes = ingredientes; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public void addIngrediente(Ingrediente ingrediente) {
        this.ingredientes.add(ingrediente);
    }

    public void addTag(String tag) {
        this.tags.add(tag);
    }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public String getPassoAPasso() { return passoAPasso; }
    public void setPassoAPasso(String passoAPasso) { this.passoAPasso = passoAPasso; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public int getClassificacao() { return classificacao; }
    public void setClassificacao(int classificacao) { this.classificacao = classificacao; }

    public String getNotasPessoais() { return notasPessoais; }
    public void setNotasPessoais(String notasPessoais) { this.notasPessoais = notasPessoais; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public boolean isTraducaoFalhou() { return traducaoFalhou; }
    public void setTraducaoFalhou(boolean traducaoFalhou) { this.traducaoFalhou = traducaoFalhou; }

    @Override
    public String toString() {
        return titulo;
    }
}
