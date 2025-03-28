package com.stonka.shopapp.ui;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.stonka.shopapp.R;
import java.util.Random;

public class ShakeomatActivity extends AppCompatActivity {

    private TextView resultTextView;
    private String[] rewards = {"10% rabatu!", "Darmowa dostawa!", "20% zniżki na kolejne zakupy!", "Bonusowy produkt!"};
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shakeomat);

        resultTextView = findViewById(R.id.resultTextView);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");

        String reward = rewards[new Random().nextInt(rewards.length)];
        resultTextView.setText("Twoja nagroda: " + reward);

        saveRewardToDatabase(reward);
    }

    private void saveRewardToDatabase(String reward) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            DatabaseReference userRewardsRef = databaseReference.child(userId).child("rewards");

            String rewardId = userRewardsRef.push().getKey();
            if (rewardId != null) {
                userRewardsRef.child(rewardId).setValue(reward)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(this, "Nagroda zapisana!", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "Błąd zapisu nagrody!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        } else {
            Toast.makeText(this, "Musisz być zalogowany, aby otrzymać nagrodę!", Toast.LENGTH_SHORT).show();
        }
    }
}
