package com.smartpantry.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.smartpantry.app.models.PantryItem;
import com.smartpantry.app.models.Recipe;
import com.smartpantry.app.models.RecipeIngredient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * SQLite Database Handler providing full CRUD operations and initial seed data.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "smart_pantry.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_PANTRY = "pantry_items";
    public static final String COL_ID = "id";
    public static final String COL_NAME = "name";
    public static final String COL_NORMALIZED_NAME = "normalized_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT = "unit";
    public static final String COL_CATEGORY = "category";
    public static final String COL_EXPIRY_DATE = "expiry_date";
    public static final String COL_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createPantryTable = "CREATE TABLE " + TABLE_PANTRY + " ("
                + COL_ID + " TEXT PRIMARY KEY,"
                + COL_NAME + " TEXT NOT NULL,"
                + COL_NORMALIZED_NAME + " TEXT,"
                + COL_QUANTITY + " REAL NOT NULL,"
                + COL_UNIT + " TEXT NOT NULL,"
                + COL_CATEGORY + " TEXT,"
                + COL_EXPIRY_DATE + " TEXT,"
                + COL_NOTES + " TEXT" + ")";
        db.execSQL(createPantryTable);
        seedDefaultPantry(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PANTRY);
        onCreate(db);
    }

    public long insertPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ID, item.getId());
        values.put(COL_NAME, item.getName());
        values.put(COL_NORMALIZED_NAME, item.getNormalizedName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_EXPIRY_DATE, item.getExpiryDate());
        values.put(COL_NOTES, item.getNotes());
        return db.insert(TABLE_PANTRY, null, values);
    }

    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> items = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PANTRY, null, null, null, null, null, COL_NAME + " ASC")) {
            if (cursor.moveToFirst()) {
                do {
                    String id = cursor.getString(cursor.getColumnIndexOrThrow(COL_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                    String norm = cursor.getString(cursor.getColumnIndexOrThrow(COL_NORMALIZED_NAME));
                    double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                    String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT));
                    String cat = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                    String exp = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY_DATE));
                    int notesIdx = cursor.getColumnIndex(COL_NOTES);
                    String notes = notesIdx != -1 ? cursor.getString(notesIdx) : "";
                    items.add(new PantryItem(id, name, norm, qty, unit, cat, exp, notes));
                } while (cursor.moveToNext());
            }
        }
        return items;
    }

    public PantryItem getPantryItemById(String id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor cursor = db.query(TABLE_PANTRY, null, COL_ID + "=?",
                new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME));
                String norm = cursor.getString(cursor.getColumnIndexOrThrow(COL_NORMALIZED_NAME));
                double qty = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                String unit = cursor.getString(cursor.getColumnIndexOrThrow(COL_UNIT));
                String cat = cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY));
                String exp = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY_DATE));
                int notesIdx = cursor.getColumnIndex(COL_NOTES);
                String notes = notesIdx != -1 ? cursor.getString(notesIdx) : "";
                return new PantryItem(id, name, norm, qty, unit, cat, exp, notes);
            }
        }
        return null;
    }

    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, item.getName());
        values.put(COL_NORMALIZED_NAME, item.getNormalizedName());
        values.put(COL_QUANTITY, item.getQuantity());
        values.put(COL_UNIT, item.getUnit());
        values.put(COL_CATEGORY, item.getCategory());
        values.put(COL_EXPIRY_DATE, item.getExpiryDate());
        values.put(COL_NOTES, item.getNotes());
        return db.update(TABLE_PANTRY, values, COL_ID + "=?", new String[]{item.getId()});
    }

    public int deletePantryItem(String id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_PANTRY, COL_ID + "=?", new String[]{id});
    }

    public void deductIngredientsForRecipe(Recipe recipe) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            for (RecipeIngredient req : recipe.getIngredients()) {
                List<PantryItem> items = getAllPantryItems();
                for (PantryItem item : items) {
                    if (item.getName().equalsIgnoreCase(req.getName())) {
                        double remaining = item.getQuantity() - req.getQuantity();
                        if (remaining <= 0) {
                            deletePantryItem(item.getId());
                        } else {
                            item.setQuantity(remaining);
                            updatePantryItem(item);
                        }
                        break;
                    }
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public void resetToInitialSeed() {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_PANTRY);
        seedDefaultPantry(db);
    }

    private String getFutureDateStr(int daysToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, daysToAdd);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        return sdf.format(cal.getTime());
    }

    private void seedDefaultPantry(SQLiteDatabase db) {
        insertSeed(db, "Cheddar Cheese", 180, "g", "Dairy & Eggs", getFutureDateStr(19), "Mature cheddar block");
        insertSeed(db, "Cooked White Rice", 400, "g", "Pantry & Grains", getFutureDateStr(4), "Leftover from dinner two days ago");
        insertSeed(db, "Cooking Oil", 500, "ml", "Spices & Condiments", getFutureDateStr(178), "Sunflower oil");
        insertSeed(db, "Eggs", 6, "piece", "Dairy & Eggs", getFutureDateStr(10), "Free-range large brown eggs");
        insertSeed(db, "Fresh Milk", 1000, "ml", "Dairy & Eggs", getFutureDateStr(5), "Whole organic milk");
        insertSeed(db, "Brown Onions", 3, "piece", "Produce", getFutureDateStr(14), "Organic brown onions");
        insertSeed(db, "Garlic Cloves", 8, "clove", "Produce", getFutureDateStr(21), "Fresh garlic bulb");
        insertSeed(db, "Soy Sauce", 150, "ml", "Spices & Condiments", getFutureDateStr(120), "Low sodium soy sauce");
        insertSeed(db, "Tomatoes", 4, "piece", "Produce", getFutureDateStr(3), "Fresh ripe tomatoes");
        insertSeed(db, "Sliced Bread", 6, "slice", "Bakery", getFutureDateStr(2), "Soft whole wheat bread");
        insertSeed(db, "Butter", 200, "g", "Dairy & Eggs", getFutureDateStr(18), "Unsalted dairy butter");
        insertSeed(db, "Pasta (Spaghetti)", 350, "g", "Pantry & Grains", getFutureDateStr(200), "Italian durum wheat spaghetti");
        insertSeed(db, "Potatoes", 5, "piece", "Produce", getFutureDateStr(12), "Waxy yellow potatoes");
        insertSeed(db, "Carrots", 3, "piece", "Produce", getFutureDateStr(8), "Crunchy orange carrots");
        insertSeed(db, "Salt", 100, "g", "Spices & Condiments", getFutureDateStr(365), "Table salt");
        insertSeed(db, "Black Pepper", 50, "g", "Spices & Condiments", getFutureDateStr(365), "Ground pepper");
    }

    private void insertSeed(SQLiteDatabase db, String name, double qty, String unit,
                            String cat, String exp, String notes) {
        ContentValues values = new ContentValues();
        values.put(COL_ID, "seed_" + System.currentTimeMillis() + "_" + name.hashCode());
        values.put(COL_NAME, name);
        values.put(COL_NORMALIZED_NAME, name.toLowerCase(Locale.ROOT));
        values.put(COL_QUANTITY, qty);
        values.put(COL_UNIT, unit);
        values.put(COL_CATEGORY, cat);
        values.put(COL_EXPIRY_DATE, exp);
        values.put(COL_NOTES, notes);
        db.insert(TABLE_PANTRY, null, values);
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        list.add(new Recipe("rec_01", "Classic Egg Fried Rice",
                "The quintessential anti-waste dish. Turns day-old leftover cooked rice and eggs into a fragrant, savory skillet meal.",
                "Dinner", 5, 10, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Cooked White Rice", 300, "g"),
                        new RecipeIngredient("Eggs", 2, "piece"),
                        new RecipeIngredient("Brown Onions", 1, "piece"),
                        new RecipeIngredient("Garlic Cloves", 2, "clove"),
                        new RecipeIngredient("Soy Sauce", 2, "tbsp"),
                        new RecipeIngredient("Cooking Oil", 1, "tbsp")),
                Arrays.asList("Heat oil in pan", "Sauté chopped onion & garlic", "Scramble eggs",
                        "Add cold rice & soy sauce", "Toss over high heat")));

        list.add(new Recipe("rec_02", "Rich Tomato & Garlic Pasta",
                "Rich home-cooked tomato sauce clinging to spaghetti strands.",
                "Dinner", 10, 15, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Pasta (Spaghetti)", 200, "g"),
                        new RecipeIngredient("Tomatoes", 3, "piece"),
                        new RecipeIngredient("Garlic Cloves", 3, "clove"),
                        new RecipeIngredient("Brown Onions", 1, "piece"),
                        new RecipeIngredient("Cooking Oil", 2, "tbsp")),
                Arrays.asList("Boil spaghetti until al dente", "Sauté garlic & onions in oil",
                        "Add chopped tomatoes and simmer", "Toss with pasta")));

        list.add(new Recipe("rec_03", "Golden French Toast",
                "Day-old stale bread revived in golden sweet egg custard.",
                "Breakfast", 5, 8, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Sliced Bread", 4, "slice"),
                        new RecipeIngredient("Eggs", 2, "piece"),
                        new RecipeIngredient("Fresh Milk", 80, "ml"),
                        new RecipeIngredient("Butter", 20, "g")),
                Arrays.asList("Whisk eggs and milk", "Dip bread slices",
                        "Fry in melted butter until golden on both sides")));

        list.add(new Recipe("rec_04", "Garlic Butter Spaghetti",
                "Aglio e olio style pasta tossed with aromatic garlic, butter, and cheese.",
                "Dinner", 5, 12, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Pasta (Spaghetti)", 200, "g"),
                        new RecipeIngredient("Garlic Cloves", 4, "clove"),
                        new RecipeIngredient("Butter", 30, "g"),
                        new RecipeIngredient("Cooking Oil", 2, "tbsp"),
                        new RecipeIngredient("Cheddar Cheese", 40, "g")),
                Arrays.asList("Boil pasta until al dente", "Sauté sliced garlic gently in butter and oil",
                        "Toss pasta with garlic butter", "Finish with grated cheese")));

        list.add(new Recipe("rec_05", "Crispy Grilled Cheese Toastie",
                "Crunchy buttered toast with melted gooey cheese.",
                "Lunch", 3, 6, 1, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Sliced Bread", 2, "slice"),
                        new RecipeIngredient("Cheddar Cheese", 60, "g"),
                        new RecipeIngredient("Butter", 15, "g")),
                Arrays.asList("Butter bread exterior", "Place cheese inside",
                        "Toast in skillet until golden and melted")));

        list.add(new Recipe("rec_06", "Hearty Tomato & Onion Scramble",
                "Sweet stewed tomatoes folded into fluffy scrambled eggs.",
                "Breakfast", 5, 7, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Eggs", 3, "piece"),
                        new RecipeIngredient("Tomatoes", 2, "piece"),
                        new RecipeIngredient("Brown Onions", 1, "piece"),
                        new RecipeIngredient("Cooking Oil", 1, "tbsp")),
                Arrays.asList("Sauté onion until translucent", "Add diced tomatoes",
                        "Pour beaten eggs and gently fold")));

        list.add(new Recipe("rec_07", "Pan-Fried Potato Hash",
                "Crispy potato cubes sautéed with onions and garlic.",
                "Lunch", 10, 15, 2, "Easy",
                Arrays.asList(
                        new RecipeIngredient("Potatoes", 3, "piece"),
                        new RecipeIngredient("Brown Onions", 1, "piece"),
                        new RecipeIngredient("Garlic Cloves", 2, "clove"),
                        new RecipeIngredient("Cooking Oil", 2, "tbsp")),
                Arrays.asList("Cube potatoes", "Fry in hot oil until crispy",
                        "Add onions and garlic, toss until tender")));

        list.add(new Recipe("rec_08", "Chicken Fried Rice",
                "Savory rice stir fry with chicken breast (requires chicken).",
                "Dinner", 8, 12, 2, "Medium",
                Arrays.asList(
                        new RecipeIngredient("Cooked White Rice", 300, "g"),
                        new RecipeIngredient("Eggs", 2, "piece"),
                        new RecipeIngredient("Chicken Breast", 200, "g"),
                        new RecipeIngredient("Soy Sauce", 2, "tbsp"),
                        new RecipeIngredient("Cooking Oil", 1, "tbsp")),
                Arrays.asList("Sear chicken cubes", "Scramble eggs",
                        "Toss with cold rice and soy sauce")));

        return list;
    }

    public Recipe getRecipeById(String id) {
        for (Recipe recipe : getAllRecipes()) {
            if (recipe.getId().equals(id)) {
                return recipe;
            }
        }
        return null;
    }
}