package com.example.manolito.adventurelogger;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int map;
    //private String path = getIntent().getStringExtra("path");
    // temporarily get coordinate data from log0
    /*
    File file = new File(path + "/log0.txt");

    FileInputStream fis = null;
    InputStreamReader isr = new InputStreamReader(fis);
    BufferedReader br = new BufferedReader(isr);
    */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        try {
            fis = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/

    }

    //draw a polyline path on Whistler
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        map = getIntent().getIntExtra("map",0);

        //int entries = numEntries();
        //int[] lats = new int[logs];
        //lats = ReadLats(file);
        //int[] longs = new int[1];

        if (map==0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.058054, -122.960060), 15));
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                            .add(new LatLng(50.058408, -122.957296))
                            .add(new LatLng(50.058100, -122.958635))
                            .add(new LatLng(50.058054, -122.960060))
                            .add(new LatLng(50.058407, -122.962063))
                            .add(new LatLng(50.058181, -122.963453))
                            .add(new LatLng(50.058112, -122.964878))
                            .color(-1)
            );

            mMap.addMarker(new MarkerOptions().position(new LatLng(50.058408, -122.957296)).title("Start").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(new LatLng(50.058112, -122.964878)).title("End"));

        }
        else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(49.403627, -123.198516), 14));
            mMap.addPolyline(new PolylineOptions().geodesic(true)
                            .add(new LatLng(49.405240, -123.195763))
                            .add(new LatLng(49.404062, -123.197485))
                            .add(new LatLng(49.403627, -123.198516))
                            .add(new LatLng(49.403208, -123.200125))
                            .add(new LatLng(49.402916, -123.201020))
                            .add(new LatLng(49.402613, -123.201517))
                            .color(-1)
            );

            mMap.addMarker(new MarkerOptions().position(new LatLng(49.405240, -123.195763)).title("Start").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mMap.addMarker(new MarkerOptions().position(new LatLng(49.402613, -123.201517)).title("End"));
        }
    }

    public void returnToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //return the number of entries in the log
    /*
    public int numEntries() {
        int test;
        int entries = 0;

        try {
            while ((test = br.read()) != 0) {
                if (test == 'x') {
                    Log.i("ADV_FILE", "Incrementing entries");
                    entries++;
                }
            }
        }
        catch (IOException e) {e.printStackTrace();};

        Log.i("ADV_FILE", ("Entries = " + entries));
        return entries;
    }*/

    /*
    //read all the latitudes
    public static int[] ReadLats(File file) {




        //debugging
        int[] array = new int[1];
        array[0] = 0;

        return array;
    } */
}
