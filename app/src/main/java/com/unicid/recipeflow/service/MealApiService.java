package com.unicid.recipeflow.service;

import com.unicid.recipeflow.model.MealResponse;

import retrofit2.Call;
import retrofit2.http.GET;

public interface MealApiService {
    @GET("random.php")
    Call<MealResponse> getRandomRecipe();
}
