package com.example.join;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class menu extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener {

    GoogleMap mMap;
    EditText txtLat, txtLong;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.menu), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        txtLat=findViewById(R.id.txtLatitud);
        txtLong=findViewById(R.id.txtLongitud);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        this.mMap.setOnMapClickListener(this);
        this.mMap.setOnMapLongClickListener(this);
        LatLng espana = new LatLng(40.4943143,-3.6568315);
        mMap.addMarker(new MarkerOptions().position(espana).title("España"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(espana));
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        txtLat.setText(""+latLng.latitude);
        txtLong.setText(""+latLng.longitude);
        mMap.clear();

        LatLng espana = new LatLng(latLng.latitude,latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(espana).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(espana));
    }

    @Override
    public void onMapLongClick(@NonNull LatLng latLng) {
        txtLat.setText(""+latLng.latitude);
        txtLong.setText(""+latLng.longitude);
        mMap.clear();

        LatLng espana = new LatLng(latLng.latitude,latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(espana).title(""));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(espana));
    }
}

