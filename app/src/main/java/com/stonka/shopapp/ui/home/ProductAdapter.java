package com.stonka.shopapp.ui.home;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stonka.shopapp.R;

import net.glxn.qrgen.android.QRCode;

import java.util.List;

/**
 * Adapter dla widoku RecyclerView wyświetlającego listę produktów.
 * Umożliwia wyświetlanie produktów w postaci nazwy, ceny, obrazu oraz kodu QR.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList; // Lista produktów do wyświetlenia

    /**
     * Konstruktor, który przyjmuje listę produktów do wyświetlenia w RecyclerView.
     *
     * @param productList Lista produktów.
     */
    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    /**
     * Tworzy nowy widok dla pojedynczego elementu listy produktów.
     *
     * @param parent Widok rodzica, w którym będzie wyświetlany element.
     * @param viewType Typ widoku.
     * @return Nowo utworzony obiekt ViewHolder dla produktu.
     */
    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflacja layoutu dla pojedynczego elementu listy
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    /**
     * Ustawia dane w widoku dla określonego elementu w liście.
     *
     * @param holder   Obiekt ViewHolder zawierający widoki dla pojedynczego elementu.
     * @param position Pozycja elementu w liście.
     */
    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        // Ustawienie nazwy i ceny produktu w odpowiednich widokach tekstowych
        holder.nameTextView.setText(product.getName());
        holder.priceTextView.setText(String.format("%.2f zł", product.getPrice()));

        // 🔷 Ustawienie obrazu produktu - używamy tutaj domyślnego obrazu (można zastąpić)
        holder.productImageView.setImageResource(R.drawable.baseline_crop_original_24); // mock

        // 🔷 Generowanie danych do QR
        String qrData = "Produkt: " + product.getName() + "\nCena: " + product.getPrice() + " zł";

        // 🔷 Generowanie kodu QR jako bitmapy
        Bitmap qrBitmap = QRCode.from(qrData).withSize(200, 200).bitmap();

        // 🔷 Ustawienie bitmapy w ImageView dla kodu QR
        holder.qrCodeImageView.setImageBitmap(qrBitmap);
    }

    /**
     * Zwraca liczbę elementów w liście produktów.
     *
     * @return Liczba produktów.
     */
    @Override
    public int getItemCount() {
        return productList.size();
    }

    /**
     * ViewHolder dla pojedynczego elementu listy produktów.
     * Zawiera widoki dla nazwy produktu, ceny, obrazu produktu i kodu QR.
     */
    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView; // Widoki dla nazwy i ceny produktu
        ImageView productImageView, qrCodeImageView; // Widok obrazu produktu i kodu QR

        /**
         * Konstruktor ViewHolder.
         * Inicjalizuje widoki znajdujące się w layout'cie elementu.
         *
         * @param itemView Widok pojedynczego elementu.
         */
        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            productImageView = itemView.findViewById(R.id.product_image);
            qrCodeImageView = itemView.findViewById(R.id.qr_code_image); // Dodajemy widok QR
        }
    }
}
