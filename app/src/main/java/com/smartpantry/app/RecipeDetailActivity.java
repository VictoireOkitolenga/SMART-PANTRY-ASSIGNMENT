package com.smartpantry.app;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.smartpantry.app.database.DatabaseHelper;
import com.smartpantry.app.logic.StrictMatcher;
import com.smartpantry.app.models.PantryItem;
import com.smartpantry.app.models.Recipe;
import com.smartpantry.app.models.RecipeIngredient;
import java.util.List;

/**
 * Recipe Detail Screen showing full ingredient checklist and preparation method.
 */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "com.smartpantry.app.EXTRA_RECIPE_ID";

    private TextView tvTitle, tvDescription, tvMeta, tvIngredients, tvInstructions;
    private Button btnCook;
    private DatabaseHelper dbHelper;
    private Recipe currentRecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);

        tvTitle = findViewById(R.id.tv_detail_title);
        tvDescription = findViewById(R.id.tv_detail_description);
        tvMeta = findViewById(R.id.tv_detail_meta);
        tvIngredients = findViewById(R.id.tv_detail_ingredients);
        tvInstructions = findViewById(R.id.tv_detail_instructions);
        btnCook = findViewById(R.id.btn_cook_recipe);

        String recipeId = getIntent().getStringExtra(EXTRA_RECIPE_ID);
        if (recipeId != null) {
            currentRecipe = dbHelper.getRecipeById(recipeId);
            displayRecipeDetails();
        }

        btnCook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cookRecipe();
            }
        });
    }

    private void displayRecipeDetails() {
        if (currentRecipe == null) return;

        setTitle(currentRecipe.getName());
        tvTitle.setText(currentRecipe.getName());
        tvDescription.setText(currentRecipe.getDescription());
        tvMeta.setText(String.format("Prep: %d min · Cook: %d min · Serves: %d · %s",
                currentRecipe.getPrepTimeMinutes(),
                currentRecipe.getCookTimeMinutes(),
                currentRecipe.getServings(),
                currentRecipe.getDifficulty()));

        List<PantryItem> pantry = dbHelper.getAllPantryItems();
        StrictMatcher.EvaluationResult eval = StrictMatcher.evaluate(currentRecipe, pantry);

        StringBuilder sbIng = new StringBuilder();
        for (RecipeIngredient req : currentRecipe.getIngredients()) {
            boolean available = eval.isIngredientAvailable(req.getName());
            String mark = available ? "✓ [In Pantry]" : "✗ [Missing]";
            sbIng.append(String.format("• %s %.1f %s %s\n", req.getName(), req.getQuantity(), req.getUnit(), mark));
        }
        tvIngredients.setText(sbIng.toString());

        StringBuilder sbInst = new StringBuilder();
        for (int i = 0; i < currentRecipe.getInstructions().size(); i++) {
            sbInst.append(String.format("%d. %s\n\n", i + 1, currentRecipe.getInstructions().get(i)));
        }
        tvInstructions.setText(sbInst.toString());

        btnCook.setEnabled(eval.isStrictMatch());
        if (!eval.isStrictMatch()) {
            btnCook.setText("Cannot Cook (Missing Ingredients)");
        } else {
            btnCook.setText("I Cooked This! (Deduct Ingredients)");
        }
    }

    private void cookRecipe() {
        dbHelper.deductIngredientsForRecipe(currentRecipe);
        Toast.makeText(this, "Pantry quantities updated!", Toast.LENGTH_LONG).show();
        finish();
    }
}
