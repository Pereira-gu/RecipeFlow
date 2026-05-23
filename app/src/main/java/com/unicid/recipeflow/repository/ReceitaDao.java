package com.unicid.recipeflow.repository;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.ReceitaIngredienteCrossRef;

import java.util.List;

@Dao
public interface ReceitaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertReceita(Receita receita);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertIngrediente(Ingrediente ingrediente);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertReceitaIngredienteCrossRef(ReceitaIngredienteCrossRef crossRef);

    @Query("SELECT * FROM receitas WHERE isDeleted = 0 ORDER BY createdAt DESC")
    List<Receita> getAllActiveRecipes();

    @Query("SELECT * FROM receitas WHERE id = :id LIMIT 1")
    Receita getRecipeById(long id);

    @Transaction
    @Query("SELECT * FROM ingredientes WHERE id IN (SELECT idIngrediente FROM receita_ingrediente_cross_ref WHERE idReceita = :recipeId)")
    List<Ingrediente> getIngredientsForRecipe(long recipeId);

    @Query("DELETE FROM receita_ingrediente_cross_ref WHERE idReceita = :recipeId")
    void deleteIngredientsForRecipe(long recipeId);

    @Update
    void updateReceita(Receita receita);

    // Soft Delete (Requisito 2.1)
    @Query("UPDATE receitas SET isDeleted = 1 WHERE id = :id")
    void softDelete(long id);

    @Query("SELECT * FROM ingredientes")
    List<Ingrediente> getAllIngredients();

    @Transaction
    @Query("SELECT * FROM receitas WHERE id IN (SELECT idReceita FROM receita_ingrediente_cross_ref WHERE idIngrediente = :ingredienteId) AND isDeleted = 0")
    List<Receita> getRecipesByIngredient(long ingredienteId);
}
