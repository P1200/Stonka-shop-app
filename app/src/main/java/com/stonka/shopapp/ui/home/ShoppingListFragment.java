package com.stonka.shopapp.ui.home;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.stonka.shopapp.databinding.FragmentShoppingListBinding;
import java.util.ArrayList;
import java.util.List;

public class ShoppingListFragment extends Fragment {
    private FragmentShoppingListBinding binding;
    private ProductAdapter productAdapter;
    private List<Product> productList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentShoppingListBinding.inflate(inflater, container, false);

        productList = new ArrayList<>();
        productAdapter = new ProductAdapter(productList);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerView.setAdapter(productAdapter);

        binding.addProductButton.setOnClickListener(v -> addProduct());

        return binding.getRoot();
    }

    private void addProduct() {
        String name = binding.productNameInput.getText().toString().trim();
        String priceStr = binding.productPriceInput.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Podaj nazwę i cenę produktu!", Toast.LENGTH_SHORT).show();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Niepoprawna cena!", Toast.LENGTH_SHORT).show();
            return;
        }

        Product product = new Product(name, price, ""); // Na razie brak obrazka
        productList.add(product);
        productAdapter.notifyItemInserted(productList.size() - 1);

        binding.productNameInput.setText("");
        binding.productPriceInput.setText("");
    }
}