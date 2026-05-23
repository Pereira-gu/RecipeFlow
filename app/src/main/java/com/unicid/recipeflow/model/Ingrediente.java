package com.unicid.recipeflow.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "ingredientes")
public class Ingrediente implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    private String nome;

    public Ingrediente() {
    }

    public Ingrediente(Long id, String nome) {
        this.id = id;
        this.nome = nome;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    @Override
    public String toString() {
        return nome;
    }
}
