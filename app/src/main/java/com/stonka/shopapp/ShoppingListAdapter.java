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

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ShoppingViewHolder> {

    private final AppCompatActivity appCompatActivity;
    private final List<ShoppingListItem> shoppingList;
    private DatabaseReference databaseReference;

    public ShoppingListAdapter(List<ShoppingListItem> shoppingList, Context context) {
        this.shoppingList = shoppingList;
        this.appCompatActivity = (AppCompatActivity) context;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference =
                    FirebaseDatabase.getInstance()
                                    .getReference("users/" + user.getUid() + "/shoppingLists");
        }
    }

    @NonNull
    @Override
    public ShoppingListAdapter.ShoppingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.shopping_list_item, parent, false);
        return new ShoppingListAdapter.ShoppingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingViewHolder holder, int position) {
        ShoppingListItem shoppingListItem = shoppingList.get(position);
        holder.nameTextView.setText(shoppingListItem.getName());
        holder.deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(position, shoppingListItem.getId()));
    }

    @Override
    public int getItemCount() {
        return shoppingList.size();
    }

    private void showDeleteConfirmationDialog(int position, String itemId) {
        new AlertDialog.Builder(appCompatActivity)
                .setTitle("Usuń produkt")
                .setMessage("Czy na pewno chcesz usunąć ten produkt z listy?")
                .setPositiveButton("Tak", (dialog, which) -> removeItem(position, itemId))
                .setNegativeButton("Anuluj", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void removeItem(int position, String itemId) {
        if (itemId != null) {
            databaseReference.child(itemId).removeValue();
        }
        shoppingList.remove(position);
        notifyItemRemoved(position);
    }

    public static class ShoppingViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        Button deleteButton;

        public ShoppingViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.shopping_item_name);
            deleteButton = itemView.findViewById(R.id.removeShoppingListItem);
        }
    }
}
