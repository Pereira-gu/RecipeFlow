package com.unicid.recipeflow.repository;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.unicid.recipeflow.model.Receita;
import com.unicid.recipeflow.model.Ingrediente;
import com.unicid.recipeflow.model.ReceitaIngredienteCrossRef;

@Database(entities = {Receita.class, Ingrediente.class, ReceitaIngredienteCrossRef.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    public abstract ReceitaDao receitaDao();

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "recipe_flow_database")
                            .fallbackToDestructiveMigration()
                            .allowMainThreadQueries() // Nota: Em produção, usar threads separadas ou LiveData
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
