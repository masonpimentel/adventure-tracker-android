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

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private int map;
    private String path = new String();
    private FileInputStream fis = null;
    private InputStreamReader isr = null;
    private BufferedReader br;
    private File file = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        path = getIntent().getStringExtra("path");
        Log.i("ADV_FILE", ("Path = " + path));

        // temporarily get coordinate data from log0
        file = new File(path + "/log1.txt");

        try {
            fis = new FileInputStream(file);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    //draw a polyline path on Whistler
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        map = getIntent().getIntExtra("map", 0);
        int entries = numEntries();
        double[] lats = new double[entries];
        double[] longs = new double[entries];

        lats = ReadLats(entries, file);
        longs = ReadLongs(entries, file);

        if (map==0) {
            //focus gps location
            //TODO: fine tune the zoom
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lats[0], -longs[0]), 15));

            //add the points to the polyline
            PolylineOptions options = new PolylineOptions().color(-1).geodesic(true);

            for (int i=0; i<entries; i++) {
                options.add(new LatLng(lats[i],-longs[i]));
                mMap.addPolyline(options);
            }

            //start point
            mMap.addMarker(new MarkerOptions().position(new LatLng(lats[0], -longs[0])).title("Start").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            //end point
            mMap.addMarker(new MarkerOptions().position(new LatLng(lats[entries - 1], -longs[entries - 1])).title("End"));

        }
    }

    public void returnToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //return the number of entries in the log

    public int numEntries() {
        int test;
        int entries = 0;
        //mark requires a "limit", set that to the maximum range of an int...
        int markLimit = 2000000000;

        try {
            //set the mark to the beginning of the file
            br.mark(markLimit);
            while ((test = br.read()) >= 0) {
                if (test == 'x') {
                    Log.i("ADV_FILE", "Incrementing entries");
                    entries++;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        entries++;
        Log.i("ADV_FILE", ("Entries = " + entries));
        return entries;
    }

    //read all the latitudes
    public double[] ReadLats(int entries, File file) {
        int character;
        double[] array = new double[entries];
        int k = 0;
        String latitude = new String();

        try {
            //reset the seek position
            br.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //fill array with the latitudes
        try {
            while ((character = br.read()) >= 0) {
                if (character == 'L') {
                    character = br.read();
                    if (character == 'a') {
                        character = br.read();
                        if (character == 't') {
                            //skip 7 characters
                            br.skip(7);
                            latitude = latitude + (char)br.read();
                            //keep reading until the next space
                            for(int i=0; character != 32; i++) {
                                character = br.read();
                                if (character == 32) {
                                    break;
                                }
                                latitude = latitude + (char)character;
                                //avoid getting stuck in an infinite loop
                                Assert.assertTrue(i<40);
                            }
                            array[k] = Double.parseDouble(latitude);
                            k++;
                            latitude = "";
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }

    //read all the longitudes
    public double[] ReadLongs(int entries, File file) {
        int character;
        double[] array = new double[entries];
        int k = 0;
        String longitude = new String();

        try {
            //reset the seek position
            br.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //fill array with the latitudes
        try {
            while ((character = br.read()) >= 0) {
                if (character == 'L') {
                    character = br.read();
                    if (character == 'o') {
                        character = br.read();
                        if (character == 'n') {
                            //skip 8 characters
                            br.skip(8);
                            longitude = longitude + (char)br.read();
                            //keep reading until the next space
                            for(int i=0; character != 32; i++) {
                                character = br.read();
                                if (character == 32) {
                                    break;
                                }
                                longitude = longitude + (char)character;
                                //avoid getting stuck in an infinite loop
                                Assert.assertTrue(i<40);
                            }
                            array[k] = Double.parseDouble(longitude);
                            k++;
                            longitude = "";
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return array;
    }
}
