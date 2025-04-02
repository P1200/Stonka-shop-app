package com.stonka.shopapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import android.widget.ArrayAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.List;

public class ShoppingListAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> shoppingList;
    private DatabaseReference databaseReference;

    public ShoppingListAdapter(Context context, List<String> shoppingList) {
        super(context, R.layout.activity_shopping_list, shoppingList);
        this.context = context;
        this.shoppingList = shoppingList;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            databaseReference = FirebaseDatabase.getInstance().getReference("shoppingLists").child(user.getUid());
        }
    }

    @NonNull
    @Override
    public View getView(int position, @NonNull View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.activity_shopping_list, parent, false);
        }

        TextView itemName = convertView.findViewById(R.id.itemName);
        Button deleteButton = convertView.findViewById(R.id.deleteButton);

        String item = shoppingList.get(position);
        itemName.setText(item);

        deleteButton.setOnClickListener(v -> confirmDelete(item));

        return convertView;
    }

    private void confirmDelete(String item) {
        new AlertDialog.Builder(context)
                .setTitle("Usuń produkt")
                .setMessage("Czy na pewno chcesz usunąć ten produkt z listy?")
                .setPositiveButton("Tak", (dialog, which) -> deleteItem(item))
                .setNegativeButton("Nie", null)
                .show();
    }

    private void deleteItem(String item) {
        if (databaseReference != null) {
            databaseReference.orderByChild("name").equalTo(item).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult().hasChildren()) {
                    for (DataSnapshot data : task.getResult().getChildren()) {
                        data.getRef().removeValue();
                    }
                    shoppingList.remove(item);
                    notifyDataSetChanged();
                    Toast.makeText(context, "Produkt usunięty!", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
