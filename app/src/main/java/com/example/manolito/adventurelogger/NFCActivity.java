package com.example.manolito.adventurelogger;

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
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class NFCActivity extends AppCompatActivity {

    NfcAdapter mNfcAdapter;
    TextView textView;

    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdventureLogger";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
    }

}
