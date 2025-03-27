package com.stonka.shopapp.ui.favorite_store;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.stonka.shopapp.R;

import java.util.List;

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private final List<Shop> shopList;
    private final OnItemClickListener listener;

    public ShopAdapter(List<Shop> shopList, OnItemClickListener listener) {
        this.shopList = shopList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        Shop shop = shopList.get(position);
        holder.bind(shop);
    }

    @Override
    public int getItemCount() {
        return shopList.size();
    }

    public interface OnItemClickListener {
        void onItemClick(Shop shop);
    }

    class ShopViewHolder extends RecyclerView.ViewHolder {
        private final TextView shopName;

        public ShopViewHolder(View itemView) {
            super(itemView);
            shopName = itemView.findViewById(R.id.shop_name);
            itemView.setOnClickListener(v -> listener.onItemClick(shopList.get(getAdapterPosition())));
        }

        public void bind(Shop shop) {
            shopName.setText(shop.getName());
        }
    }
}
