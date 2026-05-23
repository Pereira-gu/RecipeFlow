package com.unicid.recipeflow;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.ReceitaIngredienteCrossRef;
import com.unicid.recipeflow.repository.AppDatabase;
import com.unicid.recipeflow.repository.ReceitaDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private EditText editTitle, editIngredients, editInstructions, editNotes;
    private RatingBar ratingBar;
    private Button btnSave, btnDelete, btnKitchenMode, btnTimer;
    
    private ReceitaDao receitaDao;
    private Receita currentRecipe;
    private boolean isEditMode = false;
    private boolean isKitchenModeActive = false;
    private CountDownTimer countDownTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        receitaDao = AppDatabase.getDatabase(this).receitaDao();

        initializeViews();
        checkIntent();
        setupListeners();
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
            
            StringBuilder sb = new StringBuilder();
            if (currentRecipe.getIngredientes() != null) {
                for (Ingrediente ing : currentRecipe.getIngredientes()) {
                    sb.append(ing.getNome()).append("\n");
                }
            }
            editIngredients.setText(sb.toString());
            Toast.makeText(this, "Receita importada! Clique em Salvar para guardar no diário.", Toast.LENGTH_LONG).show();
        }
    }

    private void loadRecipe(long id) {
        currentRecipe = receitaDao.getRecipeById(id);
        if (currentRecipe != null) {
            editTitle.setText(currentRecipe.getTitulo());
            editInstructions.setText(currentRecipe.getPassoAPasso());
            editNotes.setText(currentRecipe.getNotasPessoais());
            ratingBar.setRating(currentRecipe.getClassificacao());
            
            // Em uma implementação real, buscaríamos os ingredientes da tabela cruzada
            // Para este exemplo, deixaremos o campo de texto para o usuário
        }
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveRecipe());
        btnDelete.setOnClickListener(v -> deleteRecipe());
        btnKitchenMode.setOnClickListener(v -> toggleKitchenMode());
        btnTimer.setOnClickListener(v -> startTimer(5 * 60 * 1000)); // 5 minutos padrão
    }

    private void toggleKitchenMode() {
        isKitchenModeActive = !isKitchenModeActive;
        if (isKitchenModeActive) {
            // Mantém a tela ligada
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            // Aumenta o tamanho das fontes (UX - Requisito 2.4)
            editInstructions.setTextSize(24);
            editIngredients.setTextSize(22);
            btnKitchenMode.setText("Sair do Modo Cozinha");
            Toast.makeText(this, "Modo Cozinha Ativado: Tela ligada e fonte maior.", Toast.LENGTH_SHORT).show();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            editInstructions.setTextSize(18);
            editIngredients.setTextSize(18);
            btnKitchenMode.setText("Modo Cozinha");
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
                btnTimer.setText("Timer Pronto!");
                Toast.makeText(RecipeDetailActivity.this, "Tempo esgotado!", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void saveRecipe() {
        String title = editTitle.getText().toString().trim();
        String instructions = editInstructions.getText().toString().trim();
        String ingredientsStr = editIngredients.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "O título é obrigatório", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentRecipe == null) {
            currentRecipe = new Receita();
        }

        currentRecipe.setTitulo(title);
        currentRecipe.setPassoAPasso(instructions);
        currentRecipe.setNotasPessoais(editNotes.getText().toString());
        currentRecipe.setClassificacao((int) ratingBar.getRating());

        if (isEditMode) {
            receitaDao.updateReceita(currentRecipe);
            Toast.makeText(this, "Receita atualizada!", Toast.LENGTH_SHORT).show();
        } else {
            long id = receitaDao.insertReceita(currentRecipe);
            currentRecipe.setId(id);
            saveIngredients(id, ingredientsStr);
            Toast.makeText(this, "Receita salva!", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void saveIngredients(long recipeId, String ingredientsStr) {
        if (ingredientsStr.isEmpty()) return;

        String[] lines = ingredientsStr.split("\n");
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                Ingrediente ing = new Ingrediente(null, line.trim());
                long ingId = receitaDao.insertIngrediente(ing);
                receitaDao.insertReceitaIngredienteCrossRef(new ReceitaIngredienteCrossRef(recipeId, ingId));
            }
        }
    }

    private void deleteRecipe() {
        if (currentRecipe != null && currentRecipe.getId() != null) {
            receitaDao.softDelete(currentRecipe.getId());
            Toast.makeText(this, "Receita movida para a lixeira", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
