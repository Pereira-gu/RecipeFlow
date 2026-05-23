package com.unicid.recipeflow;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.ReceitaIngredienteCrossRef;
import com.unicid.recipeflow.repository.AppDatabase;
import com.unicid.recipeflow.repository.ReceitaDao;
import com.unicid.recipeflow.service.ReceitaService;

import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private EditText editTitle, editOrigin, editIngredients, editInstructions, editNotes;
    private RatingBar ratingBar;
    private MaterialButton btnSave, btnDelete, btnKitchenMode, btnTimer, btnRetryTranslation, btnWatchVideo;
    private ImageView imgRecipe;
    private Toolbar toolbar;
    private View cardTranslationWarning;
    
    private ReceitaDao receitaDao;
    private ReceitaService receitaService;
    private Receita currentRecipe;
    private boolean isEditMode = false;
    private boolean isKitchenModeActive = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        receitaDao = AppDatabase.getDatabase(this).receitaDao();
        receitaService = new ReceitaService(receitaDao);

        initializeViews();
        setupToolbar();
        checkIntent();
        setupListeners();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initializeViews() {
        editTitle = findViewById(R.id.editTitle);
        editIngredients = findViewById(R.id.editIngredients);
        editInstructions = findViewById(R.id.editInstructions);
        editNotes = findViewById(R.id.editNotes);
        ratingBar = findViewById(R.id.ratingDetail);
        btnSave = findViewById(R.id.btnSave);
        btnDelete = findViewById(R.id.btnDelete);
        btnKitchenMode = findViewById(R.id.btnKitchenMode);
        btnTimer = findViewById(R.id.btnTimer);
        btnRetryTranslation = findViewById(R.id.btnRetryTranslation);
        btnWatchVideo = findViewById(R.id.btnWatchVideo);
        cardTranslationWarning = findViewById(R.id.cardTranslationWarning);
        imgRecipe = findViewById(R.id.imgRecipeDetail);
        toolbar = findViewById(R.id.toolbar);
        editOrigin = findViewById(R.id.editOrigin);
    }

    private void checkIntent() {
        if (getIntent().hasExtra("RECIPE_ID")) {
            long recipeId = getIntent().getLongExtra("RECIPE_ID", -1);
            loadRecipe(recipeId);
            isEditMode = true;
            btnDelete.setVisibility(View.VISIBLE);
        } else if (getIntent().hasExtra("EXTERNAL_RECIPE")) {
            currentRecipe = (Receita) getIntent().getSerializableExtra("EXTERNAL_RECIPE");
            preFillFromExternal();
        }
    }

    private void preFillFromExternal() {
        if (currentRecipe != null) {
            editTitle.setText(currentRecipe.getTitulo());
            editInstructions.setText(currentRecipe.getPassoAPasso());
            editTitle.setText(currentRecipe.getTitulo());

            if (currentRecipe.getOrigem() != null) {
                editOrigin.setText(currentRecipe.getOrigem());
            }

            if (currentRecipe.getFotoUrl() != null && !currentRecipe.getFotoUrl().isEmpty()) {
                Glide.with(this).load(currentRecipe.getFotoUrl()).into(imgRecipe);
            }

            if (currentRecipe.getVideoUrl() != null && !currentRecipe.getVideoUrl().isEmpty()) {
                btnWatchVideo.setVisibility(View.VISIBLE);
            } else {
                btnWatchVideo.setVisibility(View.GONE);
            }
            
            StringBuilder sb = new StringBuilder();
            if (currentRecipe.getIngredientes() != null) {
                for (Ingrediente ing : currentRecipe.getIngredientes()) {
                    sb.append("• ").append(ing.getNome()).append("\n");
                }
            }
            editIngredients.setText(sb.toString());
            
            if (currentRecipe.isTraducaoFalhou()) {
                cardTranslationWarning.setVisibility(View.VISIBLE);
            } else {
                cardTranslationWarning.setVisibility(View.GONE);
                Toast.makeText(this, "Receita importada e traduzida!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadRecipe(long id) {
        currentRecipe = receitaDao.getRecipeById(id);
        editTitle.setText(currentRecipe.getTitulo());

        if (currentRecipe.getOrigem() != null) {
            editOrigin.setText(currentRecipe.getOrigem());
        }

        if (currentRecipe != null) {
            editTitle.setText(currentRecipe.getTitulo());
            editInstructions.setText(currentRecipe.getPassoAPasso());
            editNotes.setText(currentRecipe.getNotasPessoais());
            ratingBar.setRating(currentRecipe.getClassificacao());
            
            if (currentRecipe.getFotoUrl() != null && !currentRecipe.getFotoUrl().isEmpty()) {
                Glide.with(this).load(currentRecipe.getFotoUrl()).into(imgRecipe);
            }

            if (currentRecipe.getVideoUrl() != null && !currentRecipe.getVideoUrl().isEmpty()) {
                btnWatchVideo.setVisibility(View.VISIBLE);
            } else {
                btnWatchVideo.setVisibility(View.GONE);
            }

            // Carregar ingredientes do banco
            new Thread(() -> {
                java.util.List<Ingrediente> ingredients = receitaDao.getIngredientsForRecipe(id);
                runOnUiThread(() -> {
                    StringBuilder sb = new StringBuilder();
                    for (Ingrediente ing : ingredients) {
                        sb.append("• ").append(ing.getNome()).append("\n");
                    }
                    editIngredients.setText(sb.toString());
                });
            }).start();
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveRecipe());
        btnDelete.setOnClickListener(v -> deleteRecipe());
        btnKitchenMode.setOnClickListener(v -> toggleKitchenMode());
        btnTimer.setOnClickListener(v -> startTimer(5 * 60 * 1000));
        btnRetryTranslation.setOnClickListener(v -> retryTranslation());
        btnWatchVideo.setOnClickListener(v -> watchVideo());
    }

    private void watchVideo() {
        if (currentRecipe != null && currentRecipe.getVideoUrl() != null && !currentRecipe.getVideoUrl().isEmpty()) {
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(currentRecipe.getVideoUrl()));
            startActivity(intent);
        }
    }

    private void retryTranslation() {
        if (currentRecipe != null) {
            Toast.makeText(this, "Tentando traduzir novamente...", Toast.LENGTH_SHORT).show();
            receitaService.retraduzirReceita(currentRecipe, new ReceitaService.OnExternalRecipeListener() {
                @Override
                public void onSuccess(Receita receita) {
                    currentRecipe = receita;
                    runOnUiThread(() -> {
                        preFillFromExternal();
                        if (!currentRecipe.isTraducaoFalhou()) {
                            Toast.makeText(RecipeDetailActivity.this, "Tradução concluída!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> 
                        Toast.makeText(RecipeDetailActivity.this, "Erro: " + message, Toast.LENGTH_SHORT).show()
                    );
                }
            });
        }
    }

    private void toggleKitchenMode() {
        isKitchenModeActive = !isKitchenModeActive;
        if (isKitchenModeActive) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            editInstructions.setTextSize(22);
            editIngredients.setTextSize(20);
            btnKitchenMode.setText("Sair");
            Toast.makeText(this, "Modo Cozinha: Tela sempre ligada", Toast.LENGTH_SHORT).show();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            editInstructions.setTextSize(16);
            editIngredients.setTextSize(16);
            btnKitchenMode.setText("Cozinhar");
        }
    }

    private void startTimer(long millis) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long minutes = (millisUntilFinished / 1000) / 60;
                long seconds = (millisUntilFinished / 1000) % 60;
                btnTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                btnTimer.setText("Timer");
                Toast.makeText(RecipeDetailActivity.this, "Tempo esgotado!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void saveRecipe() {
        String title = editTitle.getText().toString().trim();
        String origin = editOrigin.getText().toString().trim();
        String instructions = editInstructions.getText().toString().trim();
        String ingredientsStr = editIngredients.getText().toString().trim();


        if (title.isEmpty()) {
            editTitle.setError("O título é obrigatório");
            return;
        }

        if (currentRecipe == null) {
            currentRecipe = new Receita();
        }

        currentRecipe.setTitulo(title);
        currentRecipe.setOrigem(origin);
        currentRecipe.setPassoAPasso(instructions);
        currentRecipe.setNotasPessoais(editNotes.getText().toString());
        currentRecipe.setClassificacao((int) ratingBar.getRating());

        new Thread(() -> {
            if (isEditMode) {
                receitaDao.updateReceita(currentRecipe);
                // Atualizar ingredientes: remove os antigos e insere os novos
                receitaDao.deleteIngredientsForRecipe(currentRecipe.getId());
                saveIngredients(currentRecipe.getId(), ingredientsStr);
                runOnUiThread(() -> Toast.makeText(this, "Atualizado!", Toast.LENGTH_SHORT).show());
            } else {
                long id = receitaDao.insertReceita(currentRecipe);
                currentRecipe.setId(id);
                saveIngredients(id, ingredientsStr);
                runOnUiThread(() -> Toast.makeText(this, "Adicionado ao Diário!", Toast.LENGTH_SHORT).show());
            }
            runOnUiThread(this::finish);
        }).start();
    }

    private void saveIngredients(long recipeId, String ingredientsStr) {
        if (ingredientsStr.isEmpty()) return;

        String[] lines = ingredientsStr.split("\n");
        for (String line : lines) {
            String cleanLine = line.replace("•", "").trim();
            if (!cleanLine.isEmpty()) {
                Ingrediente ing = new Ingrediente(null, cleanLine);
                long ingId = receitaDao.insertIngrediente(ing);
                receitaDao.insertReceitaIngredienteCrossRef(new ReceitaIngredienteCrossRef(recipeId, ingId));
            }
        }
    }

    private void deleteRecipe() {
        if (currentRecipe != null && currentRecipe.getId() != null) {
            receitaDao.softDelete(currentRecipe.getId());
            Toast.makeText(this, "Excluído", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
