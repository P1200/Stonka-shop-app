package com.stonka.shopapp.ui.home;

import android.app.AlertDialog;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import com.journeyapps.barcodescanner.CaptureActivity;
import com.stonka.shopapp.ShoppingListActivity;
import com.stonka.shopapp.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment startowy aplikacji — wyświetla listę produktów, umożliwia skanowanie kodów QR
 * oraz generowanie katalogu w PDF.
 */
public class HomeFragment extends Fragment {

    private static final int PAGE_SIZE = 5; // Ilość produktów ładowanych na jedną stronę (paginacja)

    private final List<Product> productList = new ArrayList<>(); // Lista produktów
    private FragmentHomeBinding binding;
    private RecyclerView recyclerView;
    private ProductAdapter adapter;

    // Zmienne do zarządzania paginacją
    private boolean isLoading = false;
    private boolean isLastPage = false;
    private String lastKey = null;

    private DatabaseReference databaseReference;
    private ActivityResultLauncher<Intent> qrScanLauncher; // Obsługa wyniku skanera QR

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Rejestracja efektu działania aktywności do skanowania QR
        qrScanLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null) {
                            String qrCode = data.getStringExtra("SCAN_RESULT");

                            // Pokazanie zawartości zeskanowanego kodu
                            if (isAdded() && getActivity() != null && !getActivity().isFinishing()) {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Informacje o produkcie")
                                        .setMessage(qrCode)
                                        .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                                        .show();
                            }
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Konfiguracja RecyclerView
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        // Przycisk generowania PDF
        binding.btnDownloadPdf.setOnClickListener(v -> generateProductCatalogPDF());

        // Przycisk przejścia do listy zakupów
        binding.btnShoppingList.setOnClickListener(e -> {
            Intent intent = new Intent(requireActivity(), ShoppingListActivity.class);
            startActivity(intent);
        });

        // Przycisk uruchomienia skanera QR
        binding.btnScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CaptureActivity.class);
            qrScanLauncher.launch(intent);
        });

        // Obsługa paginacji przy scrollowaniu
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

                // Sprawdzenie, czy załadować kolejną stronę
                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0) {
                        loadMoreProducts();
                    }
                }
            }
        });

        loadMoreProducts(); // Załaduj pierwszą stronę produktów
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        // Ukryj przycisk listy zakupów, jeśli użytkownik nie jest zalogowany
        if (mAuth.getCurrentUser() == null) {
            binding.btnShoppingList.setVisibility(View.GONE);
        } else {
            binding.btnShoppingList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Zapobiega wyciekom pamięci
    }

    /**
     * Ładowanie kolejnej partii produktów z Firebase (paginacja).
     */
    private void loadMoreProducts() {
        if (isLoading || isLastPage) return;
        isLoading = true;

        databaseReference = FirebaseDatabase.getInstance().getReference("products");

        Query query = (lastKey == null)
                ? databaseReference.orderByKey().limitToFirst(PAGE_SIZE)
                : databaseReference.orderByKey().startAfter(lastKey).limitToFirst(PAGE_SIZE);

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
                adapter.notifyDataSetChanged();
                isLoading = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Błąd pobierania danych: " + error.getMessage());
                isLoading = false;
            }
        });
    }

    /**
     * Generuje PDF z katalogiem produktów i zapisuje go do folderu Downloads.
     */
    private void generateProductCatalogPDF() {
        PdfDocument pdfDocument = getPdfDocument();

        // Przygotowanie metadanych pliku
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, "gazetka.pdf");
        values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = requireContext().getContentResolver()
                    .insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
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

    /**
     * Tworzy dokument PDF zawierający listę produktów.
     *
     * @return gotowy obiekt PdfDocument
     */
    @NonNull
    private PdfDocument getPdfDocument() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(300, 400, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(12);

        int y = 20; // Wysokość początkowa tekstu na stronie

        for (Product product : productList) {
            canvas.drawText("Produkt: " + product.getName(), 10, y, paint);
            canvas.drawText("Cena: " + product.getPrice() + " PLN", 10, y + 15, paint);
            y += 40;

            // Ograniczenie jednej strony do ok. 9 produktów
            if (y > 380) break;
        }

        pdfDocument.finishPage(page);
        return pdfDocument;
    }
}
