package com.stonka.shopapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

/**
 * Adapter do wyświetlania przedmiotów na liście zakupów w RecyclerView.
 * Umożliwia wyświetlanie przedmiotów i usuwanie ich z listy.
 */
public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingViewHolder> {

    private final AppCompatActivity appCompatActivity; // Aktywność, która wywołuje adapter
    private final List<ShoppingListItem> shoppingList; // Lista przedmiotów na liście zakupów
    private DatabaseReference databaseReference; // Referencja do bazy danych Firebase

    /**
     * Konstruktor adaptera.
     *
     * @param shoppingList Lista przedmiotów do wyświetlenia.
     * @param context      Kontekst aktywności.
     */
    public ShoppingListAdapter(List<ShoppingListItem> shoppingList, Context context) {
        this.shoppingList = shoppingList;
        this.appCompatActivity = (AppCompatActivity) context;

        // Inicjalizacja bazy danych Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference =
                    FirebaseDatabase.getInstance()
                            .getReference("users/" + user.getUid() + "/shoppingLists");
        }
    }

    /**
     * Tworzy nowy widok dla pojedynczego elementu na liście zakupów.
     *
     * @param parent   Widok nadrzędny, do którego dodać nowy widok.
     * @param viewType Typ widoku (nie używany w tym przypadku).
     * @return Nowy widok przedmiotu na liście zakupów.
     */
    @NonNull
    @Override
    public ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflatuje layout dla pojedynczego elementu listy
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingViewHolder(view);
    }

    /**
     * Ustawia dane dla widoku elementu listy.
     *
     * @param holder   Holder, który przechowuje widok dla elementu listy.
     * @param position Pozycja przedmiotu w liście.
     */
    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        // Pobiera przedmiot z listy i ustawia go w widoku
        ShoppingListItem shoppingListItem = shoppingList.get(position);
        holder.nameTextView.setText(shoppingListItem.getName());

        // Ustawia kliknięcie przycisku do usuwania przedmiotu
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(position, shoppingListItem.getId()));
    }

    /**
     * Zwraca liczbę przedmiotów na liście zakupów.
     *
     * @return Liczba przedmiotów w liście.
     */
    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    /**
     * Wyświetla okno dialogowe z potwierdzeniem usunięcia przedmiotu z listy.
     *
     * @param position Pozycja przedmiotu w liście.
     * @param itemId   ID przedmiotu do usunięcia.
     */
    private void showDeleteConfirmationDialog(int position, String itemId) {
        new AlertDialog.Builder(appCompatActivity)
                .setTitle("Usuń produkt")
                .setMessage("Czy na pewno chcesz usunąć ten produkt z listy?")
                .setPositiveButton("Tak", (dialog, which) -> removeItem(position, itemId))
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Usuwa przedmiot z bazy danych i z listy.
     *
     * @param position Pozycja przedmiotu w liście.
     * @param itemId   ID przedmiotu do usunięcia.
     */
    private void removeItem(int position, String itemId) {
        if (itemId != null) {
            // Usuwa przedmiot z bazy danych Firebase
            databaseReference.child(itemId).removeValue();
        }
        // Usuwa przedmiot z lokalnej listy
        shoppingList.remove(position);
        // Powiadamia adapter o usunięciu przedmiotu
        notifyItemRemoved(position);
    }

    /**
     * ViewHolder dla pojedynczego przedmiotu na liście zakupów.
     */
    public static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView; // TextView wyświetlający nazwę przedmiotu
        Button deleteButton;   // Przycisk do usuwania przedmiotu

        /**
         * Konstruktor ViewHoldera.
         *
         * @param itemView Widok pojedynczego przedmiotu.
         */
        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.shopping_item_name);
            deleteButton = itemView.findViewById(R.id.removeShoppingListItem);
        }
    }
}
