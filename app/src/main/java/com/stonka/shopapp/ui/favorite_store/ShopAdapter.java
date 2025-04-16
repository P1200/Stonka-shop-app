package com.stonka.shopapp.ui.favorite_store;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stonka.shopapp.R;

import java.util.List;

/**
 * Adapter do RecyclerView obsługujący listę sklepów (Shop).
 * Pozwala na wyświetlenie każdego sklepu w postaci prostego widoku.
 */
public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<Shop> shopList; // Lista sklepów do wyświetlenia
    private final OnItemClickListener listener; // Interfejs obsługujący kliknięcia

    // Konstruktor adaptera z przekazaniem listy sklepów i listenera kliknięcia
    public ShopAdapter(List<Shop> shopList, OnItemClickListener listener) {
        this.shopList = shopList;
        this.listener = listener;
    }

    // Tworzy nowy widok (ViewHolder) dla elementu listy
    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tworzymy widok na podstawie layoutu item_shop.xml
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    // Wypełnia dane widoku dla danego elementu listy
    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position); // Pobranie sklepu z listy
        holder.bind(shop); // Przypisanie danych do widoku
    }

    // Zwraca ilość elementów w liście
    @Override
    public int getItemCount() {
        return shopList.size();
    }

    /**
     * Interfejs do obsługi kliknięcia w element listy.
     */
    public interface OnItemClickListener {
        void onItemClick(Shop shop); // Wywoływane po kliknięciu w element sklepu
    }

    /**
     * ViewHolder reprezentujący pojedynczy element listy (sklep).
     */
    class ShopViewHolder extends RecyclerView.ViewHolder {
        private final TextView shopName; // Referencja do pola tekstowego z nazwą sklepu

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shop_name);

            // Ustawienie nasłuchiwania kliknięcia – przekazuje wybrany sklep do listenera
            itemView.setOnClickListener(v -> listener.onItemClick(shopList.get(getAdapterPosition())));
        }

        // Przypisanie danych sklepu do widoku
        public void bind(Shop shop) {
            shopName.setText(shop.getName()); // Ustawienie tekstu z nazwą sklepu
        }
    }
}
