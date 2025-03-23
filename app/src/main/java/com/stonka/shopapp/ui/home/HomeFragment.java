package com.stonka.shopapp.ui.home;
import com.stonka.shopapp.databinding.FragmentHomeBinding;
import android.graphics.pdf.PdfDocument;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.stonka.shopapp.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PAGE_SIZE = 5;
    private static final String TAG = "HomeFragment"; // Ustal TAG dla logów
    private final List<Product> productList = new ArrayList<>();
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    private String lastKey = null;
    private DatabaseReference databaseReference;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        // Dodajemy logi przy kliknięciu przycisku
        binding.btnDownloadPdf.setOnClickListener(v -> {
            Log.d(TAG, "Przycisk 'Pobierz PDF' kliknięty");
            generateProductCatalogPDF();
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager == null) {
                    Log.e("Null-pointer", "Layout manager is null");
                    return;
                }

                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreProducts();
                    }
                }
            }
        });

        loadMoreProducts(); //Load first page

        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;
        isLoading = true;
        databaseReference =
                FirebaseDatabase.getInstance("https://stonka-shop-app-default-rtdb.europe-west1.firebasedatabase.app/")
                        .getReference("products");

        Query query;
        if (lastKey == null) {
            query = databaseReference.orderByKey().limitToFirst(PAGE_SIZE);
        } else {
            query = databaseReference.orderByKey().startAfter(lastKey).limitToFirst(PAGE_SIZE);
        }

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Product> newProducts = new ArrayList<>();
                String newLastKey = null;

                for (DataSnapshot productSnapshot : snapshot.getChildren()) {
                    Product product = productSnapshot.getValue(Product.class);
                    if (product != null) {
                        newProducts.add(product);
                        newLastKey = productSnapshot.getKey();
                    }
                }

                if (newProducts.size() < PAGE_SIZE) {
                    isLastPage = true;
                }

                lastKey = newLastKey;
                productList.addAll(newProducts);
                adapter.notifyDataSetChanged(); //Update adapter

                isLoading = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Download data error: " + error.getMessage());
                isLoading = false;
            }
        });
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
                @Override
                public void onActivityResult(Boolean isGranted) {
                    if (isGranted) {
                        // Użytkownik przyznał uprawnienia
                        Log.d(TAG, "Uprawnienia przyznane.");
                        generateProductCatalogPDF();  // Uruchom generowanie PDF
                    } else {
                        // Użytkownik odmówił uprawnień
                        Log.d(TAG, "Uprawnienia odrzucone.");
                        Toast.makeText(getContext(), "Nie masz uprawnień do zapisu na dysku.", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void generateProductCatalogPDF() {
        Log.d(TAG, "Rozpoczęcie generowania PDF");

        // Sprawdzenie, czy uprawnienie jest przyznane
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            Log.d(TAG, "Brak uprawnień do zapisu, żądanie uprawnień");
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            return;
        }

        Log.d(TAG, "Uprawnienia do zapisu zostały przyznane");

        // Rozpoczynanie generowania PDF
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 400, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 20;
        paint.setTextSize(12);

        // Wypełnianie PDF listą produktów
        for (Product product : productList) {
            canvas.drawText("Produkt: " + product.getName(), 10, y, paint);
            canvas.drawText("Cena: " + product.getPrice() + " PLN", 10, y + 15, paint);
            y += 40;
            if (y > 380) break;  // W przypadku za dużej liczby produktów, zaczynaj nową stronę
        }

        pdfDocument.finishPage(page);

        // Zapis do pliku PDF
        File pdfFile = new File(requireContext().getExternalFilesDir(null), "gazetka1.pdf");
        try {
            pdfDocument.writeTo(new FileOutputStream(pdfFile));
            Log.d(TAG, "PDF zapisany: " + pdfFile.getAbsolutePath()); // Log po zapisaniu PDF
            Toast.makeText(getContext(), "PDF zapisany: " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, "Błąd zapisu PDF: " + e.getMessage()); // Log w przypadku błędu zapisu
            Toast.makeText(getContext(), "Błąd zapisu PDF!", Toast.LENGTH_SHORT).show();
        }
        pdfDocument.close();
    }
}