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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final List<Product> productList;

    public ProductAdapter(List<Product> productList) {
        this.productList = productList;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.nameTextView.setText(product.getName());
        holder.priceTextView.setText(String.format("%.2f zł", product.getPrice()));
        holder.productImageView.setImageResource(R.drawable.baseline_crop_original_24); // mock

        // 🔷 Generowanie danych do QR
        String qrData = "Produkt: " + product.getName() + "\nCena: " + product.getPrice() + " zł";

        // 🔷 Generowanie kodu QR jako bitmapy
        Bitmap qrBitmap = QRCode.from(qrData).withSize(200, 200).bitmap();

        // 🔷 Ustawienie bitmapy w ImageView
        holder.qrCodeImageView.setImageBitmap(qrBitmap);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, priceTextView;
        ImageView productImageView, qrCodeImageView;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.product_name);
            priceTextView = itemView.findViewById(R.id.product_price);
            productImageView = itemView.findViewById(R.id.product_image);
            qrCodeImageView = itemView.findViewById(R.id.qr_code_image); // dodajemy QR
        }
    }
}
