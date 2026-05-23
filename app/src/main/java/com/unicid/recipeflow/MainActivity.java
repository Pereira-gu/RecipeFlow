package com.unicid.recipeflow;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.unicid.recipeflow.adapter.RecipeAdapter;
import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.repository.AppDatabase;
import com.unicid.recipeflow.repository.ReceitaDao;
import com.unicid.recipeflow.service.ReceitaService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView listViewRecipes;
    private RecipeAdapter adapter;
    private List<Receita> recipeList;
    private SearchView searchView;
    private FloatingActionButton fabAdd;
    
    private ReceitaDao receitaDao;
    private ReceitaService receitaService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializa Banco e Serviço
        receitaDao = AppDatabase.getDatabase(this).receitaDao();

        receitaService = new ReceitaService(receitaDao);

        initializeViews();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRecipes(); // Recarrega a lista ao voltar de outras telas
    }

    private void initializeViews() {
        listViewRecipes = findViewById(R.id.listViewRecipes);
        searchView = findViewById(R.id.searchViewRecipes);
        fabAdd = findViewById(R.id.fabAddRecipe);
        
        recipeList = new ArrayList<>();
        adapter = new RecipeAdapter(this, recipeList);
        listViewRecipes.setAdapter(adapter);
    }

    private void loadRecipes() {
        recipeList = receitaDao.getAllActiveRecipes();
        adapter.updateList(recipeList);
    }

    private void setupListeners() {
        // Abrir detalhes para editar
        listViewRecipes.setOnItemClickListener((parent, view, position, id) -> {
            Receita selected = (Receita) adapter.getItem(position);
            Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
            intent.putExtra("RECIPE_ID", selected.getId());
            startActivity(intent);
        });

        // Abrir tela para novo cadastro (Requisito 2.1)
        fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, RecipeDetailActivity.class));
        });

        // Busca em tempo real (Requisito 2.2)
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                List<Receita> filtered = receitaService.filtrar(recipeList, newText);
                adapter.updateList(filtered);
                return true;
            }
        });

        // Sorteio Local (Requisito 2.3)
        findViewById(R.id.btnSorteioLocal).setOnClickListener(v -> {
            Receita sorteada = receitaService.sortearLocal(recipeList);
            if (sorteada != null) {
                Toast.makeText(this, "Sugestão: " + sorteada.getTitulo(), Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, RecipeDetailActivity.class);
                intent.putExtra("RECIPE_ID", sorteada.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Nenhuma receita salva para sortear.", Toast.LENGTH_SHORT).show();
            }
        });

        // Descoberta Externa + Tradução (Requisito 2.3)
        findViewById(R.id.btnSorteioExterno).setOnClickListener(v -> {
            android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
            progress.setTitle("Buscando Receita");
            progress.setMessage("Consultando o Chef Internacional e traduzindo...");
            progress.setCancelable(false);
            progress.show();

            receitaService.buscarReceitaExterna(new ReceitaService.OnExternalRecipeListener() {
                @Override
                public void onSuccess(Receita receita) {
                    runOnUiThread(() -> {
                        progress.dismiss();
                        Intent intent = new Intent(MainActivity.this, RecipeDetailActivity.class);
                        intent.putExtra("EXTERNAL_RECIPE", receita);
                        startActivity(intent);
                    });
                }

                @Override
                public void onError(String message) {
                    runOnUiThread(() -> {
                        progress.dismiss();
                        new androidx.appcompat.app.AlertDialog.Builder(MainActivity.this)
                            .setTitle("Ops! Algo deu errado")
                            .setMessage("Não conseguimos obter ou traduzir a receita agora: " + message + "\nDeseja tentar novamente?")
                            .setPositiveButton("Sim", (d, w) -> v.performClick())
                            .setNegativeButton("Agora não", null)
                            .show();
                    });
                }
            });
        });
    }
}