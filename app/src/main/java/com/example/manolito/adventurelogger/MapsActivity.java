package com.example.manolito.adventurelogger;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
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
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private String map = new String();
    private String path = new String();
    private FileInputStream fis = null;
    private InputStreamReader isr = null;
    private BufferedReader br;
    private File file = null;
    private double[] lats;
    private double[] longs;
    private double[] altitudes;
    private String[] times;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        map = getIntent().getStringExtra("map");

        path = getIntent().getStringExtra("path");
        Log.i("ADV_FILE", ("Path = " + path));
        file = new File(path + "/" + map);

        // temporarily get coordinate data from log0
        //file = new File(path + "/log0.txt");

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
        int entries = numEntries();
        lats = new double[entries];
        longs = new double[entries];
        altitudes = new double[entries];
        times = new String[entries];
        ArrayList<POI> poiList = new ArrayList<>();
        double poiLat;
        double poiLon;

        lats = ReadLats(entries, file);
        longs = ReadLongs(entries, file);
        altitudes = ReadAltitudes(entries, file);
        poiList = ReadPois(entries, file);
        times = ReadTimes(entries, file);


        //TODO: draw POI on the map
        //focus gps location
        //TODO: fine tune the zoom
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lats[0], -longs[0]), 15));

        //add the points to the polyline
        PolylineOptions options = new PolylineOptions().color(-1).geodesic(true);

        for (int i=0; i<entries; i++) {
            options.add(new LatLng(lats[i],-longs[i]));
            mMap.addPolyline(options);
        }

        for (int i=0; i<poiList.size(); i++) {
            poiLat = poiList.get(i).latitude;
            poiLon = poiList.get(i).longitude;
            mMap.addMarker(new MarkerOptions().position(new LatLng(poiLat, -poiLon)).title("POI").
                    icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        }

        //start point
        mMap.addMarker(new MarkerOptions().position(new LatLng(lats[0], -longs[0])).title("Start").icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        //end point
        mMap.addMarker(new MarkerOptions().position(new LatLng(lats[entries - 1], -longs[entries - 1])).title("End"));


    }

    public void returnToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void tripStatistics(View view) {
        double totalDistance = getTotalDistance();
        double totalAltitudeChange = getTotalAltitudeChange();
        String totalTime = getTotalTime();
        Intent intent = new Intent(this, TripStatistics.class);
        intent.putExtra("distance",totalDistance);
        intent.putExtra("altitude",totalAltitudeChange);
        intent.putExtra("time",totalTime);

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

      public ArrayList<POI> ReadPois(int entries, File file) {
        ArrayList<POI> poiList = new ArrayList<>();
        double latitude, longitude;
        int character;

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
                if (character == 'P') {
                    character = br.read();
                    if (character == 'O') {
                        character = br.read();
                        if (character == 'I') {
                            br.skip(2);
                            character = br.read();
                            if (character == '1'){
                                /*
                                    after finding the current point is a Poi, it adds the next gps
                                    coordinate to the list of Pois
                                 */
                                latitude = ReadNextLat(file);
                                longitude = ReadNextLon(file);

                                POI newPoi = new POI(latitude, longitude);
                                poiList.add(newPoi);
                            }

                        }
                    }
                }
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
        return poiList;
    }

    public double ReadNextLat(File file) {
        int character;
        double retLat = 0;
        String latitude = "";
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
                            retLat = Double.parseDouble(latitude);
                            break;
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return retLat;
    }

    public double ReadNextLon(File file) {
        int character;
        double retLon = 0;
        String longitude = "";
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
                            retLon = Double.parseDouble(longitude);
                            break;
                        }
                    }
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return retLon;
    }

    public double getTotalDistance() {
        double totalDistance = 0;
        for(int i=0; i<lats.length-1; i++) {
            double startLat = lats[i];
            double endLat = lats[i + 1];
            double startLon = longs[i];
            double endLon = longs[i + 1];
            double dist = getDistance(startLat, startLon, endLat, endLon, "K");
            totalDistance = totalDistance + dist;
        }
        return totalDistance;
    }

    public double getTotalAltitudeChange() {
        double totalAltitudeChange = 0;
        for(int i=0; i<altitudes.length-1; i++) {
            double startAltitude = altitudes[i];
            double endAltitude =altitudes[i+1];
            double altitudeChange = endAltitude - startAltitude;
            totalAltitudeChange = totalAltitudeChange+altitudeChange;
        }
        return totalAltitudeChange;
    }

    public String getTotalTime() {
        int hours=0;
        int minutes=0;
        int seconds=0;

        String startTime = times[0];
        String endTime = times[times.length-1];
        int totalStartSeconds = getTotalSeconds(startTime);
        int totalEndSeconds = getTotalSeconds(endTime);
        int result = totalEndSeconds - totalStartSeconds;

        if(result<0){
            result = result + 12*3600;
        }

        if(result>3600){
            hours = result/3600;
            result = result-(3600*hours);
        }

        if(result>60){
            minutes = result/60;
            result = result-(60*minutes);
        }

        seconds = result;

        return Integer.toString(hours) + ":" + Integer.toString(minutes) + ":" + Integer.toString(seconds);

    }

    public int getTotalSeconds(String time) {
        String times[] = time.split(":");
        String hours = times[0];
        String minutes = times[1];
        String seconds = times[2];

        return 3600*Integer.parseInt(hours)+60*Integer.parseInt(minutes)+Integer.parseInt(seconds);
    }

    public double getDistance(double startLat, double startLon, double endLat, double endLon, String unit) {

        double dist, theta;
        if((startLat == endLat) && (startLon == endLon)) {
            return 0.0;
        }

        theta = startLon - endLon;

        dist = Math.sin(deg2rad(startLat)) * Math.sin(deg2rad(endLat)) + Math.cos(deg2rad(startLat))*Math.cos(deg2rad(endLat))*Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist*60*1.1515;

        switch(unit) {
            case "M":
                break;
            case "k":
                dist = dist*1.609344;
                break;
            case "N":
                dist = dist*0.8684;
                break;
            }

        return dist;
    }

    public double deg2rad(double deg){
        return deg*Math.PI/180;
    }

    public double rad2deg(double rad){
        return rad*180/Math.PI;
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

    //read all the longitudes
    public double[] ReadAltitudes(int entries, File file) {
        int character;
        double[] array = new double[entries];
        int k = 0;
        String altitude = new String();

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
                if (character == 'A') {
                    character = br.read();
                    if (character == 'l') {
                        character = br.read();
                        if (character == 't') {
                            //skip 8 characters
                            br.skip(7);
                            altitude = altitude + (char)br.read();
                            //keep reading until the next space
                            for(int i=0; character != 32; i++) {
                                character = br.read();
                                if (character == 32) {
                                    break;
                                }
                                altitude = altitude + (char)character;
                                //avoid getting stuck in an infinite loop
                                Assert.assertTrue(i<40);
                            }
                            array[k] = Double.parseDouble(altitude);
                            k++;
                            altitude = "";
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

    public String[] ReadTimes(int entries, File file) {
        int character;
        String[] array = new String[entries];
        int k = 0;
        String time = new String();

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
                if (character == 'T') {
                    character = br.read();
                    if (character == 'i') {
                        character = br.read();
                        if (character == 'm') {
                            if(character == 'e') {
                                //skip 8 characters
                                br.skip(2);
                                time = time + (char) br.read();
                                //keep reading until the next space
                                for (int i = 0; character != 32; i++) {
                                    character = br.read();
                                    if (character == 32) {
                                        break;
                                    }
                                    time = time + (char) character;
                                    //avoid getting stuck in an infinite loop
                                    Assert.assertTrue(i < 40);
                                }
                                array[k] = time;
                                k++;
                                time = "";
                            }
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
