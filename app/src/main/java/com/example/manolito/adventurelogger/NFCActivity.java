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

import java.io.File;
import java.io.IOException;

public class NFCActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter;
    TextView textView;
    public static String compile = new String("Total distance: ");
    public static String compare = new String("You travelled this much more: ");
    public static String compare2 = new String();
    public int distance = 500;
    public int altitude = 25;
    public int time = 200;

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


        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Toast.makeText(this, "NFC is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

    }

    public void sendFile(View v) {
        //compile total

        //iterate through AdventureLogger directory

        //put total into total.txt


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
        String b_distance = new String();
        String b_altitude = new String();
        String b_time = new String();
        String b_pois = new String();

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

            //distance = br.readLine;
            //altitude = br.readLine;
            //time = br.readLine;
            //pois = br.readLine;

        }
        //there were multiple total files
        else {
            compare2 = new String(MainActivity.pathDownload + "/total-" + maxnum + ".txt");
            File totalFile = new File((MainActivity.pathDownload + "/total-" + maxnum + ".txt"));

            //distance = br.readLine;
            //altitude = br.readLine;
            //time = br.readLine;
            //pois = br.readLine;
        }

        //compile total again

        //compare

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
            builder.setMessage(NFCActivity.compare + NFCActivity.compare2)
                    .setPositiveButton(R.string.okay, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    /*
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

    //read all the times
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

    //read all the altitudes
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
    */

}


