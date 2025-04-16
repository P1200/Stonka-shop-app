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

    // Stała do obsługi żądań uprawnień
    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    // Lista przechowująca sklepy
    private final List<Shop> shopList = new ArrayList<>();

    // Zmienne związane z widokiem i mapą
    private FragmentFavoriteStoreBinding binding;
    private MapView mapView;
    private boolean isPositionInitialized;
    private RecyclerView recyclerView;

    // Firebase – odwołanie do bazy danych
    private DatabaseReference databaseReference;
    private ShopAdapter shopAdapter;
    private GeoPoint goal; // cel podróży
    private MyLocationNewOverlay locationOverlay;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        // Inicjalizacja widoku z użyciem ViewBinding
        binding = FragmentFavoriteStoreBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Konfiguracja RecyclerView do listy sklepów
        recyclerView = root.findViewById(R.id.recycler_view_shops);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Pobranie odniesienia do "shops" w Firebase
        databaseReference = FirebaseDatabase.getInstance().getReference("shops");
        loadShopsFromFirebase(); // Pobranie danych sklepów z bazy

        // Ustawienie widoczności listy po kliknięciu przycisku
        root.findViewById(R.id.button_show_shops).setOnClickListener(v -> recyclerView.setVisibility(View.VISIBLE));

        // Konfiguracja mapy i lokalizacji użytkownika
        locationOverlay = configureMap(root);

        // Poproszenie o wymagane uprawnienia, jeśli nie zostały jeszcze udzielone
        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });

        isPositionInitialized = false;

        // Handler do cyklicznego sprawdzania lokalizacji użytkownika i aktualizacji mapy/trasy
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                GeoPoint currentLocation = locationOverlay.getMyLocation();

                if (currentLocation != null) {
                    if (!isPositionInitialized) {
                        mapView.getController().setCenter(currentLocation);
                        isPositionInitialized = true;
                    }

                    // Jeżeli jest ustawiony cel podróży, generujemy trasę
                    if (goal != null) {
                        getRoute(currentLocation, goal);
                    }
                }

                // Uruchomienie ponownie po 1 sekundzie
                new Handler(Looper.getMainLooper()).postDelayed(this, 1000);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Zwalnianie zasobów view bindingu
    }

    @Override
    public void onResume() {
        super.onResume();

        // Po wznowieniu: ustawienie centrum mapy na bieżącą lokalizację
        GeoPoint currentLocation = locationOverlay.getMyLocation();
        if (currentLocation != null) {
            mapView.getController().setCenter(currentLocation);
        }

        // Próba załadowania zapisanych wcześniej współrzędnych ulubionego sklepu
        Shop shop = getSavedCoordinates();
        if (shop != null) {
            goal = new GeoPoint(shop.getLatitude(), shop.getLongitude());
            ((TextView) requireActivity().findViewById(R.id.shop_name)).setText(shop.getName());
        }
    }

    @NonNull
    private MyLocationNewOverlay configureMap(View root) {
        // Konfiguracja biblioteki osmdroid
        Configuration.getInstance().load(root.getContext(), requireActivity().getPreferences(MODE_PRIVATE));

        // Ustawienia mapy
        mapView = root.findViewById(R.id.mapView);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setMaxZoomLevel(150.0);
        mapView.getController().setZoom(15.0);

        // Overlay lokalizacji użytkownika
        MyLocationNewOverlay locationOverlay = new MyLocationNewOverlay(mapView);
        locationOverlay.enableMyLocation();
        locationOverlay.enableFollowLocation();
        mapView.getOverlays().add(locationOverlay);

        addCompass(); // Dodanie kompasu

        // Dodanie skali
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        mapView.getOverlays().add(scaleBarOverlay);

        return locationOverlay;
    }

    private void addCompass() {
        // Dodaje overlay z kompasem do mapy
        CompassOverlay compassOverlay = new CompassOverlay(
                requireContext(), new InternalCompassOrientationProvider(requireContext()), mapView);
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        // Sprawdza i prosi o niezbędne uprawnienia
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(requireContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{permission}, REQUEST_PERMISSIONS_REQUEST_CODE);
            }
        }
    }

    private void getRoute(GeoPoint start, GeoPoint end) {
        // Używa OSRM do wygenerowania trasy
        RoadManager roadManager = new OSRMRoadManager(getContext(), "MyAppUserAgent");

        new Thread(() -> {
            ArrayList<GeoPoint> waypoints = new ArrayList<>();
            waypoints.add(start);
            waypoints.add(end);

            Road road = roadManager.getRoad(waypoints);

            // Aktualizacja UI na wątku głównym
            requireActivity().runOnUiThread(() -> {
                if (road.mStatus != Road.STATUS_OK) {
                    Toast.makeText(getContext(), "Błąd pobierania trasy", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Usuwanie poprzednich tras (Polyline)
                mapView.getOverlays().removeIf(overlay -> overlay instanceof Polyline);

                // Rysowanie nowej trasy
                Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                roadOverlay.setWidth(10);
                roadOverlay.setColor(Color.BLUE);
                mapView.getOverlays().add(roadOverlay);
                mapView.invalidate();
            });
        }).start();
    }

    private void loadShopsFromFirebase() {
        // Pobiera dane sklepów z Firebase
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
        // Inicjalizacja lub odświeżenie adaptera do RecyclerView
        if (shopAdapter == null) {
            shopAdapter = new ShopAdapter(shopList, shop -> {
                recyclerView.setVisibility(View.GONE); // Ukrywa listę po wyborze sklepu
                goal = new GeoPoint(shop.getLatitude(), shop.getLongitude()); // Ustawia cel trasy
                ((TextView) requireActivity().findViewById(R.id.shop_name)).setText(shop.getName());
                saveShop(goal, shop.getName()); // Zapisuje lokalizację
                isPositionInitialized = false; // Resetuje flagę pozycjonowania
            });
            recyclerView.setAdapter(shopAdapter);
        } else {
            shopAdapter.notifyDataSetChanged();
        }
    }

    private void saveShop(GeoPoint point, String name) {
        // Zapisuje dane sklepu w SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat("latitude", (float) point.getLatitude());
        editor.putFloat("longitude", (float) point.getLongitude());
        editor.putString("name", name);
        editor.apply();
    }

    private Shop getSavedCoordinates() {
        // Odczytuje zapisane dane sklepu z SharedPreferences
        SharedPreferences prefs = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        float latitude = prefs.getFloat("latitude", 0);
        float longitude = prefs.getFloat("longitude", 0);
        String name = prefs.getString("name", null);

        if (latitude == 0 || longitude == 0 || name == null) {
            return null; // Jeśli brak danych
        }
        return new Shop(name, latitude, longitude);
    }
}
