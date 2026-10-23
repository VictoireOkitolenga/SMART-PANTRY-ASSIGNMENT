package com.smartpantry.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.smartpantry.app.R;
import com.smartpantry.app.models.Recipe;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    public interface OnRecipeClickListener {
        void onRecipeClick(Recipe recipe);
    }

    private final Context context;
    private List<Recipe> recipes;
    private final OnRecipeClickListener listener;
    private boolean isAlmostThereTab = false;

    public RecipeAdapter(Context context, List<Recipe> recipes, OnRecipeClickListener listener) {
        this.context = context;
        this.recipes = recipes;
        this.listener = listener;
    }

    public void updateList(List<Recipe> newRecipes, boolean isAlmostThereTab) {
        this.recipes = newRecipes;
        this.isAlmostThereTab = isAlmostThereTab;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Recipe recipe = recipes.get(position);
        holder.tvName.setText(recipe.getName());
        holder.tvDescription.setText(recipe.getDescription());

        int totalMinutes = recipe.getPrepTimeMinutes() + recipe.getCookTimeMinutes();
        holder.tvMeta.setText(String.format("%d mins  ·  %s  ·  %s",
                totalMinutes, recipe.getDifficulty(), recipe.getCategory()));

        if (isAlmostThereTab) {
            holder.tvBadge.setText("Missing 1");
            holder.tvIngredientsStatus.setText(String.format(
                    "Need %d ingredients (Missing 1)", recipe.getIngredients().size()));
        } else {
            holder.tvBadge.setText("Strict Match");
            holder.tvIngredientsStatus.setText(String.format(
                    "All %d ingredients available in pantry", recipe.getIngredients().size()));
        }

        holder.itemView.setOnClickListener(v -> listener.onRecipeClick(recipe));
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvBadge, tvDescription, tvMeta, tvIngredientsStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_recipe_name);
            tvBadge = itemView.findViewById(R.id.tv_recipe_badge);
            tvDescription = itemView.findViewById(R.id.tv_recipe_description);
            tvMeta = itemView.findViewById(R.id.tv_recipe_meta);
            tvIngredientsStatus = itemView.findViewById(R.id.tv_ingredients_status);
        }
    }
}