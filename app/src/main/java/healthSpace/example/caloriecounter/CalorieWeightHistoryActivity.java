package healthSpace.example.caloriecounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import healthSpace.example.caloriecounter.R;

import java.util.List;

public class CalorieWeightHistoryActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    Users fUsers = new Users();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calorie_weight_history);
        try {
            fUsers.readUsers(this);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CalorieWeightHistoryActivity", "Could not read users:\n" + e.getMessage());
        }
        populateSpinners();
        populateHistory();
    }

    private void populateSpinners() {
        Spinner sp1 = findViewById(R.id.userList);
        List<String> userList = fUsers.getUserList();
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                userList);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sp1.setAdapter(adapter1);
        sp1.setOnItemSelectedListener(this);
    }

    private void populateHistory() {
        String currUser = getSelectedUser();
        String userHistory = "";
        try {
            userHistory = fUsers.getUserHistory(this, currUser);
            Log.d("CalorieWeightHistoryActivity", "Read user history: " + userHistory);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("CalorieWeightHistoryActivity", "Could not read user history:\n" + e.getMessage());
        }
        TextView tv1 = findViewById(R.id.textHistory);
        tv1.setText(userHistory);
    }
   
    public void onGoHome(View aView) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public void onDeleteFile(View aView) {
        String currUser = getSelectedUser();
        try {
            fUsers.deleteHistoryFile(this, currUser);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Log.d("CalorieWeightHistoryActivity", "Could not delete file for user " + currUser);
        }
        populateHistory();
    }

    public String getSelectedUser() {
        AdapterView av1 = findViewById(R.id.userList);
        return av1.getSelectedItem().toString();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        populateHistory();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}