# SMART-PANTRY-ASSIGNMENT

A Java Android Application that Suggests Recipes Based Strictly on Leftover Ingredients to Cut Food Waste

Module: Mobile App Development 700
Student Name: Victoire Okitolenga
Student ITS No: 402312973
Target Platform**: Android (Java, API 24+)
Database Choice**: SQLite (`SQLiteOpenHelper`)

---

#1. Project Overview & Problem Solved
Household food waste is an acute economic and ecological problem. A major cause is discarded half-used ingredients and confusion over what can be cooked without purchasing more food.

The Smart Pantry Manager implements a Strict-Matching Rule (Section 2.3):
- A recipe is only suggested if every required ingredient is currently present in the user's pantry in at least the required quantity.
- If a recipe requires 5 ingredients and the user has 4, it is strictly excluded from suggestions.
- The matching engine includes robust normalization for singular/plural forms (e.g. *tomato* vs *tomatoes*) and unit conversions (g/kg, ml/l, tsp/tbsp).

---

#2. Database Justification (Section 3.2)
We selected SQLite via `SQLiteOpenHelper` for the following reasons:
1. Zero External Dependency / Offline Availability**: Pantry tracking is inherently a home kitchen activity. An on-device SQLite database ensures instantaneous CRUD responsiveness even with zero internet connectivity.
2. ACID Transaction Support**: When a user marks a recipe as cooked, ingredient quantities must be atomically deducted across multiple pantry rows. SQLite transactions prevent partial deductions and data corruption.
3. Curriculum Alignment**: Directly builds upon the persistent data storage chapter covered in Mobile App Development 700.

---

# 3. Architecture & Screens
The application features 5 distinct Activities:
1. `PantryListActivity`: Dynamic `RecyclerView` bound to SQLite via `PantryAdapter`, with search filtering and CRUD operations.
2. `AddEditIngredientActivity`: Comprehensive form with input validation (non-empty names, positive numeric quantities, unit and category spinners, DatePicker for expiry).
3. `SuggestedRecipesActivity`: Core business logic screen displaying 100% strict matches with informative feedback when 0 recipes match, plus a bonus "Almost There" tab.
4. `RecipeDetailActivity`: Complete ingredient checklist, preparation instructions, and interactive "Cooked This" pantry deduction.
5. `SettingsActivity`: Expiry threshold alerts, unit systems, and database re-seeding.

---

 4. Setup and Run Instructions
1. Open Android Studio.
2. Choose File -> Open and select this project directory.
3. Allow Gradle to sync dependencies.
4. Select an Android Emulator running API 24 or higher (or connect a physical Android device with USB Debugging enabled).
5. Click Run (`Shift + F10`).
