package com.unicid.recipeflow.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.unicid.recipeflow.R;
import com.unicid.recipeflow.model.Receita;

import java.util.List;

public class RecipeAdapter extends BaseAdapter {

    private Context context;
    private List<Receita> recipes;

    public RecipeAdapter(Context context, List<Receita> recipes) {
        this.context = context;
        this.recipes = recipes;
    }

    @Override
    public int getCount() {
        return recipes.size();
    }

    @Override
    public Object getItem(int position) {
        return recipes.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.recipe_item, parent, false);
        }

        Receita recipe = recipes.get(position);

        TextView txtTitle = convertView.findViewById(R.id.txtRecipeTitle);
        RatingBar ratingBar = convertView.findViewById(R.id.ratingRecipe);
        ImageView imgThumbnail = convertView.findViewById(R.id.imgRecipeThumbnail);

        txtTitle.setText(recipe.getTitulo());
        ratingBar.setRating(recipe.getClassificacao());
        
        // Note: For actual image loading, we'd use Glide or Picasso later.
        // For now, it uses the placeholder set in XML.

        return convertView;
    }

    public void updateList(List<Receita> newList) {
        this.recipes = newList;
        notifyDataSetChanged();
    }
}
