package com.unicid.recipeflow.model;

import androidx.room.Entity;

@Entity(primaryKeys = {"idReceita", "idIngrediente"}, tableName = "receita_ingrediente_cross_ref")
public class ReceitaIngredienteCrossRef {
    private long idReceita;
    private long idIngrediente;

    public ReceitaIngredienteCrossRef(long idReceita, long idIngrediente) {
        this.idReceita = idReceita;
        this.idIngrediente = idIngrediente;
    }

    public long getIdReceita() {
        return idReceita;
    }

    public void setIdReceita(long idReceita) {
        this.idReceita = idReceita;
    }

    public long getIdIngrediente() {
        return idIngrediente;
    }

    public void setIdIngrediente(long idIngrediente) {
        this.idIngrediente = idIngrediente;
    }
}
