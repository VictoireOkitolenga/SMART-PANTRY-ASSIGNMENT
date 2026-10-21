package com.smartpantry.app;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.smartpantry.app.database.DatabaseHelper;
import com.smartpantry.app.logic.StrictMatcher;
import com.smartpantry.app.models.PantryItem;
import java.util.Calendar;

/**
 * Add / Edit Ingredient Screen with full form validation and notes support.
 */
public class AddEditIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "com.smartpantry.app.EXTRA_ITEM_ID";

    private TextView tvFormTitle;
    private EditText etName, etQuantity, etExpiryDate, etNotes;
    private Spinner spinnerUnit, spinnerCategory;
    private Button btnSave, btnCancel;
    private DatabaseHelper dbHelper;
    private String editingItemId = null;

    private final String[] UNITS = {"g", "kg", "ml", "l", "tsp", "tbsp", "cup", "piece", "clove", "slice", "can"};
    private final String[] CATEGORIES = {"Produce", "Dairy & Eggs", "Meat & Seafood", "Pantry & Grains", "Bakery", "Spices & Condiments", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_ingredient);

        dbHelper = new DatabaseHelper(this);

        tvFormTitle = findViewById(R.id.tv_form_title);
        etName = findViewById(R.id.et_ingredient_name);
        etQuantity = findViewById(R.id.et_ingredient_quantity);
        etExpiryDate = findViewById(R.id.et_expiry_date);
        etNotes = findViewById(R.id.et_ingredient_notes);
        spinnerUnit = findViewById(R.id.spinner_unit);
        spinnerCategory = findViewById(R.id.spinner_category);
        btnSave = findViewById(R.id.btn_save_ingredient);
        btnCancel = findViewById(R.id.btn_cancel);

        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, UNITS);
        spinnerUnit.setAdapter(unitAdapter);

        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, CATEGORIES);
        spinnerCategory.setAdapter(categoryAdapter);

        etExpiryDate.setOnClickListener(v -> showDatePicker());

        if (getIntent().hasExtra(EXTRA_ITEM_ID)) {
            editingItemId = getIntent().getStringExtra(EXTRA_ITEM_ID);
            tvFormTitle.setText("Edit Ingredient");
            loadItemDetails(editingItemId);
        } else {
            tvFormTitle.setText("Add Leftover Ingredient");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            String formatted = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
            etExpiryDate.setText(formatted);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void loadItemDetails(String id) {
        PantryItem item = dbHelper.getPantryItemById(id);
        if (item != null) {
            etName.setText(item.getName());
            etQuantity.setText(String.valueOf(item.getQuantity()));
            etExpiryDate.setText(item.getExpiryDate());
            etNotes.setText(item.getNotes());

            for (int i = 0; i < UNITS.length; i++) {
                if (UNITS[i].equalsIgnoreCase(item.getUnit())) {
                    spinnerUnit.setSelection(i);
                    break;
                }
            }
            for (int i = 0; i < CATEGORIES.length; i++) {
                if (CATEGORIES[i].equalsIgnoreCase(item.getCategory())) {
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyStr = etQuantity.getText().toString().trim();
        String expiry = etExpiryDate.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String unit = spinnerUnit.getSelectedItem().toString();
        String category = spinnerCategory.getSelectedItem().toString();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Ingredient name is required");
            etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(qtyStr)) {
            etQuantity.setError("Quantity is required");
            etQuantity.requestFocus();
            return;
        }

        double quantity;
        try {
            quantity = Double.parseDouble(qtyStr);
            if (quantity <= 0) {
                etQuantity.setError("Quantity must be greater than zero");
                etQuantity.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etQuantity.setError("Enter a valid numerical quantity");
            etQuantity.requestFocus();
            return;
        }

        String normalized = StrictMatcher.normalize(name);

        if (editingItemId != null) {
            PantryItem updated = new PantryItem(editingItemId, name, normalized, quantity, unit, category, expiry, notes);
            dbHelper.updatePantryItem(updated);
            Toast.makeText(this, "Updated " + name, Toast.LENGTH_SHORT).show();
        } else {
            String newId = "item_" + System.currentTimeMillis();
            PantryItem newItem = new PantryItem(newId, name, normalized, quantity, unit, category, expiry, notes);
            dbHelper.insertPantryItem(newItem);
            Toast.makeText(this, "Added " + name + " to pantry", Toast.LENGTH_SHORT).show();
        }

        finish();
    }
}
