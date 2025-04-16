package com.stonka.shopapp;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

/**
 * Aktywność odpowiedzialna za zarządzanie listą zakupów użytkownika.
 * Pozwala na dodawanie przedmiotów do listy i ich wyświetlanie.
 */
public class ShoppingListActivity extends AppCompatActivity {

    // Lista przechowująca przedmioty na liście zakupów
    private final List<ShoppingListItem> shoppingList = new ArrayList<>();

    private EditText itemInput; // Pole do wprowadzania nazwy przedmiotu
    private Button addButton; // Przycisk do dodawania przedmiotu
    private RecyclerView shoppingListView; // Widok listy zakupów
    private ShoppingListAdapter adapter; // Adapter do wyświetlania listy zakupów
    private DatabaseReference databaseReference; // Referencja do bazy danych Firebase
    private FirebaseAuth mAuth; // Autentykacja użytkownika

    /**
     * Tworzy widok aktywności i inicjalizuje komponenty.
     *
     * @param savedInstanceState Stan zapisany aktywności z poprzednich cykli życia.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_list);

        // Umożliwia powrót do poprzedniego ekranu
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Inicjalizacja widoków
        itemInput = findViewById(R.id.itemInput);
        addButton = findViewById(R.id.addButton);
        shoppingListView = findViewById(R.id.shoppingListView);

        // Konfiguracja adaptera dla RecyclerView
        adapter = new ShoppingListAdapter(shoppingList, this);
        shoppingListView.setLayoutManager(new LinearLayoutManager(this));
        shoppingListView.setAdapter(adapter);

        // Inicjalizacja FirebaseAuth i uzyskanie użytkownika
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        // Jeśli użytkownik jest zalogowany, załaduj listę zakupów
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance()
                    .getReference("users/" + user.getUid() + "/shoppingLists");
            loadShoppingList();
        } else {
            // Jeśli użytkownik nie jest zalogowany, wyświetl komunikat
            Toast.makeText(this, "Musisz być zalogowany, aby używać listy zakupów!", Toast.LENGTH_SHORT).show();
        }

        // Ustawienie akcji przycisku dodawania przedmiotu
        addButton.setOnClickListener(v -> addItem());
    }

    /**
     * Obsługuje kliknięcie w element opcji menu (np. przycisk powrotu).
     *
     * @param item Element menu, który został wybrany.
     * @return Zwraca true, jeśli element menu został obsłużony, false w przeciwnym przypadku.
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // Kończy aktywność (powrót do poprzedniego ekranu)
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Dodaje nowy przedmiot do listy zakupów.
     * Jeśli nazwa przedmiotu nie jest pusta, zapisuje go w bazie danych Firebase.
     */
    private void addItem() {
        String item = itemInput.getText().toString().trim();

        // Sprawdzenie, czy pole wejściowe nie jest puste
        if (!item.isEmpty()) {
            // Generowanie unikalnego klucza dla nowego przedmiotu
            String key = databaseReference.push().getKey();
            if (key != null) {
                Map<String, Object> newItem = new HashMap<>();
                newItem.put("id", key);
                newItem.put("name", item);

                // Zapisanie nowego przedmiotu w bazie danych Firebase
                databaseReference.child(key).setValue(newItem);

                // Wyczyść pole wejściowe po dodaniu przedmiotu
                itemInput.setText("");
            }
        } else {
            // Wyświetlenie komunikatu, jeśli pole jest puste
            Toast.makeText(this, "Wprowadź nazwę produktu!", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ładuje listę zakupów z bazy danych Firebase i aktualizuje widok.
     */
    private void loadShoppingList() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoppingList.clear(); // Czyszczenie bieżącej listy

                // Iteracja przez dane w bazie danych i dodanie elementów do listy
                for (DataSnapshot data : snapshot.getChildren()) {
                    String item = data.child("name").getValue(String.class);
                    String itemId = data.child("id").getValue(String.class);

                    if (item != null) {
                        ShoppingListItem shoppingListItem = new ShoppingListItem(item, itemId);
                        shoppingList.add(shoppingListItem); // Dodanie elementu do listy
                    }
                }

                // Powiadomienie adaptera o zmianach
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Obsługa błędów, jeśli dane nie zostaną załadowane
                Toast.makeText(ShoppingListActivity.this, "Błąd wczytywania danych.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
