package com.stonka.shopapp.ui.home;
import com.stonka.shopapp.ShoppingListActivity;
import com.stonka.shopapp.databinding.FragmentHomeBinding;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private static final int PAGE_SIZE = 5;
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

        binding.btnDownloadPdf.setOnClickListener(v -> generateProductCatalogPDF());

        binding.btnShoppingList.setOnClickListener(e -> {
            Intent intent = new Intent(requireActivity(), ShoppingListActivity.class);
            startActivity(intent);
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
        databaseReference = FirebaseDatabase.getInstance()
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

    private void generateProductCatalogPDF() {

        PdfDocument pdfDocument = getPdfDocument();

        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "gazetka.pdf");
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = requireContext().getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        }

        try {
            OutputStream outputStream = requireContext().getContentResolver().openOutputStream(uri);
            pdfDocument.writeTo(outputStream);
            outputStream.close();

            Toast.makeText(getContext(), "PDF zapisany w Download", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Błąd zapisu PDF!", Toast.LENGTH_SHORT).show();
        }

        pdfDocument.close();
    }

    @NonNull
    private PdfDocument getPdfDocument() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 400, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        int y = 20;
        paint.setTextSize(12);

        for (Product product : productList) {
            canvas.drawText("Produkt: " + product.getName(), 10, y, paint);
            canvas.drawText("Cena: " + product.getPrice() + " PLN", 10, y + 15, paint);
            y += 40;
            if (y > 380) break;
        }

        pdfDocument.finishPage(page);
        return pdfDocument;
    }
}