package healthSpace.example.caloriecounter;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodCalories {
    private String fName;
    private FoodCategoryEnum fCategory;
    private double fCaloriesPerGram = -1;
    private double fCaloriesPerCup = -1;
    private double fCaloriesPerUnit = -1;
    private double fCaloriesPerTbsp = -1;
    private double fCaloriesPerTsp = -1;
    private double fCaloriesPerMl = -1;
    private double fCaloriesPerOz = -1;
    private List<String> fSynonyms;

    public enum CaloriesUnitEnum {
        GRAM, CUP, UNIT, TBSP, TSP, MILILITERS, OUNCES
    }

    public enum FoodCategoryEnum {
        CARBS, PROTEIN, FRUITS, VEGETABLES, LEGUMES, FATS, DAIRY
    }

    protected void finalize() throws Throwable
    {
//        fSynonyms.clear();
//        fSynonyms = null;
//        fName = null;
    }

    public FoodCalories(String aName, String aCategory, double aCalsPerGram, double aCalsPerCup, double aCalsPerUnit,
                        double aCalsPerTbsp, double aCalsPerTsp, double aCalsPerMl, double aCalsPerOz) throws ClassNotFoundException {
        fName = aName.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
        fCategory = getCategoryFromString(aCategory.toLowerCase(Locale.ROOT).replaceAll("\\s+",""));
        fCaloriesPerGram = aCalsPerGram;
        fCaloriesPerCup = aCalsPerCup;
        fCaloriesPerUnit = aCalsPerUnit;
        fCaloriesPerTbsp = aCalsPerTbsp;
        fCaloriesPerTsp = aCalsPerTsp;
        fCaloriesPerMl = aCalsPerMl;
        fCaloriesPerOz = aCalsPerOz;
        fSynonyms = new ArrayList<String>();
    }

    public void addSynonim(String aSynonym) {
        fSynonyms.add(aSynonym);
    }

    public String getCaloriesRowString() throws ClassNotFoundException {
        return fName + ", " + getCategoryString(fCategory) + ", " + fCaloriesPerGram + ", " +
               fCaloriesPerCup + ", " + fCaloriesPerUnit + ", " + fCaloriesPerTbsp + ", " +
               fCaloriesPerTsp + ", " + fCaloriesPerMl + ", " + fCaloriesPerOz;
    }

    public String getName() { return fName; }

    public boolean hasSynonyms() { return fSynonyms.size() > 0; }

    public String getSynonymsRowString() {
        String retStr = fName;
        for (int i = 0; i < fSynonyms.size(); i++) {
            retStr += ", " + fSynonyms.get(i);
        }
        return retStr;
    }

    public double getCalorieRate(CaloriesUnitEnum aUnit) throws ClassNotFoundException {
        switch (aUnit) {
            case GRAM:
                return fCaloriesPerGram;
            case CUP:
                return fCaloriesPerCup;
            case UNIT:
                return fCaloriesPerUnit;
            case TBSP:
                return fCaloriesPerTbsp;
            case TSP:
                return fCaloriesPerTsp;
            case MILILITERS:
                return fCaloriesPerMl;
            case OUNCES:
                return fCaloriesPerOz;
            default:
                throw new ClassNotFoundException("Invalid enum");
        }
    }

    static public CaloriesUnitEnum getUnitEnum(String aUnitType) throws ClassNotFoundException {
        Log.d("FoodCalories", "Getting enum for " + aUnitType);
        if (aUnitType.equalsIgnoreCase("Grams")) {
            return CaloriesUnitEnum.GRAM;
        } else if (aUnitType.equalsIgnoreCase("Cup")) {
            return CaloriesUnitEnum.CUP;
        } else if (aUnitType.equalsIgnoreCase("Unit")) {
            return CaloriesUnitEnum.UNIT;
        } else if (aUnitType.equalsIgnoreCase("Tablespoon")) {
            return CaloriesUnitEnum.TBSP;
        } else if (aUnitType.equalsIgnoreCase("Teaspoon")) {
            return CaloriesUnitEnum.TSP;
        } else if (aUnitType.equalsIgnoreCase("Mililiters")) {
            return CaloriesUnitEnum.MILILITERS;
        } else if (aUnitType.equalsIgnoreCase("Ounces")) {
            return CaloriesUnitEnum.OUNCES;
        } else {
            throw new ClassNotFoundException("Unit type not found: " + aUnitType);
        }
    }

    static public FoodCategoryEnum getCategoryFromString(String aCategory) throws ClassNotFoundException {
        Log.d("FoodCalories", "Getting enum for " + aCategory);
        if (aCategory.equals("carbs")) {
            return FoodCategoryEnum.CARBS;
        } else if (aCategory.equals("proteins")) {
            return FoodCategoryEnum.PROTEIN;
        } else if (aCategory.equals("fruits")) {
            return FoodCategoryEnum.FRUITS;
        } else if (aCategory.equals("legumes")) {
            return FoodCategoryEnum.LEGUMES;
        } else if (aCategory.equals("vegetables")) {
            return FoodCategoryEnum.VEGETABLES;
        } else if (aCategory.equals("fats")) {
            return FoodCategoryEnum.FATS;
        } else if (aCategory.equals("dairy")) {
            return FoodCategoryEnum.DAIRY;
        } else {
            throw new ClassNotFoundException("Food category not found: " + aCategory);
        }
    }

    static public String getCategoryString(FoodCategoryEnum aCategory) throws ClassNotFoundException {
        switch (aCategory) {
            case CARBS:
                return "carbs";
            case PROTEIN:
                return "proteins";
            case FRUITS:
                return "fruits";
            case LEGUMES:
                return "legumes";
            case VEGETABLES:
                return "vegetables";
            case FATS:
                return "fats";
            case DAIRY:
                return "dairy";
            default:
                throw new ClassNotFoundException("Food category not found: " + aCategory);
        }
    }

    public boolean isFoodName(String aName) {
        return fName.equals(aName) || fSynonyms.stream().filter(name -> name.equals(aName)).findAny().isPresent();
    }

    public boolean isFoodInCategory(String aCategory) {
        try {
            return getCategoryFromString(aCategory) == fCategory;
        } catch (ClassNotFoundException e) {
            Log.d("FoodCalories", "Could not determine if food was in category " + aCategory);
            return false;
        }
    }

    public double getFoodCalories(String aUnitType, double aFoodAmount) throws ClassNotFoundException {
        CaloriesUnitEnum unitType = getUnitEnum(aUnitType);
        double rate = getCalorieRate(unitType);
        Log.d("FoodCalories", "rate(" + rate + ") * aFoodAmount(" + aFoodAmount + ") = " + rate * aFoodAmount);
        return rate * aFoodAmount;
    }

    static public double getFoodCaloriesWithRate(String aCalsUnit, double aCalsRate, double aFoodAmount) throws ClassNotFoundException {
        CaloriesUnitEnum unitType = getUnitEnum(aCalsUnit);
        Log.d("FoodCalories", "rate(" + aCalsRate + ") * aFoodAmount(" + aFoodAmount + ") = " + aCalsRate * aFoodAmount);
        return aCalsRate * aFoodAmount;
    }
}
