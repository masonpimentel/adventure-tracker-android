package com.example.manolito.adventurelogger;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    //draw a polyline path on Whistler
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.058408, -122.957296), 15));

        mMap.addPolyline(new PolylineOptions().geodesic(true)
                        .add(new LatLng(50.058408, -122.957296))
                        .add(new LatLng(50.058100, -122.958635))
                        .add(new LatLng(50.058054, -122.960060))
                        .add(new LatLng(50.058407, -122.962063))
                        .add(new LatLng(50.058181, -122.963453))
                        .add(new LatLng(50.058112, -122.964878))
                        .color(-1)
        );

        mMap.addMarker(new MarkerOptions().position(new LatLng(50.058408, -122.957296)).title("Start"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(50.058112, -122.964878)).title("End"));
    }
}
