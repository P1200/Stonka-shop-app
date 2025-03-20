package com.stonka.shopapp.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.stonka.shopapp.R;
import java.util.ArrayList;
import java.util.List;

public class HomeViewModel extends ViewModel {

    private final MutableLiveData<List<Product>> productList;

    public HomeViewModel() {
        productList = new MutableLiveData<>();
        loadProducts();
    }

    private void loadProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product("Laptop", 3499.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Telfon", 1599.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Słuchawki", 199.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Laptop", 3499.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Telfon", 1599.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Słuchawki", 199.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Laptop", 3499.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Telfon", 1599.99, R.drawable.ic_notifications_black_24dp));
        products.add(new Product("Słuchawki", 199.99, R.drawable.ic_notifications_black_24dp));
        productList.setValue(products);
    }

    public LiveData<List<Product>> getProductList() {
        return productList;
    }
}