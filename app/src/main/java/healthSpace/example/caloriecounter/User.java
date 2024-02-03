package healthSpace.example.caloriecounter;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Locale;

public class User {
    private String fName;
    private double fTotalCalories;
    private double fWeight;
    private String fHistoryFileName;
    private String fDayFileName;

    public User(String aName) {
        fName = aName;
        fTotalCalories = 0.0;
        fWeight = 0.0;
        String userName = fName.toLowerCase(Locale.ROOT).replaceAll("\\s+","");
        fHistoryFileName = userName + "_cal_weight_history.txt";
        fDayFileName = userName + "_cal_weight_day.txt";
    }

    public String getName() { return fName; }
    public void setName(String aName) { fName = aName; }
    public double getCalories() { return fTotalCalories; }
    public void setCalories(double aCalories) { fTotalCalories = aCalories; }
    public void clearCalories() { fTotalCalories = 0.0; }
    public double getWeight() { return fWeight; }
    public void setWeight(double aWeight) { fWeight = aWeight; }
    public String getFileName() { return fHistoryFileName; }

    public void appendHistoryLine(Context aContext) throws IOException {
        try {
            File file = new File(aContext.getCacheDir(), fHistoryFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file, true);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            String historyLine = Double.toString(fTotalCalories) + ", " + Double.toString(fWeight) + "\n";
            outputStreamWriter.append(historyLine);
            Log.d("User", String.format("Writing to file: %s", historyLine));
            outputStreamWriter.close();
            fTotalCalories = 0.0;
            fWeight = 0.0;
        }
        catch (Exception e) {
            Log.e("User", "History file write failed: " + e.toString());
        }
    }

    public String readUserHistoryFile(Context aContext) throws IOException {
        String retStr = "";
        String line;
        File file = new File(aContext.getCacheDir(), fHistoryFileName);
        if (file.exists()) {
            Log.d("User", "Reading file " + fHistoryFileName);
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                Log.d("User", "Reading line: " + line);
                retStr += line + "\n";
            }
            isr.close();
        }
        return retStr;
    }

    public void deleteHistoryFile(Context aContext) {
        File historyFile = new File(aContext.getCacheDir(), fHistoryFileName);
        if (historyFile.exists()) historyFile.delete();
    }

    public void saveCalsWeight(Context aContext) {
        try {
            File file = new File(aContext.getCacheDir(), fDayFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(Double.toString(fTotalCalories) + "\n");
            outputStreamWriter.write(Double.toString(fWeight) + "\n");
            Log.d("User", "Writing day calories and weight to file: " + fTotalCalories + ", " + fWeight);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("User", "Day file write failed: " + e.toString());
        }
    }

    public void loadCaloriesWeight(Context aContext) {
        String line;
        File file = new File(aContext.getCacheDir(), fDayFileName);
        if (file.exists()) {
            try {
                Log.d("User", "Reading file " + fDayFileName);
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fileInputStream);
                BufferedReader br = new BufferedReader(isr);
                line = br.readLine();
                assert (line != null);
                Log.d("User", "Reading line: " + line);
                fTotalCalories = Double.parseDouble(line.toString());
                line = br.readLine();
                assert (line != null);
                Log.d("User", "Reading line: " + line);
                fWeight = Double.parseDouble(line.toString());
                isr.close();
            } catch (IOException e) {
                Log.d("User", "Could not read day file.");
            }
        }
    }
}
