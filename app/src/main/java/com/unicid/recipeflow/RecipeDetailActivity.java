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
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.bumptech.glide.Glide;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.ReceitaIngredienteCrossRef;
import com.unicid.recipeflow.repository.AppDatabase;
import com.unicid.recipeflow.repository.ReceitaDao;
import com.unicid.recipeflow.service.ReceitaService;

import java.util.Locale;

public class RecipeDetailActivity extends AppCompatActivity {

    private EditText editTitle, editOrigin, editIngredients, editInstructions, editNotes, editVideoUrl;
    private RatingBar ratingBar;
    private MaterialButton btnSave, btnDelete, btnKitchenMode, btnTimer, btnRetryTranslation, btnWatchVideo;
    private ImageView imgRecipe;
    private Toolbar toolbar;
    private View cardTranslationWarning;
    
    private ReceitaDao receitaDao;
    private ReceitaService receitaService;
    private Receita currentRecipe;
    private boolean isEditMode = false;
    private boolean isUserEditing = false;
    private boolean isKitchenModeActive = false;
    private boolean isRandomRecipe = false;
    private CountDownTimer countDownTimer;

    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    try {
                        String savedPath = copyUriToInternalStorage(uri);
                        if (savedPath != null) {
                            if (currentRecipe == null) {
                                currentRecipe = new Receita();
                            }
                            currentRecipe.setFotoUrl(savedPath);
                            Glide.with(this).load(savedPath).into(imgRecipe);
                            Toast.makeText(this, "Foto atualizada!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Erro ao carregar imagem da galeria", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nestedScrollViewDetail), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
            return insets;
        });

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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_recipe_detail, menu);
        // Só mostra o lápis se estivermos visualizando uma receita existente e não estivermos já editando
        android.view.MenuItem editItem = menu.findItem(R.id.action_edit);
        if (editItem != null) {
            editItem.setVisible(isEditMode && !isUserEditing && !isKitchenModeActive);
        }

        // Mostrar reroll APENAS se for uma receita aleatória (externa) e não estivermos no modo cozinha
        android.view.MenuItem rerollItem = menu.findItem(R.id.action_reroll);
        if (rerollItem != null) {
            rerollItem.setVisible(isRandomRecipe && !isKitchenModeActive);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_edit) {
            setEditEnabled(true);
            return true;
        } else if (item.getItemId() == R.id.action_reroll) {
            rerollRecipe();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setEditEnabled(boolean enabled) {
        isUserEditing = enabled;
        
        // Transição suave
        android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) findViewById(R.id.nestedScrollViewDetail));

        editTitle.setEnabled(enabled);
        editOrigin.setEnabled(enabled);
        editIngredients.setEnabled(enabled);
        editInstructions.setEnabled(enabled);
        editNotes.setEnabled(enabled);
        editVideoUrl.setEnabled(enabled);
        editVideoUrl.setVisibility(enabled ? View.VISIBLE : View.GONE);
        ratingBar.setIsIndicator(!enabled);
        
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (isEditMode) {
            btnDelete.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
        
        if (!enabled) hideKeyboard();
        invalidateOptionsMenu();
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
        editVideoUrl = findViewById(R.id.editVideoUrl);
    }


    private void checkIntent() {
        if (getIntent().hasExtra("RECIPE_ID")) {
            long recipeId = getIntent().getLongExtra("RECIPE_ID", -1);
            loadRecipe(recipeId);
            isEditMode = true;
            isRandomRecipe = false;
            setEditEnabled(false); // Inicia em modo leitura para receitas existentes
        } else if (getIntent().hasExtra("EXTERNAL_RECIPE")) {
            currentRecipe = (Receita) getIntent().getSerializableExtra("EXTERNAL_RECIPE");
            preFillFromExternal();
            isEditMode = false;
            isRandomRecipe = true;
            setEditEnabled(true); // Nova receita externa inicia em modo edição
        } else {
            isEditMode = false;
            isRandomRecipe = false;
            setEditEnabled(true); // Novo cadastro manual inicia em modo edição
        }
    }

    private void preFillFromExternal() {
        if (currentRecipe != null) {
            editTitle.setText(currentRecipe.getTitulo());
            editInstructions.setText(currentRecipe.getPassoAPasso());
            editVideoUrl.setText(currentRecipe.getVideoUrl());

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
        if (currentRecipe != null) {
            editTitle.setText(currentRecipe.getTitulo());
            editInstructions.setText(currentRecipe.getPassoAPasso());
            editNotes.setText(currentRecipe.getNotasPessoais());
            editOrigin.setText(currentRecipe.getOrigem());
            editVideoUrl.setText(currentRecipe.getVideoUrl());
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
        btnTimer.setOnClickListener(v -> showTimerOptions());
        btnRetryTranslation.setOnClickListener(v -> retryTranslation());
        btnWatchVideo.setOnClickListener(v -> watchVideo());

        imgRecipe.setOnClickListener(v -> {
            if (isUserEditing) {
                pickImageLauncher.launch("image/*");
            }
        });

        editIngredients.addTextChangedListener(new android.text.TextWatcher() {
            private boolean isFormatting = false;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                if (isFormatting || !isUserEditing) return;
                String text = s.toString();
                if (text.endsWith("\n")) {
                    isFormatting = true;
                    s.append("• ");
                    isFormatting = false;
                } else if (text.length() > 0 && s.charAt(0) != '•' && !text.startsWith("•")) {
                     isFormatting = true;
                     s.insert(0, "• ");
                     isFormatting = false;
                }
            }
        });
    }

    private void showTimerOptions() {
        String[] options = {"1 min", "5 min", "10 min", "15 min", "30 min", "45 min", "1 hora"};
        int[] values = {1, 5, 10, 15, 30, 45, 60};
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Configurar Timer")
            .setItems(options, (dialog, which) -> startTimer(values[which] * 60 * 1000))
            .show();
    }

    private void watchVideo() {
        if (currentRecipe != null && currentRecipe.getVideoUrl() != null && !currentRecipe.getVideoUrl().isEmpty()) {
            android.net.Uri videoUri = android.net.Uri.parse(currentRecipe.getVideoUrl());
            android.content.Intent intent = new android.content.Intent(android.content.Intent.ACTION_VIEW, videoUri);
            
            // Validar se existe um app para lidar com a Intent
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nenhum aplicativo encontrado para abrir o vídeo.", Toast.LENGTH_SHORT).show();
            }
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

    private void rerollRecipe() {
        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setTitle("O Chef está inspirado!");
        progress.setMessage("Buscando uma nova surpresa para você...");
        progress.setCancelable(false);
        progress.show();

        receitaService.buscarReceitaExterna(new ReceitaService.OnExternalRecipeListener() {
            @Override
            public void onSuccess(Receita receita) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    currentRecipe = receita;
                    isEditMode = false;
                    isUserEditing = true;
                    isRandomRecipe = true;
                    preFillFromExternal();
                    setEditEnabled(true); 
                    Toast.makeText(RecipeDetailActivity.this, "Prontinho! Uma nova ideia para você. ✨", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    progress.dismiss();
                    String userFriendlyMessage = "Erro ao buscar nova receita: " + message;
                    if (message != null && (message.toLowerCase().contains("unable to resolve host") || 
                        message.toLowerCase().contains("timeout") || 
                        message.toLowerCase().contains("network"))) {
                        userFriendlyMessage = "Sem conexão com a internet. Verifique seu sinal e tente novamente! 📶";
                    }
                    Snackbar.make(findViewById(R.id.nestedScrollViewDetail), userFriendlyMessage, Snackbar.LENGTH_LONG).show();
                });
            }
        });
    }

    private void toggleKitchenMode() {
        isKitchenModeActive = !isKitchenModeActive;
        // Transição suave para o modo cozinha
        android.transition.TransitionManager.beginDelayedTransition((android.view.ViewGroup) findViewById(R.id.nestedScrollViewDetail));
        
        if (isKitchenModeActive) {
            hideKeyboard();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            editInstructions.setTextSize(22);
            editIngredients.setTextSize(20);
            
            // Foco Visual Absoluto: Ocultar o que não é essencial
            editOrigin.setVisibility(View.GONE);
            editNotes.setVisibility(View.GONE);
            ratingBar.setVisibility(View.GONE);
            btnSave.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            
            // Desativar edição
            editInstructions.setEnabled(false);
            editIngredients.setEnabled(false);
            editTitle.setEnabled(false);
            
            btnKitchenMode.setText("Sair");
            Toast.makeText(this, "Modo Cozinha: Foco Total", Toast.LENGTH_SHORT).show();
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            editInstructions.setTextSize(16);
            editIngredients.setTextSize(16);
            
            editOrigin.setVisibility(View.VISIBLE);
            editNotes.setVisibility(View.VISIBLE);
            ratingBar.setVisibility(View.VISIBLE);
            
            // Restaurar estado de edição anterior
            setEditEnabled(isUserEditing);
            
            btnKitchenMode.setText("Cozinhar");
        }
        invalidateOptionsMenu();
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
        String videoUrl = editVideoUrl.getText().toString().trim();


        if (title.isEmpty()) {
            editTitle.setError("O título é obrigatório");
            return;
        }

        btnSave.setEnabled(false); // Evitar cliques duplos
        hideKeyboard();

        if (currentRecipe == null) {
            currentRecipe = new Receita();
        }

        currentRecipe.setTitulo(title);
        currentRecipe.setOrigem(origin);
        currentRecipe.setPassoAPasso(instructions);
        currentRecipe.setNotasPessoais(editNotes.getText().toString());
        currentRecipe.setClassificacao((int) ratingBar.getRating());
        currentRecipe.setVideoUrl(videoUrl);

        new Thread(() -> {
            try {
                if (isEditMode) {
                    receitaDao.updateReceita(currentRecipe);
                    receitaDao.deleteIngredientsForRecipe(currentRecipe.getId());
                    saveIngredients(currentRecipe.getId(), ingredientsStr);
                    runOnUiThread(() -> Toast.makeText(this, "Alterações salvas! 📝", Toast.LENGTH_SHORT).show());
                } else {
                    long id = receitaDao.insertReceita(currentRecipe);
                    currentRecipe.setId(id);
                    saveIngredients(id, ingredientsStr);
                    runOnUiThread(() -> Toast.makeText(this, "Salvo no seu Diário! 📖✨", Toast.LENGTH_SHORT).show());
                }
                runOnUiThread(this::finish);
            } catch (Exception e) {
                runOnUiThread(() -> {
                    btnSave.setEnabled(true);
                    Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
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
            Toast.makeText(this, "Receita removida do diário.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String copyUriToInternalStorage(android.net.Uri uri) {
        try {
            String fileName = "recipe_image_" + System.currentTimeMillis() + ".jpg";
            java.io.File file = new java.io.File(getFilesDir(), fileName);
            try (java.io.InputStream inputStream = getContentResolver().openInputStream(uri);
                 java.io.OutputStream outputStream = new java.io.FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                return file.getAbsolutePath();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
