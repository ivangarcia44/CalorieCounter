package healthSpace.example.caloriecounter;

import android.content.Context;
import android.util.Log;

import healthSpace.example.caloriecounter.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class CalorieDB {
    private ArrayList<FoodCalories> foodList = new ArrayList<FoodCalories>();
    private static String localFoodCaloriesFileName = "food_calories_local.txt";
    private static String localFoodSynonymsFileName = "food_synonyms_local.txt";
    private static String fFoodCaloriesFileHeader = "Food, CalsPerGram, CalsPerCup, CalsPerUnit, CalsPerTbsp, CalsPerTsp\n";
    private static String fFoodSynonymsFileHeader = "Food Name, synonym list\n";
    public boolean fHadToReadFromApk = false;
    private Users fUsers = new Users();

    private enum CSV_PARSING_ENUM { FOOD_CALORIES, SYNONYMS };

    protected void finalize() throws Throwable
    {
//        foodList.clear();
//        foodList = null;
//        fUsers.finalize();
//        fUsers = null;
//        localFoodCaloriesFileName = null;
//        localFoodSynonymsFileName = null;
//        fFoodCaloriesFileHeader = null;
//        fFoodSynonymsFileHeader = null;
    }

    public void addFood(String aName, String aCategory, double aCalsPerGram, double aCalsPerCup,
                        double aCalsPerUnit, double aCalsPerTbsp, double aCalsPerTsp,
                        double aCalsPerMl, double aCalsPerOz) throws ClassNotFoundException {
        foodList.add(new FoodCalories(aName, aCategory, aCalsPerGram, aCalsPerCup,
                                      aCalsPerUnit, aCalsPerTbsp, aCalsPerTsp, aCalsPerMl,
                                      aCalsPerOz));
    }

    public boolean addCalories(String aUser, double aCals) throws ClassNotFoundException {
        return fUsers.addCalories(aUser, aCals);
    }
    public double getCalories(String aUser) throws ClassNotFoundException {
        return fUsers.getCalories(aUser);
    }
    public void clearUserCalories(String aUser) {
        fUsers.clearUserCalories(aUser);
    }

    private FoodCalories getFood(String aFoodName) throws ClassNotFoundException {
        Log.d("CalorieDB", "Looking for food with name: " + aFoodName);
        Optional<FoodCalories> foodObj = foodList.stream().filter(food -> food.isFoodName(aFoodName)).findAny();
        if (foodObj.isPresent()) {
            return foodObj.get();
        } else {
            throw new ClassNotFoundException("Food not found");
        }
    }

    public double getFoodCalories(String aFoodName, String aCalsUnit, double aFoodAmount) throws ClassNotFoundException {
        FoodCalories foodObj = getFood(aFoodName);
        return foodObj.getFoodCalories(aCalsUnit, aFoodAmount);
    }

    public double getFoodCaloriesWithRate(String aCalsUnit, double aCalsRate, double aFoodAmount) throws ClassNotFoundException {
        return FoodCalories.getFoodCaloriesWithRate(aCalsUnit, aCalsRate, aFoodAmount);
    }

    private void addFoodSynonym(String aFoodName, String aSynonym) throws ClassNotFoundException {
        FoodCalories foodObj = getFood(aFoodName);
        foodObj.addSynonim(aSynonym);
    }

    public String getData() throws ClassNotFoundException {
        String retStr = "";
        for (int i = 0; i < foodList.size(); i++) {
            retStr += String.format("%s\n", foodList.get(i).getCaloriesRowString());
        }
        for (int i = 0; i < foodList.size(); i++) {
            retStr += String.format("%s\n", foodList.get(i).getSynonymsRowString());
        }
        retStr += fUsers.getUsersString();
        return retStr;
    }

    public List<String> getListOfFoods(String aFoodCategory) {
        List<String> retStrings = new ArrayList<String>();
        foodList.stream().filter(food -> food.isFoodInCategory(aFoodCategory)).forEach(food -> retStrings.add(food.getName()));
        return retStrings;
    }

    public List<String> getUserList() {
        return fUsers.getUserList();
    }

    public void clearCache(Context aContext) {
        File caloriesFile = new File(aContext.getCacheDir(), localFoodCaloriesFileName);
        if (caloriesFile.exists()) caloriesFile.delete();
        File synonymFile = new File(aContext.getCacheDir(), localFoodSynonymsFileName);
        if (synonymFile.exists()) synonymFile.delete();
        fUsers.clearCache(aContext);
        Log.d("CalorieDB", "Cleared cache");
    }

    public void writeCalorieDatabase(Context aContext) throws IOException {
        writeCaloriesFile(aContext);
        writeSynonymsFile(aContext);
        fUsers.writeUsers(aContext);
    }

    public void writeCaloriesFile(Context aContext) throws IOException {
        try {
            File file = new File(aContext.getCacheDir(), localFoodCaloriesFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(fFoodCaloriesFileHeader);
            for (int i = 0; i < foodList.size(); i++) {
                outputStreamWriter.write(foodList.get(i).getCaloriesRowString() + "\n");
                Log.d("CalorieDB", String.format("Writing to file %s: %s", i, foodList.get(i).getCaloriesRowString()));
            }
            outputStreamWriter.close();
        }
        catch (Exception e) {
            Log.e("CalorieDB", "File write failed: " + e.toString());
        }
    }

    public void writeSynonymsFile(Context aContext) throws IOException {
        try {
            File file = new File(aContext.getCacheDir(), localFoodSynonymsFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(fFoodSynonymsFileHeader);
            for (int i = 0; i < foodList.size(); i++) {
                if (foodList.get(i).hasSynonyms()) {
                    outputStreamWriter.write(foodList.get(i).getSynonymsRowString() + "\n");
                    Log.d("CalorieDB", String.format("Writing to file %s: %s", i, foodList.get(i).getSynonymsRowString()));
                } else {
                    Log.d("CalorieDB", foodList.get(i).getName() + " does not have synonyms.");
                }
            }
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("CalorieDB", "File write failed: " + e.toString());
        }
    }

    private void parseCsvLine(String aCurrRow, CSV_PARSING_ENUM aParsingOption) throws IOException, ClassNotFoundException {
        String[] rowTokens = aCurrRow.split(",");
        if (aParsingOption == CSV_PARSING_ENUM.FOOD_CALORIES) {
            if (rowTokens.length != 9) {
                throw new IOException("Expected number of columns in CSV file to be seven");
            }
            foodList.add(new FoodCalories(rowTokens[0].toLowerCase(Locale.ROOT).replaceAll("\\s+",""),
                    rowTokens[1].toLowerCase(Locale.ROOT).replaceAll("\\s+",""), Double.parseDouble(rowTokens[2]),
                    Double.parseDouble(rowTokens[3]), Double.parseDouble(rowTokens[4]),
                    Double.parseDouble(rowTokens[5]), Double.parseDouble(rowTokens[6]),
                    Double.parseDouble(rowTokens[7]), Double.parseDouble(rowTokens[8])));
        } else if (aParsingOption == CSV_PARSING_ENUM.SYNONYMS) {
            if (rowTokens.length < 2) throw new IOException("Expected number of columns to be 2 or higher: " + rowTokens.length);
            String foodName = rowTokens[0].toLowerCase(Locale.ROOT);
            for (int i = 1; i < rowTokens.length; i++) {
                addFoodSynonym(foodName, rowTokens[i].toLowerCase(Locale.ROOT));
            }
        } else {
            throw new IOException("Invalid option: " + aParsingOption);
        }
    }

    private void readCsv(Context context, InputStreamReader aIsr, CSV_PARSING_ENUM aParsingOption) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(aIsr);
        String line;
        String firstLine = br.readLine();
        Log.d("CalorieDB", "Reading line: " + firstLine);
        while ((line = br.readLine()) != null) {
            Log.d("CalorieDB", "Reading line: " + line);
            parseCsvLine(line, aParsingOption);
        }
    }

    public void readCalorieDatabase(Context aContext) throws IOException, ClassNotFoundException {
        foodList.clear();
        readFoodFile(aContext, CSV_PARSING_ENUM.FOOD_CALORIES);
        readFoodFile(aContext, CSV_PARSING_ENUM.SYNONYMS);
        fUsers.readUsers(aContext);
        Log.d("CalorieDB", getData());
    }

    private String getFoodFileName(CSV_PARSING_ENUM aParsingOption) throws ClassNotFoundException {
        switch (aParsingOption) {
            case FOOD_CALORIES:
                return localFoodCaloriesFileName;
            case SYNONYMS:
                return localFoodSynonymsFileName;
            default:
                throw new ClassNotFoundException("Invalid option: " + aParsingOption);
        }
    }

    private int getFoodApkFileIndex(CSV_PARSING_ENUM aParsingOption) throws ClassNotFoundException {
        switch (aParsingOption) {
            case FOOD_CALORIES:
                return R.raw.food_calories_csv;
            case SYNONYMS:
                return R.raw.food_synonyms_csv;
            default:
                throw new ClassNotFoundException("Invalid option: " + aParsingOption);
        }
    }

    public void readFoodFile(Context aContext, CSV_PARSING_ENUM aParsingOption) throws IOException, ClassNotFoundException {
        String localFileName = getFoodFileName(aParsingOption);
        File file = new File(aContext.getCacheDir(), localFileName);
        int apkFileIdx = getFoodApkFileIndex(aParsingOption);
        if (file.exists()) {
            Log.d("CalorieDB", "Reading file " + localFileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            readCsv(aContext, isr, aParsingOption);
            isr.close();
        } else {
            Log.d("CalorieDB", "Reading file " + apkFileIdx);
            fHadToReadFromApk = true;
            InputStream is = aContext.getResources().openRawResource(apkFileIdx);
            InputStreamReader isr = new InputStreamReader(is);
            readCsv(aContext, isr, aParsingOption);
            isr.close();
        }
    }

    public double getWeight(String aUser) throws ClassNotFoundException { return fUsers.getWeight(aUser); }
    public void setWeight(String aUser, double aWeight) throws ClassNotFoundException { fUsers.setWeight(aUser, aWeight); }

    public void endDayForUser(Context aContext, String aUser) throws IOException, ClassNotFoundException {
        fUsers.endDayForUser(aContext, aUser);
    }

    public void saveUserDayCaloriesWeight(Context aContext) {
        fUsers.saveUserDayCaloriesWeight(aContext);
    }

    public void loadUserDayCaloriesWeight(Context aContext) {
        fUsers.loadUserDayCaloriesWeight(aContext);
    }
}
