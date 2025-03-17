package com.stonka.shopapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class ShakeomatActivity extends AppCompatActivity {

    private TextView resultTextView;
    private String[] rewards = {"10% rabatu!", "Darmowa dostawa!", "20% zniżki na kolejne zakupy!", "Bonusowy produkt!"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shakeomat);

        resultTextView = findViewById(R.id.resultTextView);

        // Losowanie nagrody po potrząśnięciu
        String reward = rewards[new Random().nextInt(rewards.length)];
        resultTextView.setText("Twoja nagroda: " + reward);
    }
}
