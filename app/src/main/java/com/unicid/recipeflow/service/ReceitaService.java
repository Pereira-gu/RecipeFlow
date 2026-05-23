package com.unicid.recipeflow.service;

import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.unicid.recipeflow.dto.ReceitaDTO;
import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.MealResponse;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.repository.ReceitaDao;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ReceitaService {

    private final ReceitaDao receitaDao;
    private final MealApiService mealApi;

    public ReceitaService(ReceitaDao receitaDao) {
        this.receitaDao = receitaDao;

        // Mantemos apenas a API de receitas (TheMealDB)
        Retrofit retrofitMeal = new Retrofit.Builder()
                .baseUrl("https://www.themealdb.com/api/json/v1/1/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        this.mealApi = retrofitMeal.create(MealApiService.class);
    }

    public interface OnExternalRecipeListener {
        void onSuccess(Receita receita);
        void onError(String message);
    }

    public void retraduzirReceita(Receita receita, OnExternalRecipeListener listener) {
        traduzirReceitaCompleta(receita, listener);
    }

    public void buscarReceitaExterna(OnExternalRecipeListener listener) {
        mealApi.getRandomRecipe().enqueue(new Callback<MealResponse>() {
            @Override
            public void onResponse(Call<MealResponse> call, Response<MealResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Receita receita = response.body().toReceita();
                    if (receita != null) {
                        traduzirReceitaCompleta(receita, listener);
                    } else {
                        listener.onError("Receita não encontrada.");
                    }
                } else {
                    listener.onError("Erro ao buscar receita da API: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<MealResponse> call, Throwable t) {
                listener.onError("Falha na rede: " + t.getMessage());
            }
        });
    }

    private void traduzirReceitaCompleta(Receita receita, OnExternalRecipeListener listener) {
        // Configura o tradutor de Inglês para Português
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.PORTUGUESE)
                .build();

        Translator englishPortugueseTranslator = Translation.getClient(options);

        // Condições para baixar o modelo de idioma (aprox. 30MB).
        DownloadConditions conditions = new DownloadConditions.Builder().build();

        englishPortugueseTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(v -> realizarTraducaoEmCadeia(receita, englishPortugueseTranslator, listener))
                .addOnFailureListener(e -> {
                    receita.setTraducaoFalhou(true);
                    listener.onError("Erro ao baixar pacote de idioma: " + e.getMessage());
                });
    }

    private void realizarTraducaoEmCadeia(Receita receita, Translator translator, OnExternalRecipeListener listener) {
        // 1. Traduzir o Título
        translator.translate(receita.getTitulo())
                .addOnSuccessListener(tituloPt -> {
                    receita.setTitulo(tituloPt);

                    if (receita.getOrigem() != null && !receita.getOrigem().isEmpty()) {
                        translator.translate(receita.getOrigem()).addOnSuccessListener(origemPt -> {
                            receita.setOrigem(origemPt);
                        });
                    }

                    // 2. Traduzir o Passo a Passo
                    translator.translate(receita.getPassoAPasso())
                            .addOnSuccessListener(passoPt -> {
                                // SOLUÇÃO PARA O TEXTO GRUDADO:
                                // Procura pontos finais seguidos de espaço e os transforma em quebra de parágrafo dupla.
                                String passoFormatado = passoPt.replaceAll("\\.\\s+", ".\n\n");
                                receita.setPassoAPasso(passoFormatado);

                                // 3. Traduzir os Ingredientes
                                traduzirIngredientes(receita, translator, listener);
                            })
                            .addOnFailureListener(e -> falharTraducao(receita, listener));
                })
                .addOnFailureListener(e -> falharTraducao(receita, listener));
    }

    private void traduzirIngredientes(Receita receita, Translator translator, OnExternalRecipeListener listener) {
        if (receita.getIngredientes() == null || receita.getIngredientes().isEmpty()) {
            receita.setTraducaoFalhou(false);
            listener.onSuccess(receita);
            return;
        }

        // SOLUÇÃO PARA OS INGREDIENTES:
        // Criamos uma lista de "Tarefas" (Tasks) para traduzir cada ingrediente separadamente
        List<Task<String>> tarefasDeTraducao = new ArrayList<>();

        for (Ingrediente ing : receita.getIngredientes()) {
            tarefasDeTraducao.add(translator.translate(ing.getNome()));
        }

        // O Tasks.whenAllSuccess aguarda TODAS as traduções terminarem em paralelo
        Tasks.whenAllSuccess(tarefasDeTraducao)
                .addOnSuccessListener(listaTraduzida -> {
                    // Atualiza cada ingrediente com sua respectiva tradução
                    for (int i = 0; i < listaTraduzida.size(); i++) {
                        // O retorno genérico da Task é Object, então fazemos o cast para String
                        String nomeTraduzido = (String) listaTraduzida.get(i);
                        // Opcional: Colocar a primeira letra em maiúscula para ficar bonito
                        if (nomeTraduzido != null && !nomeTraduzido.isEmpty()) {
                            nomeTraduzido = nomeTraduzido.substring(0, 1).toUpperCase() + nomeTraduzido.substring(1);
                        }
                        receita.getIngredientes().get(i).setNome(nomeTraduzido);
                    }

                    receita.setTraducaoFalhou(false);
                    listener.onSuccess(receita);
                })
                .addOnFailureListener(e -> falharTraducao(receita, listener));
    }

    private void falharTraducao(Receita receita, OnExternalRecipeListener listener) {
        receita.setTraducaoFalhou(true);
        // Retornamos onSuccess com a receita preenchida em inglês, para o utilizador não ficar de mãos vazias
        listener.onSuccess(receita);
    }

    // --- MÉTODOS EXISTENTES MANTIDOS INTACTOS ---
    public ReceitaDTO paraDTO(Receita receita) {
        List<String> nomesIngredientes = new ArrayList<>();
        if (receita.getIngredientes() != null) {
            for (Ingrediente i : receita.getIngredientes()) {
                nomesIngredientes.add(i.getNome());
            }
        }
        return new ReceitaDTO(receita.getTitulo(), nomesIngredientes, receita.getPassoAPasso(), receita.getClassificacao(), receita.getTags());
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
                        matchesTag = true; break;
                    }
                }
            }

            boolean matchesIngrediente = false;
            if (r.getIngredientes() != null) {
                for (Ingrediente i : r.getIngredientes()) {
                    if (i.getNome().toLowerCase().contains(query)) {
                        matchesIngrediente = true; break;
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