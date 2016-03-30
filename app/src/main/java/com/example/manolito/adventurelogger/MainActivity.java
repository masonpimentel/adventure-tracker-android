package com.example.manolito.adventurelogger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    //a constant used to determine if our request to turn on bluetooth worked
    private final static int REQUEST_ENABLE_BT = 1;

    //a handle to the tablet’s bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter;

    //get the context for the application. We use this with things like "toast" popups
    private Context context;

    //handle to BroadCastReceiver object
    private BroadcastReceiver mReceiver ;

    //a bluetooth socket to a bluetooth device
    private BluetoothSocket mmSocket = null;

    //input/output streams to read and write to device
    //use of “static” means variables can be accessed
    //without an object, this is useful as other activities can use
    //these streams to communicate after they have been opened
    public static InputStream mmInStream = null;
    public static OutputStream mmOutStream = null;

    //indicates if we are connected to a device
    private boolean Connected = false;

    private BluetoothDevice pairDevice;

    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdventureLogger";

    private String testStr = new String();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        context = getApplicationContext();
        //returns a handle to the one bluetooth device within the Android device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast toast = Toast.makeText(context, "", Toast.LENGTH_LONG);
            toast.show();
            finish();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            //create a new intent that will ask the bluetooth adaptor to “enable” itself.
            Intent enableBtIntent = new Intent ( BluetoothAdapter.ACTION_REQUEST_ENABLE );

            //REQUEST_ENABLE_BT below is a constant (defined as '1 - but could be anything)
            //when the “activity” is run and finishes, Android will run onActivityResult()
            //function
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        //create AdventureLogger directory in external storage
        File dir = new File(path);
        Log.i("ADV_FILE", ("AdventureLogger path is " + path));
        if (dir.mkdirs() || dir.isDirectory()) {
            Log.i("ADV_FILE", "AdventureLogger path already exists");
        }
        else {
            Log.i("ADV_FILE", "AdventureLogger path created");
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Connecting to Adventure Tracker...", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();

                // we are going to connect to the other device as a client
                // if we are already connected to a device, close connections
                if(Connected == true)
                    closeConnection();	// user defined fn to close streams (Page23)

                CreateSerialBluetoothDeviceSocket( pairDevice ) ;
                ConnectToSerialBlueToothDevice();	// user defined fn

                //get a string from DE2
                testStr = ReadFromBTDevice();
                Log.i("BLUETOOTH", testStr);

            }
        });

        mReceiver = new BroadcastReceiver() {
            public void onReceive (Context context, Intent intent) {
                String action = intent.getAction();
                BluetoothDevice newDevice;

                if ( action.equals(BluetoothDevice.ACTION_FOUND) ) {
                    //if notification is a “new device found”
                    newDevice = intent.getParcelableExtra ( BluetoothDevice.EXTRA_DEVICE );

                    String theDevice = new String( newDevice.getName() +
                            "\nMAC Address = " + newDevice.getAddress());

                    Log.i("MY_MESSAGE", theDevice);

                    //hackery for now - just make sure some info from the DE2 is here
                    //this way we will only connect to the DE2
                    if (theDevice.contains("00:06:66:6C:A9:B1")) {
                        Log.i("MY_MESSAGE", "Found the DE2");
                        pairDevice = newDevice;
                    }

                    Toast.makeText(context, theDevice, Toast.LENGTH_LONG).show();	// create popup for device
                }
                // more visual feedback for user (not essential but useful)
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    Toast.makeText(context, "Discovery Started", Toast.LENGTH_LONG).show();
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ) {
                    Toast.makeText(context, "Discovery Finished", Toast.LENGTH_LONG).show();
                }
            }
        };

        //create 3 separate IntentFilters that are tuned to listen to certain Android notifications
        //1) when new Bluetooth devices are discovered,
        //2) when discovery of devices starts (not essential but give useful feedback)
        //3) When discovery ends (not essential but give useful feedback)
        IntentFilter filterFound = new IntentFilter (BluetoothDevice.ACTION_FOUND);
        IntentFilter filterStart = new IntentFilter (BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        IntentFilter filterStop = new IntentFilter (BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        //register our broadcast receiver using the filters defined above
        //broadcast receiver will have it’s “onReceive()” function called
        //so it gets called every time a notification is broacast by Android that matches one of the
        //3 filters, e.g.
        //a new bluetooth device is found or discovery starts or finishes
        registerReceiver (mReceiver, filterFound);
        registerReceiver (mReceiver, filterStart);
        registerReceiver (mReceiver, filterStop);

        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        mBluetoothAdapter.startDiscovery() ;
    }

    void closeConnection() {
        try {
            mmInStream.close();
            mmInStream = null;
        } catch (IOException e) {}

        try {
            mmOutStream.close();
            mmOutStream = null;
        } catch (IOException e) {}

        try {
            mmSocket.close();
            mmSocket = null;
        } catch (IOException e) {}

        Connected = false ;
    }

    public void CreateSerialBluetoothDeviceSocket(BluetoothDevice device)
    {
        mmSocket = null;

        //universal UUID for a serial profile RFCOMM blue tooth device
        UUID MY_UUID = UUID.fromString ("00001101-0000-1000-8000-00805F9B34FB");

        //get a Bluetooth Socket to connect with the given BluetoothDevice
        try {
            //MY_UUID is the app's UUID string, also used by the server code
            mmSocket = device.createRfcommSocketToServiceRecord (MY_UUID);
        }
        catch (IOException e) {
            Toast.makeText(context, "Socket Creation Failed", Toast.LENGTH_LONG).show();
        }
    }

    public void ConnectToSerialBlueToothDevice() {
        //cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            //attempt connection to the device through the socket.
            mmSocket.connect();
            Toast.makeText(context, "Connection Made", Toast.LENGTH_LONG).show();
        }
        catch (IOException connectException) {
            Toast.makeText(context, "Connection Failed", Toast.LENGTH_LONG).show();
            return;
        }

        //create the input/output stream and record fact we have made a connection
        GetInputOutputStreamsForSocket();
        Connected = true ;
    }

    //gets the input/output stream associated with the current socket
    public void GetInputOutputStreamsForSocket() {
        try {
            mmInStream = mmSocket.getInputStream();
            mmOutStream = mmSocket.getOutputStream();
        } catch (IOException e) { }
    }

    //@Override
    //public void onDestroy() {
    //    unregisterReceiver ( mReceiver );  // make sure we unregister
        // our broadcast receiver at end
    //}

    // This function reads a line of text from the Bluetooth device
    public String ReadFromBTDevice() {
        byte c;
        String s = new String("");

        try { // Read from the InputStream using polling and timeout
            for (int i = 0; i < (200*60); i++) {
                // try to read for 2 seconds * 60 max
                SystemClock.sleep(10);
                if (mmInStream.available() > 0) {
                    if ((c = (byte) mmInStream.read()) != '\r') // '\r' terminator
                        s += (char) c; // build up string 1 byte by byte
                }
            }
        } catch (IOException e) {
            return new String("-- No Response --");
        }
        return s;
    }


    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast toast = Toast.makeText(context, "BlueTooth Failed to Start ", Toast.LENGTH_LONG);
                toast.show();
                finish();
                return;
            }
        }
    }

    public void sendCoordinates(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("map",0);
        intent.putExtra("path",path);
        startActivity(intent);
    }

    public void sendCypress(View view) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("map",1);
        intent.putExtra("path",path);
        startActivity(intent);
    }
}
