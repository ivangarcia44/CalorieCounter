package healthSpace.example.caloriecounter;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import healthSpace.example.caloriecounter.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onCountCalories(View aView) {
        Intent i = new Intent(this, CalorieCounterActivity.class);
        startActivity(i);
    }

    public void onCalWeightHistory(View aView) {
        Intent i = new Intent(this, CalorieWeightHistoryActivity.class);
        startActivity(i);
    }
}