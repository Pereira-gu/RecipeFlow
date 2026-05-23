package com.unicid.recipeflow.service;

import com.unicid.recipeflow.dto.ReceitaDTO;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.Ingrediente;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.unicid.recipeflow.model.MealResponse;
import com.unicid.recipeflow.repository.ReceitaDao;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReceitaService {

    private final ReceitaDao receitaDao;
    private final MealApiService mealApi;
    private final TranslationApiService translationApi;

    public ReceitaService(ReceitaDao receitaDao) {
        this.receitaDao = receitaDao;
        
        Retrofit retrofitMeal = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mealApi = retrofitMeal.create(MealApiService.class);

        Retrofit retrofitTranslate = new Retrofit.Builder()
                .baseUrl("https://libretranslate.de/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.translationApi = retrofitTranslate.create(TranslationApiService.class);
    }

    public interface OnExternalRecipeListener {
        void onSuccess(Receita receita);
        void onError(String message);
    }

    public void buscarReceitaExterna(OnExternalRecipeListener listener) {
        mealApi.getRandomRecipe().enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Receita receita = response.body().toReceita();
                    // O plano pede tradução aqui (Requisito 2.3)
                    traduzirReceita(receita, listener);
                } else {
                    listener.onError("Erro ao buscar receita da API");
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                listener.onError(t.getMessage());
            }
        });
    }

    private void traduzirReceita(Receita receita, OnExternalRecipeListener listener) {
        // Por simplicidade nesta fase, traduziremos o título e as instruções.
        // Nota: O LibreTranslate pode ter limites de taxa; em produção usaríamos uma chave.
        String textoParaTraduzir = receita.getTitulo() + " ||| " + receita.getPassoAPasso();
        
        translationApi.translate(new TranslationApiService.TranslationRequest(textoParaTraduzir))
                .enqueue(new Callback<TranslationApiService.TranslationResponse>() {
            @Override
            public void onResponse(Call<TranslationApiService.TranslationResponse> call, Response<TranslationApiService.TranslationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String[] partes = response.body().translatedText.split(" \\|\\|\\| ");
                    if (partes.length >= 2) {
                        receita.setTitulo(partes[0].trim());
                        receita.setPassoAPasso(partes[1].trim());
                    }
                    listener.onSuccess(receita);
                } else {
                    // Se a tradução falhar, retornamos em inglês mesmo (fallback)
                    listener.onSuccess(receita);
                }
            }

            @Override
            public void onFailure(Call<TranslationApiService.TranslationResponse> call, Throwable t) {
                listener.onSuccess(receita);
            }
        });
    }

    // Métodos anteriores mantidos...
    public ReceitaDTO paraDTO(Receita receita) {
        List<String> nomesIngredientes = new ArrayList<>();
        if (receita.getIngredientes() != null) {
            for (Ingrediente i : receita.getIngredientes()) {
                nomesIngredientes.add(i.getNome());
            }
        }

        return new ReceitaDTO(
                receita.getTitulo(),
                nomesIngredientes,
                receita.getPassoAPasso(),
                receita.getClassificacao(),
                receita.getTags()
        );
    }

    public List<Receita> filtrar(List<Receita> todas, String termo) {
        if (termo == null || termo.isEmpty()) return todas;

        String query = termo.toLowerCase();
        List<Receita> filtradas = new ArrayList<>();
        
        for (Receita r : todas) {
            boolean matchesTitle = r.getTitulo().toLowerCase().contains(query);
            boolean matchesTag = false;
            if (r.getTags() != null) {
                for (String tag : r.getTags()) {
                    if (tag.toLowerCase().contains(query)) {
                        matchesTag = true;
                        break;
                    }
                }
            }
            
            boolean matchesIngrediente = false;
            if (r.getIngredientes() != null) {
                for (Ingrediente i : r.getIngredientes()) {
                    if (i.getNome().toLowerCase().contains(query)) {
                        matchesIngrediente = true;
                        break;
                    }
                }
            }

            if (matchesTitle || matchesTag || matchesIngrediente) {
                filtradas.add(r);
            }
        }
        return filtradas;
    }

    public Receita sortearLocal(List<Receita> receitas) {
        if (receitas == null || receitas.isEmpty()) return null;
        int index = (int) (Math.random() * receitas.size());
        return receitas.get(index);
    }
}
