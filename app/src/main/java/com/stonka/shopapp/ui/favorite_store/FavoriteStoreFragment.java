package com.stonka.shopapp.ui.favorite_store;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.stonka.shopapp.R;
import com.stonka.shopapp.databinding.FragmentFavoriteStoreBinding;

import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.List;

public class FavoriteStoreFragment extends Fragment {

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;
    private final List<Shop> shopList = new ArrayList<>();
    private FragmentFavoriteStoreBinding binding;
    private MapView mapView;
    private boolean isPositionInitialized;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private ShopAdapter shopAdapter;
    private GeoPoint goal;
    private MyLocationNewOverlay locationOverlay;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentFavoriteStoreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = root.findViewById(R.id.recycler_view_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        databaseReference = FirebaseDatabase.getInstance()
                                            .getReference("shops");
        loadShopsFromFirebase();

        root.findViewById(R.id.button_show_shops)
            .setOnClickListener(v -> recyclerView.setVisibility(View.VISIBLE));

        locationOverlay = configureMap(root);

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        isPositionInitialized = false;
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GeoPoint currentLocation = locationOverlay.getMyLocation();

                if (currentLocation != null) {
                    if (!isPositionInitialized) {
                        mapView.getController().setCenter(currentLocation);
                        isPositionInitialized = true;
                    }

                    if (goal != null) {
                        getRoute(currentLocation, goal);
                    }
                }
                // Run every one second
                new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        GeoPoint currentLocation = locationOverlay.getMyLocation();

        if (currentLocation != null) {
            mapView.getController().setCenter(currentLocation);
        }
        Shop shop = getSavedCoordinates();
        if (shop != null) {
            goal = new GeoPoint(shop.getLatitude(), shop.getLongitude());
            ((TextView) requireActivity().findViewById(R.id.shop_name)).setText(shop.getName());
        }
    }

    @NonNull
    private MyLocationNewOverlay configureMap(View root) {
        // osmdroid configuration
        Configuration.getInstance()
                        .load(root.getContext(), requireActivity().getPreferences(MODE_PRIVATE));

        mapView = root.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel(150.0);
        mapView.getController().setZoom(15.0);

        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        addCompass();

        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);
        return locationOverlay;
    }

    private void addCompass() {
        CompassOverlay compassOverlay =
                new CompassOverlay(requireContext(), new InternalCompassOrientationProvider(requireContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, REQUEST_PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void getRoute(GeoPoint start, GeoPoint end) {
        RoadManager roadManager = new OSRMRoadManager(getContext(), "MyAppUserAgent");

        new Thread(() -> {
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(start);
            waypoints.add(end);

            Road road = roadManager.getRoad(waypoints);

            // Update UI
            requireActivity().runOnUiThread(() -> {
                if (road.mStatus != Road.STATUS_OK) {
                    Toast.makeText(getContext(), "Błąd pobierania trasy", Toast.LENGTH_SHORT).show();
                    return;
                }
                mapView.getOverlays().removeIf(overlay -> overlay instanceof Polyline);

                // Draw path
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                roadOverlay.setWidth(10);
                roadOverlay.setColor(Color.BLUE);
                mapView.getOverlays().add(roadOverlay);
                mapView.invalidate();
            });
        }).start();
    }

    private void loadShopsFromFirebase() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                shopList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Shop shop = snapshot.getValue(Shop.class);
                    if (shop != null) {
                        shopList.add(shop);
                    }
                }
                updateRecyclerView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Błąd pobierania danych", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRecyclerView() {
        if (shopAdapter == null) {
            shopAdapter = new ShopAdapter(shopList, shop -> {
                // Hide list after item click
                recyclerView.setVisibility(View.GONE);
                goal = new GeoPoint(shop.getLatitude(), shop.getLongitude());
                ((TextView) requireActivity().findViewById(R.id.shop_name)).setText(shop.getName());
                saveShop(goal, shop.getName());
                isPositionInitialized = false;
            });
            recyclerView.setAdapter(shopAdapter);
        } else {
            shopAdapter.notifyDataSetChanged();
        }
    }

    private void saveShop(GeoPoint point, String name) {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("latitude", (float) point.getLatitude());
        editor.putFloat("longitude", (float) point.getLongitude());
        editor.putString("name", name);
        editor.apply();
    }

    private Shop getSavedCoordinates() {
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        float latitude = prefs.getFloat("latitude", 0);
        float longitude = prefs.getFloat("longitude", 0);
        String name = prefs.getString("name", null);

        if (latitude == 0 || longitude == 0 || name == null) {
            return null; // No coordinates saved
        }
        return new Shop(name, latitude, longitude);
    }
}