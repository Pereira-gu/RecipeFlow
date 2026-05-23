package com.unicid.recipeflow.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MealResponse {
    @SerializedName("meals")
    public List<Map<String, String>> meals;

    public Receita toReceita() {
        if (meals == null || meals.isEmpty()) return null;
        Map<String, String> map = meals.get(0);
        
        Receita receita = new Receita();
        receita.setTitulo(map.get("strMeal"));
        receita.setPassoAPasso(map.get("strInstructions"));
        receita.setFotoUrl(map.get("strMealThumb"));
        receita.setVideoUrl(map.get("strYoutube"));
        
        List<Ingrediente> ingredientes = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String name = map.get("strIngredient" + i);
            String measure = map.get("strMeasure" + i);
            if (name != null && !name.isEmpty()) {
                ingredientes.add(new Ingrediente(null, measure + " " + name));
            }
        }
        receita.setIngredientes(ingredientes);
        return receita;
    }
}
