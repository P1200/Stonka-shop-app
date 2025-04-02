package com.stonka.shopapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingListActivity extends AppCompatActivity {

    private EditText itemInput;
    private Button addButton;
    private ListView shoppingListView;
    private List<String> shoppingList;
    private ShoppingListAdapter adapter;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        itemInput = findViewById(R.id.itemInput);
        addButton = findViewById(R.id.addButton);
        shoppingListView = findViewById(R.id.shoppingListView);

        shoppingList = new ArrayList<>();
        adapter = new ShoppingListAdapter(this, shoppingList);
        shoppingListView.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("shoppingLists").child(user.getUid());
            loadShoppingList();
        } else {
            Toast.makeText(this, "Musisz być zalogowany, aby używać listy zakupów!", Toast.LENGTH_SHORT).show();
        }

        addButton.setOnClickListener(v -> addItem());
    }

    private void addItem() {
        String item = itemInput.getText().toString().trim();
        if (!item.isEmpty()) {
            String key = databaseReference.push().getKey();
            if (key != null) {
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("id", key);
                newItem.put("name", item);
                databaseReference.child(key).setValue(newItem);
                itemInput.setText("");
            }
        } else {
            Toast.makeText(this, "Wprowadź nazwę produktu!", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadShoppingList() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    String item = data.child("name").getValue(String.class);
                    if (item != null) {
                        shoppingList.add(item);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ShoppingListActivity.this, "Błąd wczytywania danych.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
