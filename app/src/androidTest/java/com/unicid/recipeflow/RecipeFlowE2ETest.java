package com.unicid.recipeflow;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.repository.AppDatabase;
import com.unicid.recipeflow.repository.ReceitaDao;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class RecipeFlowE2ETest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private ReceitaDao receitaDao;

    @Before
    public void setup() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        receitaDao = AppDatabase.getDatabase(context).receitaDao();
    }

    @Test
    public void testSorteioEdicaoESalvamento() throws InterruptedException {
        // 1. Clica em "Explorar Sabores" (Sorteio Externo)
        onView(withId(R.id.btnSorteioExterno)).perform(click());

        // Aguarda a resposta da API e Tradução (Simulado por delay simples para o teste)
        Thread.sleep(5000);

        // 2. Verifica se abriu a tela de detalhes com a receita sorteada
        onView(withId(R.id.editTitle)).check(matches(isDisplayed()));

        // 3. Edita o título para garantir a personalização
        String uniqueTitle = "Receita Teste " + System.currentTimeMillis();
        onView(withId(R.id.editTitle)).perform(replaceText(uniqueTitle));

        // 4. Salva a receita
        onView(withId(R.id.btnSave)).perform(click());

        // 5. Verifica no banco de dados se a receita foi persistida corretamente
        List<Receita> receitas = receitaDao.getAllActiveRecipes();
        boolean found = false;
        for (Receita r : receitas) {
            if (r.getTitulo().equals(uniqueTitle)) {
                found = true;
                break;
            }
        }
        assertTrue("A receita sorteada e editada deveria estar no banco de dados", found);
    }
}
