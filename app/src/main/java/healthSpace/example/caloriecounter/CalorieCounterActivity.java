package healthSpace.example.caloriecounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.os.Handler;

import healthSpace.example.caloriecounter.R;

import java.util.List;
import java.util.Locale;

public class CalorieCounterActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
        private CalorieDB fCalorieDB = new CalorieDB();
        private boolean fRateFoodN = false;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_calorie_counter);

//            fCalorieDB.clearCache(this);
            populateCalorieDB();
            populateSpinners();
            hideControls();
            fCalorieDB.loadUserDayCaloriesWeight(this);

            populateTotalCalories();
            populateUserWeight();

            hideErrorText();
        }

        private void populateSpinners() {
            populateSpinnerFromResource(R.id.foodUnitList, R.array.UnitTypes);
            populateSpinnerFromResource(R.id.foodCategoryList, R.array.FoodCategories);

            String currFoodCategory = getSelectedFoodCategory();
            List<String> foodItems = fCalorieDB.getListOfFoods(currFoodCategory);
            populateSpinnerFromList(R.id.foodItemList, foodItems);
            List<String> userList = fCalorieDB.getUserList();
            populateSpinnerFromList(R.id.userList, userList);
        }

        private void populateSpinnerFromResource(int aSpinnerID, int aStrArrResourceID) {
            ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this, aStrArrResourceID, android.R.layout.simple_spinner_item);
            populateSpinner(aSpinnerID, adapter1);
        }

        private void populateSpinnerFromList(int aSpinnerID, List<String> aList) {
            ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                    android.R.layout.simple_spinner_item, aList);
            populateSpinner(aSpinnerID, adapter1);
        }

        private void populateSpinner(int aSpinnerID, ArrayAdapter aAdapter) {
            Spinner sp1 = findViewById(aSpinnerID);
            aAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp1.setAdapter(aAdapter);
            sp1.setOnItemSelectedListener(this);
        }

        private void updateFoodSpinner() {
            String currFoodCategory = getSelectedFoodCategory();
            List<String> foodItems = fCalorieDB.getListOfFoods(currFoodCategory);
            Spinner sp1 = findViewById(R.id.foodItemList);
            ArrayAdapter<String> adapter1 = (ArrayAdapter<String>)(sp1.getAdapter());
            adapter1.clear();
            adapter1.addAll(foodItems);
            adapter1.notifyDataSetChanged();
        }

        private void populateCalorieDB() {
            String debugStr = "";
            try {
                fCalorieDB.readCalorieDatabase(this);
                fCalorieDB.writeCalorieDatabase(this);
                fCalorieDB.readCalorieDatabase(this);
                debugStr = fCalorieDB.getData();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            if (fCalorieDB.fHadToReadFromApk) {
//                showLabelForDuration("From APK\n" + debugStr, 3000);
//            } else {
//                showLabelForDuration("From file\n" + debugStr, 3000);
//            }
        }

        private void populateTotalCalories() {
            try {
                String user = getSelectedUser();
                TextView tv1 = findViewById(R.id.totalCalories);
                tv1.setText(Double.toString(fCalorieDB.getCalories(user)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("CaloriesCounterActivity", "Could not add calories to user.");
            }

        }

        private void populateUserWeight() {
            try {
                String user = getSelectedUser();
                TextView tv1 = findViewById(R.id.userWeight);
                tv1.setText(Double.toString(fCalorieDB.getWeight(user)));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("CaloriesCounterActivity", "Could not set weight for user.");
            }
        }

        public void onAddCalories(View aView) {
            assert(isCaloriesEntryModeSelected());
            EditText calsEntered = findViewById(R.id.caloriesEntered);
            String calsToAddString = calsEntered.getText().toString().trim();
            if (calsToAddString.isEmpty()) {
                showLabelForDuration("Please enter calories", 3000);
            } else {
                double calsToAdd = Double.parseDouble(calsToAddString);
                if (addCalories(calsToAdd)) calsEntered.setText("");
            }
        }

        public void onAddFoodAmount(View aView) {
            assert(!isCaloriesEntryModeSelected());
            String currUnit = getSelectedCaloriesUnit();
            EditText et1 = findViewById(R.id.foodAmountEntered);
            String foodAmountStr = et1.getText().toString().trim();
            if (foodAmountStr.isEmpty()) {
                showLabelForDuration("Please enter food amount", 3000);
            } else {
                double foodAmount = Double.parseDouble(foodAmountStr);
                try {
                    double calsToAdd = 0.0;
                    if (fRateFoodN) {
                        EditText rt = findViewById(R.id.rateEntered);
                        String rateStr = rt.getText().toString().trim();
                        if (rateStr.isEmpty()) {
                            showLabelForDuration("Please enter food rate", 3000);
                        } else {
                            double rate = Double.parseDouble(rateStr);
                            calsToAdd = fCalorieDB.getFoodCaloriesWithRate(currUnit, rate, foodAmount);
                            if (addCalories(calsToAdd)) et1.setText("");
                        }
                    } else {
                        String currFoodItem = getSelectedFoodItem();
                        calsToAdd = fCalorieDB.getFoodCalories(currFoodItem, currUnit, foodAmount);
                        if (addCalories(calsToAdd)) et1.setText("");
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.d("CaloriesCounterActivity", "Could compute calories from food amount.");
                }
            }
        }

        public boolean addCalories(double aCalsToAdd) {
            boolean success = false;
            try {
                String user = getSelectedUser();
                success = fCalorieDB.addCalories(user, aCalsToAdd);
                populateTotalCalories();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("CaloriesCounterActivity", "Could not add calories to user.");
            }
            return success;
        }

        public void onRateFoodSwitchClick(View aView) {
            Switch rateSwitch = (Switch)findViewById(R.id.rateFoodItemSwitch);
            fRateFoodN = rateSwitch.isChecked();
            hideControls();
        }

        public void onGoHome(View aView) {
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        public void onClearCache(View aView) {
            fCalorieDB.clearCache(this);
        }

        public void onClearCalories(View aView) {
            TextView tv1 = findViewById(R.id.totalCalories);
            tv1.setText("0");
            String user = getSelectedUser();
            fCalorieDB.clearUserCalories(user);
        }

        public void onSetWeight(View aView) {
            try {
                String user = getSelectedUser();
                EditText et1 = findViewById(R.id.weightEntered);
                Log.d("CalorieCounterActivity", "Parsing weight " + et1.getText());
                String weightStr = et1.getText().toString().trim();
                if (weightStr.isEmpty()) {
                    showLabelForDuration("Please enter weight", 3000);
                } else {
                    double weight = Double.parseDouble(weightStr);

                    fCalorieDB.setWeight(user, weight);
                    et1.setText("");

                    populateUserWeight();
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                Log.d("CaloriesCounterActivity", "Could not set user weight.");
            }
        }

        public void onFinishDay(View aView) {
            try {
                String user = getSelectedUser();
                fCalorieDB.endDayForUser(this, user);
                populateTotalCalories();
                populateUserWeight();
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("CaloriesCounterActivity", "Could not log history for user.");
            }
        }

        public boolean isCaloriesEntryModeSelected() {
            String selectedItem = getSelectedCaloriesUnit();
            return selectedItem.equalsIgnoreCase("calories");
        }

        public String getSelectedUser() {
            AdapterView av1 = findViewById(R.id.userList);
            return av1.getSelectedItem().toString();
        }

        public String getSelectedFoodCategory() {
            AdapterView av1 = findViewById(R.id.foodCategoryList);
            return av1.getSelectedItem().toString();
        }

        public String getSelectedFoodItem() {
            AdapterView av1 = findViewById(R.id.foodItemList);
            return av1.getSelectedItem().toString();
        }

        public String getSelectedCaloriesUnit() {
            AdapterView av1 = findViewById(R.id.foodUnitList);
            return av1.getSelectedItem().toString();
        }

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String unitType = adapterView.getItemAtPosition(i).toString().toLowerCase(Locale.ROOT);
            Log.d("CalorieCounterActivity", "Selected " + unitType + " from spinner");
            hideControls();
            populateTotalCalories();
            populateUserWeight();
            updateFoodSpinner();
        }

        private void hideControls() {
            Log.d("CalorieCounterActivity", "Trying to hide buttons");
            Button calsButton = findViewById(R.id.addCalories);
            EditText calsEntered = findViewById(R.id.caloriesEntered);
            TextView foodCategoryLabel = findViewById(R.id.foodCategoryListLabel);
            Spinner foodCategory = findViewById(R.id.foodCategoryList);
            TextView foodItemLabel = findViewById(R.id.foodItemListLabel);
            Spinner foodItem = findViewById(R.id.foodItemList);
            TextView rateLabel = findViewById((R.id.rateEnteredLabel));
            EditText rateEntered = findViewById(R.id.rateEntered);
            Button foodAmountButton = findViewById(R.id.foodAmountButton);
            EditText foodAmount = findViewById(R.id.foodAmountEntered);
            Switch rateFoodSwitch = findViewById(R.id.rateFoodItemSwitch);
            if (isCaloriesEntryModeSelected()) {
                calsEntered.setVisibility(View.VISIBLE);
                calsButton.setVisibility(View.VISIBLE);
                foodCategoryLabel.setVisibility(View.INVISIBLE);
                foodCategory.setVisibility(View.INVISIBLE);
                foodItemLabel.setVisibility(View.INVISIBLE);
                foodItem.setVisibility(View.INVISIBLE);
                rateLabel.setVisibility(View.INVISIBLE);
                rateEntered.setVisibility(View.INVISIBLE);
                foodAmountButton.setVisibility(View.INVISIBLE);
                foodAmount.setVisibility(View.INVISIBLE);
                rateFoodSwitch.setVisibility(View.INVISIBLE);
                Log.d("CalorieCounterActivity", "hidden for calories");
            } else {
                calsEntered.setVisibility(View.INVISIBLE);
                calsButton.setVisibility(View.INVISIBLE);
                if (fRateFoodN) {
                    foodCategoryLabel.setVisibility(View.INVISIBLE);
                    foodCategory.setVisibility(View.INVISIBLE);
                    foodItemLabel.setVisibility(View.INVISIBLE);
                    foodItem.setVisibility(View.INVISIBLE);
                    rateLabel.setVisibility(View.VISIBLE);
                    rateEntered.setVisibility(View.VISIBLE);
                } else {
                    foodCategoryLabel.setVisibility(View.VISIBLE);
                    foodCategory.setVisibility(View.VISIBLE);
                    foodItemLabel.setVisibility(View.VISIBLE);
                    foodItem.setVisibility(View.VISIBLE);
                    rateLabel.setVisibility(View.INVISIBLE);
                    rateEntered.setVisibility(View.INVISIBLE);
                }
                foodAmountButton.setVisibility(View.VISIBLE);
                foodAmount.setVisibility(View.VISIBLE);
                rateFoodSwitch.setVisibility(View.VISIBLE);
                Log.d("CalorieCounterActivity", "hidden for non-calories");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }

        protected void onStop() {
            super.onStop();
            fCalorieDB.saveUserDayCaloriesWeight(this);
//            try {
//                fCalorieDB.finalize();
//            } catch (Throwable e) {
//                e.printStackTrace();
//                Log.d("CalorieCounterActivity", "Could not finalize calorie DB.\n" + e.getMessage());
//            }
//            fCalorieDB = null;
            Log.d("CalorieCounterActivity", "Stopped");
        }

        private void hideErrorText() {
            TextView tv1 = findViewById(R.id.textOut1);
            tv1.setVisibility(View.INVISIBLE);
        }

        private void showLabelForDuration(String aTextToDisplay, int duration) {
            TextView tv1 = findViewById(R.id.textOut1);
            tv1.setVisibility(View.VISIBLE);
            tv1.setText(aTextToDisplay);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tv1.setVisibility(View.INVISIBLE);
                }
            }, duration);
        }
}