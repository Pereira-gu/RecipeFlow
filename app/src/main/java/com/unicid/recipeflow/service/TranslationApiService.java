package com.unicid.recipeflow.service;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface TranslationApiService {
    // Formato: api/v1/:source/:target/:query
    @GET("api/v1/{source}/{target}/{query}")
    Call<TranslationResponse> translate(
        @Path("source") String source,
        @Path("target") String target,
        @Path("query") String query
    );

    class TranslationResponse {
        public String translation; // O Lingva retorna o campo "translation"
    }
}
