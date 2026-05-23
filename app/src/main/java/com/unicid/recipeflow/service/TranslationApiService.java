package com.unicid.recipeflow.service;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TranslationApiService {
    @POST("translate")
    Call<TranslationResponse> translate(@Body TranslationRequest request);

    class TranslationRequest {
        public String q;
        public String source = "en";
        public String target = "pt";
        public String format = "text";

        public TranslationRequest(String q) {
            this.q = q;
        }
    }

    class TranslationResponse {
        public String translatedText;
    }
}
