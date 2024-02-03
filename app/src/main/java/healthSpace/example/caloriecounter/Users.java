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

public class Users {
    ArrayList<User> fUsers = new ArrayList<User>();
    private static String fLocalUsersFileName = "users_local.txt";
    private static String fUsersFileHeader = "Users\n";

    protected void finalize() throws Throwable
    {
        fUsers.clear();
        fUsers = null;
        fLocalUsersFileName = null;
        fUsersFileHeader = null;
    }

    public void addUser(String aUser) {
        fUsers.add(new User(aUser));
    }

    public boolean addCalories(String aUser, double aCals) throws ClassNotFoundException {
        if (aCals < 0.0) return false;
        User currUser = getUser(aUser);
        double totalCals = currUser.getCalories() + aCals;
        currUser.setCalories(Math.round(totalCals * 100.0) / 100.0);
        return true;
    }

    public double getCalories(String aUser) throws ClassNotFoundException {
        User currUser = getUser(aUser);
        return currUser.getCalories();
    }

    public void clearUserCalories(String aUser) {
        try {
            User currUser = getUser(aUser);
            currUser.clearCalories();
        } catch (ClassNotFoundException e) {
            Log.d("Users", "Could not clear calories for user " + aUser);
        }
    }

    private int getUserIdx(String aUser) throws ClassNotFoundException {
        for (int i = 0; i < fUsers.size(); i++) {
            if (fUsers.get(i).getName().equalsIgnoreCase(aUser)) {
                return i;
            }
        }
        throw new ClassNotFoundException("User not found " + aUser);
    }

    private User getUser(String aUser) throws ClassNotFoundException {
        int userIdx = getUserIdx(aUser);
        return fUsers.get(userIdx);
    }

    public List<String> getUserList() {
        List<String> retStrings = new ArrayList<String>();
        fUsers.forEach(user -> retStrings.add(user.getName()));
        return retStrings;
    }

    public String[] getUsers() {
        String[] retStr = new String[fUsers.size()];
        for (int i = 0; i < fUsers.size(); i++) {
            retStr[i] = fUsers.get(i).getName();
        }
        return retStr;
    }

    public String getUsersString() {
        String retStr = fUsersFileHeader;
        for (int i = 0; i < fUsers.size(); i++) {
            retStr += fUsers.get(i).getName() + "\n";
        }
        return retStr;
    }

    public void clearCache(Context aContext) {
        File caloriesFile = new File(aContext.getCacheDir(), fLocalUsersFileName);
        if (caloriesFile.exists()) caloriesFile.delete();
        Log.d("Users", "Cleared cache");
    }

    public void writeUsers(Context aContext) throws IOException {
        try {
            File file = new File(aContext.getCacheDir(), fLocalUsersFileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(fUsersFileHeader);
            for (int i = 0; i < fUsers.size(); i++) {
                outputStreamWriter.write(fUsers.get(i).getName() + "\n");
                Log.d("Users", String.format("Writing to file %s: %s", i, fUsers.get(i).getName()));
            }
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Users", "File write failed: " + e.toString());
        }
    }

    private void readUsersLines(Context context, InputStreamReader aIsr) throws IOException, ClassNotFoundException {
        BufferedReader br = new BufferedReader(aIsr);
        String line;
        String firstLine = br.readLine();
        Log.d("Users", "Reading line: " + firstLine);
        while ((line = br.readLine()) != null) {
            Log.d("Users", "Reading line: " + line);
            addUser(line);
        }
    }

    public void readUsers(Context aContext) throws IOException, ClassNotFoundException {
        fUsers.clear();
        boolean fileExists = false;
        int apkFileIdx = R.raw.users_csv;
        try {
            File file = new File(aContext.getCacheDir(), fLocalUsersFileName);
            if (file.exists()) {
                Log.d("Users", "Reading file " + fLocalUsersFileName);
                FileInputStream fileInputStream = new FileInputStream(file);
                InputStreamReader isr = new InputStreamReader(fileInputStream);
                readUsersLines(aContext, isr);
                isr.close();
                fileInputStream.close();
                fileExists = true;
            }
        } catch (Exception ex) {
            Log.d("Users", "Could not read cache file for reading users");
        }
        if (!fileExists) {
            Log.d("Users", "Reading file " + apkFileIdx);
            InputStream is = aContext.getResources().openRawResource(apkFileIdx);
            InputStreamReader isr = new InputStreamReader(is);
            readUsersLines(aContext, isr);
            isr.close();
        }
    }

    public double getWeight(String aUser) throws ClassNotFoundException {
        return getUser(aUser).getWeight();
    }

    public void setWeight(String aUser, double aWeight) throws ClassNotFoundException {
        getUser(aUser).setWeight(aWeight);
    }

    public void endDayForUser(Context aContext, String aUser) throws IOException, ClassNotFoundException {
        getUser(aUser).appendHistoryLine(aContext);
    }

    public String getUserHistory(Context aContext, String aUser) throws IOException, ClassNotFoundException {
        return getUser(aUser).readUserHistoryFile(aContext);
    }

    public void deleteHistoryFile(Context aContext, String aUser) throws ClassNotFoundException {
        getUser(aUser).deleteHistoryFile(aContext);
    }

    public void saveUserDayCaloriesWeight(Context aContext) {
        fUsers.forEach(user -> user.saveCalsWeight(aContext));
    }

    public void loadUserDayCaloriesWeight(Context aContext) {
        fUsers.forEach(user -> user.loadCaloriesWeight(aContext));
    }
}