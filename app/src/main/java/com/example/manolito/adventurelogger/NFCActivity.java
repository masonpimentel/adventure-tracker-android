package com.example.manolito.adventurelogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import junit.framework.Assert;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class NFCActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter;
    TextView textView;
    public static String compile = new String("Total distance: ");
    public static String compare = new String("Your distance: ");
    public static String compare2 = new String();
    public double distance = 0;
    public double altitude = 0;
    public double time = 0;
    public double pois = 0;
    public double your_distance = 0;
    public double your_altitude = 0;
    public double your_time = 0;
    public double your_pois = 0;

    private FileInputStream fis = null;
    private InputStreamReader isr = null;
    private BufferedReader br;

    public static FileOutputStream fos = null;

    //the three states the bluetooth adapter can be in
    private boolean attempting = false;
    private boolean found = false;
    private boolean paired = false;

    private static final File totalsFile = new File(MainActivity.path + "/total.txt");

    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdventureLogger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_left);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = getSupportActionBar();

        // Enable the Up button
        ab.setDisplayHomeAsUpEnabled(true);

        PackageManager pm = this.getPackageManager();
        if(!pm.hasSystemFeature(PackageManager.FEATURE_NFC)) {
            Toast.makeText(this, "The device does not has NFC hardware.",
                    Toast.LENGTH_SHORT).show();
        } else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            Toast.makeText(this, "Android Beam is not supported.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Android Beam is supported on your device.",
                    Toast.LENGTH_SHORT).show();
        }

        attempting = getIntent().getBooleanExtra("attempting", false);
        found = getIntent().getBooleanExtra("found", false);
        paired = getIntent().getBooleanExtra("paired", false);

        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        //compile data right away
        View parentLayout = findViewById(android.R.id.content);
        sendFile(parentLayout);

    }

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
                    entries++;
                }
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //entries++;
        return entries;
    }

    public void sendFile(View v) {
        //iterate through AdventureLogger directory
        File directory = new File(MainActivity.path);
        double[] lats;
        double[] longs;
        double[] a_times;
        double[] a_altitudes;
        double[] a_pois;
        double distance = 0;
        double altitude = 0;
        double time = 0;
        double pois = 0;
        int entries;
        int i;
        double diffSeconds;
        double firstSeconds;
        double secondSeconds;
        String distance_s = new String();
        String altitude_s = new String();
        String time_s = new String();
        String poi_s = new String();
        byte[] output;
        String output_s = new String();
        int length;

        File[] files = directory.listFiles();
        for (File file : files) {
            //set up buffered reader
            try {
                fis = new FileInputStream(file);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            entries = numEntries()-1;
            lats = new double[entries];
            longs = new double[entries];
            a_times = new double[entries];
            a_altitudes = new double[entries];
            a_pois = new double[entries];
            lats = N_ReadLats(entries, file);
            longs = N_ReadLongs(entries, file);
            a_times = N_ReadTimes(entries, file);
            a_altitudes = N_Read_Altitudes(entries, file);
            a_pois = N_Read_POIS(entries,file);
            //convert to decimal form
            for (i=0; i<entries; i++) {
                lats[i] = decimalConvert(lats[i]);
                longs[i] = decimalConvert(longs[i]);
            }
            for (i = 0; i < entries - 1; i++) {
                distance = distance + Distance.distance(lats[i], longs[i], lats[i + 1], longs[i + 1], "K");
            }
            your_distance = distance;
            for (i = 0; i < entries - 1; i++) {
                firstSeconds = a_times[i];
                secondSeconds = a_times[i+1];
                diffSeconds = secondSeconds - firstSeconds;
                //check for roll-over
                if (diffSeconds < 0) {
                    diffSeconds += (12*3600);
                }
                time = time + diffSeconds;
            }
            your_time = time;
            for (i = 0; i < entries-1; i++) {
                altitude = altitude + Math.abs(a_altitudes[i+1] - a_altitudes[i]);
            }
            your_altitude = altitude;
            for (i = 0; i < entries; i++) {
                pois = pois + a_pois[i];
            }
            your_pois = pois;
        }

        //put total into total.txt
        try
        {
            fos = new FileOutputStream(totalsFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            output_s = Double.toString(distance);
            output = output_s.getBytes();
            length = output_s.length();
            for (i=0; i<length; i++) {
                fos.write(output[i]);
            }
            fos.write(13);
            fos.write(10);
            output_s = Double.toString(time);
            output = output_s.getBytes();
            length = output_s.length();
            for (i=0; i<length; i++) {
                fos.write(output[i]);
            }
            fos.write(13);
            fos.write(10);
            output_s = Double.toString(altitude);
            output = output_s.getBytes();
            length = output_s.length();
            for (i=0; i<length; i++) {
                fos.write(output[i]);
            }
            fos.write(13);
            fos.write(10);
            output_s = Double.toString(pois);
            output = output_s.getBytes();
            length = output_s.length();
            for (i=0; i<length; i++) {
                fos.write(output[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try
        {
            fos.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if(!mNfcAdapter.isEnabled()) {
            Toast.makeText(this, "Please enable NFC",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(Settings.ACTION_NFC_SETTINGS));
        } else if(!mNfcAdapter.isNdefPushEnabled()) {
            Toast.makeText(this, "Please enable Android Beam",
                    Toast.LENGTH_SHORT).show();

            startActivity(new Intent(Settings.ACTION_NFCSHARING_SETTINGS));
        } else {
            String fileName = "total.txt";

            File fileToTransfer = new File(path, fileName);
            fileToTransfer.setReadable(true, false);

            mNfcAdapter.setBeamPushUris(new Uri[]{Uri.fromFile(fileToTransfer)}, this);
        }

        compile = "Your total stats:\n\n" + "Total distance: " + String.format("%.4f", distance) + "KM\nTotal change in altitude: " + String.format("%.2f", altitude) + "M\nTotal time: "
            + secondsToTime(time) + "\nTotal POIs:" + String.format("%.0f", pois);
        CompileDialogFragment compileDialog = new CompileDialogFragment();
        compileDialog.show(getFragmentManager(), "compile");
    }

    public void compareStats(View v) {
        //read totals in downloads folder
        File directory = new File(MainActivity.pathDownload);
        int tempnum = 0;
        int maxnum = 0;
        boolean totalfile = false;
        String filename = new String();
        String c_distance = new String();
        String c_altitude = new String();
        String c_time = new String();
        String c_pois = new String();

        File[] files = directory.listFiles();
        for (File file : files) {
            filename = file.getName();
            if (filename.contains("total-")) {
                String[] parts = filename.split("-");
                String[] parts2 = parts[1].split("\\.");
                String filenameStr = parts2[0];
                //can't use parseInt on a single char...
                tempnum = Integer.parseInt(filenameStr);
                if (tempnum > maxnum) {
                    maxnum = tempnum;
                }
            }
            else if (filename.contains("total")) {
                totalfile = true;
            }
        }

        //there was no total file
        if (totalfile == false) {
            Toast.makeText(this, "Please Beam a file from your friend first",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        //there was only one totals file
        if (maxnum == 0) {
            File totalFile = new File((MainActivity.pathDownload + "/total.txt"));
            try {
                fis = new FileInputStream(totalFile);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                distance = Double.parseDouble(br.readLine());
                time = Double.parseDouble(br.readLine());
                altitude = Double.parseDouble(br.readLine());
                pois = Double.parseDouble(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        //there were multiple total files
        else {
            compare2 = new String(MainActivity.pathDownload + "/total-" + maxnum + ".txt");
            File totalFile = new File((MainActivity.pathDownload + "/total-" + maxnum + ".txt"));
            try {
                fis = new FileInputStream(totalFile);
                isr = new InputStreamReader(fis);
                br = new BufferedReader(isr);
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e) {
                e.printStackTrace();
            }
            try {
                distance = Double.parseDouble(br.readLine());
                time = Double.parseDouble(br.readLine());
                altitude = Double.parseDouble(br.readLine());
                pois = Double.parseDouble(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //compare
        if (your_distance > distance) {
            c_distance = "You travelled " + String.format("%.4f", (your_distance - distance)) + " KM further.";
        }
        else if (your_distance < distance) {
            c_distance = "Your friend travelled " + String.format("%.4f", (distance-your_distance)) + " KM further.";
        }
        else {
            c_distance = "You travelled the same distance!";
        }
        if (your_time > time) {
            c_time = "You travelled " + secondsToTime((int)(your_time - time)) + " longer.";
        }
        else if (your_time < time){
            c_time = "Your friend travelled " + secondsToTime((int)(time - your_time)) + " longer.";
        }
        else {
            c_time = "You travelled the same amount of time!";
        }
        if (your_altitude > altitude) {
            c_altitude = "You changed " + String.format("%.2f", (your_altitude - altitude)) + " M more in altitude.";
        }
        else if (your_altitude < altitude) {
            c_altitude = "Your friend changed " + String.format("%.2f", (altitude - your_altitude)) + " M more in altitude.";
        }
        else {
            c_altitude = "You both changed the same amount in altitude!";
        }
        if (your_pois > pois) {
            c_pois = "You made " + String.format("%.0f", (your_pois - pois)) + " more POIs.";
        }
        else if (your_pois < pois) {
            c_pois = "Your friend made " + String.format("%.0f", (pois - your_pois)) + " more POIs.";
        }
        else {
            c_pois = "You both made the same number of POIs!";
        }

        compare = c_distance + "\n" + c_time + "\n" + c_altitude + "\n" + c_pois;
        CompareDialogFragment compareDialog = new CompareDialogFragment();
        compareDialog.show(getFragmentManager(), "compare");
    }

    public static class CompileDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(NFCActivity.compile)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public static class CompareDialogFragment extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            //use the Builder class for convenient dialog construction
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(NFCActivity.compare)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    //read all the longitudes
    public double[] N_ReadLongs(int entries, File file) {
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

        //fill array with the longitudes
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
                                if (character == 32 || character == 69 || character == 87) {
                                    break;
                                }
                                longitude = longitude + (char)character;
                                //avoid getting stuck in an infinite loop
                                Assert.assertTrue(i < 40);
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

    //read all the latitudes
    public double[] N_ReadLats(int entries, File file) {
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
                                if (character == 32 || character == 83 || character == 78) {
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

    //read all the times
    public double[] N_ReadTimes(int entries, File file) {
        int character;
        double[] array = new double[entries];
        int k = 0;
        String hour = new String();
        String minute = new String();
        String second = new String();
        double hoursDbl = 0;
        double minutesDbl = 0;
        double secondsDbl = 0;
        double totalSeconds = 0;

        try {
            //reset the seek position
            br.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //fill array with the times
        try {
            while ((character = br.read()) >= 0) {
                if (character == 'T') {
                    character = br.read();
                    if (character == 'i') {
                        character = br.read();
                        if (character == 'm') {
                            //skip 3 characters
                            br.skip(3);
                            //get hours
                            hour = hour + (char)br.read();
                            hour = hour + (char)br.read();
                            br.skip(1);
                            //get minutes
                            minute = minute + (char)br.read();
                            minute = minute + (char)br.read();
                            br.skip(1);
                            //get seconds
                            second = second + (char)br.read();
                            second = second + (char)br.read();
                            //keep reading until the next space
                            hoursDbl = 3600*(Double.parseDouble(hour));
                            minutesDbl = 60*(Double.parseDouble(minute));
                            secondsDbl = Double.parseDouble(second);
                            totalSeconds = hoursDbl + minutesDbl + secondsDbl;
                            array[k] = totalSeconds;
                            hour = "";
                            minute = "";
                            second = "";
                            k++;
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

    //read all the altitudes
    public double[] N_Read_Altitudes(int entries, File file) {
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

        //fill array with the altitudes
        try {
            while ((character = br.read()) >= 0) {
                if (character == 'A') {
                    character = br.read();
                    if (character == 'l') {
                        character = br.read();
                        if (character == 't') {
                            //skip 7 characters
                            br.skip(7);
                            altitude = altitude + (char)br.read();
                            //keep reading until the next M
                            for(int i=0; character != 32; i++) {
                                character = br.read();
                                if (character == 32 || character == 77) {
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

    //read all the pois
    public double[] N_Read_POIS(int entries, File file) {
        int character;
        double[] array = new double[entries];
        int k = 0;
        String poi = new String();

        try {
            //reset the seek position
            br.reset();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        //fill array with the pois
        try {
            while ((character = br.read()) >= 0) {
                if (character == 'P') {
                    character = br.read();
                    if (character == 'O') {
                        character = br.read();
                        if (character == 'I') {
                            //skip 1 character
                            br.skip(1);
                            poi = poi + (char)br.read();
                            array[k] = Double.parseDouble(poi);
                            k++;
                            poi = "";
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

    public double decimalConvert(double entry) {
        String entryS = String.format("%.4f", entry);
        String[] split1 = entryS.split("\\.");
        //substring 0 to len-2
        double part1 = Double.parseDouble(split1[0].substring(0,(split1[0].length()-2)));
        //substring of whole entry from length-7 to length
        int lengthEntry = entryS.length();
        double part2 = (Double.parseDouble(entryS.substring((lengthEntry-7),lengthEntry)))/60;
        return (part1+part2);
    }

    public String secondsToTime(double seconds) {
        String time = new String();

        double hours = 0;
        double minutes = 0;

        if (seconds >= 3600) {
            hours = seconds/3600;
            seconds = seconds - (hours*3600);
        }
        if (seconds >= 60) {
            minutes = seconds/60;
            seconds = seconds - (minutes*60);
        }

        time = String.format("%02.0f:%02.0f:%02.0f", hours, minutes, seconds);

        return time;
    }

}


