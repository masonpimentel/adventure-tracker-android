package com.example.manolito.adventurelogger;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Slide;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TitlePage extends AppCompatActivity implements GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener{

    //this is the timeout for the bluetooth adapter
    private static final int timeout = 100;

    private static final String DEBUG_TAG = "TITLE_PAGE";
    private GestureDetectorCompat mDetector;

    //a constant used to determine if our request to turn on bluetooth worked
    public final static int REQUEST_ENABLE_BT = 1;

    //a handle to the tablet’s bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter;

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

    public static FileOutputStream fos = null;

    private File outFile = null;

    public TextView pairStatus;

    private int numFiles = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        pairStatus = (TextView) findViewById(R.id.pairStatus);

        checkStatus();

        // Instantiate the gesture detector with the
        // application context and an implementation of
        // GestureDetector.OnGestureListener
        mDetector = new GestureDetectorCompat(this,this);
        mDetector.setOnDoubleTapListener(this);

        //returns a handle to the one bluetooth device within the Android device
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
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
        File dir = new File(MainActivity.path);
        Log.i("ADV_FILE", ("AdventureLogger path is " + MainActivity.path));
        if (dir.mkdirs() || dir.isDirectory()) {
            Log.i("ADV_FILE", "AdventureLogger path already exists");
        }
        else {
            Log.i("ADV_FILE", "AdventureLogger path created");
        }

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

                    //hardcoded to connect to just to our DE2 dongle
                    if (theDevice.contains("Adventure Tracker")) {
                        Toast.makeText(context, "Found Adventure Tracker!", Toast.LENGTH_LONG).show();
                        updateStatus(GlobalVariables.BTStatus.PAIRING);
                        pairDevice = newDevice;

                        //start pairing process
                        View parentLayout = findViewById(android.R.id.content);
                        bluetoothPair(parentLayout);
                    }

                    //Toast.makeText(context, theDevice, Toast.LENGTH_LONG).show();	// create popup for device
                }
                // more visual feedback for user (not essential but useful)
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                    Toast.makeText(context, "Discovery Started", Toast.LENGTH_LONG).show();
                }
                else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED) ) {
                    //Toast.makeText(context, "Discovery Finished", Toast.LENGTH_LONG).show();
                    if (GlobalVariables.status == GlobalVariables.BTStatus.NOT_PAIRED) {
                        Toast.makeText(context, "Could not find Adventure Tracker", Toast.LENGTH_LONG).show();
                    }
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
        registerReceiver(mReceiver, filterStop);
    }

    public void updateStatus(GlobalVariables.BTStatus status) {
        if (status == GlobalVariables.BTStatus.ATTEMPTING) {
            GlobalVariables.status = GlobalVariables.BTStatus.ATTEMPTING;
            pairStatus.setText("Attempting to find Adventure Tracker...");
            pairStatus.setTextColor(Color.BLUE);
        }
        else if (status == GlobalVariables.BTStatus.PAIRING) {
            GlobalVariables.status = GlobalVariables.BTStatus.PAIRING;
            pairStatus.setText("Pairing...");
            pairStatus.setTextColor(Color.BLUE);
        }
        else if (status == GlobalVariables.BTStatus.PAIRED) {
            GlobalVariables.status = GlobalVariables.BTStatus.PAIRED;
            pairStatus.setText("Paired to Adventure Tracker");
            pairStatus.setTextColor(Color.parseColor("#229133"));
        }
        else {
            //not paired
            GlobalVariables.status = GlobalVariables.BTStatus.NOT_PAIRED;
            pairStatus.setText("Not paired");
            pairStatus.setTextColor(Color.RED);
        }
    }
    public void checkStatus() {
        if (GlobalVariables.status == GlobalVariables.BTStatus.ATTEMPTING) {
            pairStatus.setText("Attempting to find Adventure Tracker...");
            pairStatus.setTextColor(Color.BLUE);
        }
        else if (GlobalVariables.status == GlobalVariables.BTStatus.PAIRING) {
            pairStatus.setText("Pairing...");
            pairStatus.setTextColor(Color.BLUE);
        }
        else if (GlobalVariables.status == GlobalVariables.BTStatus.PAIRED) {
            pairStatus.setText("Paired to Adventure Tracker");
            pairStatus.setTextColor(Color.parseColor("#229133"));
        }
        else {
            //not paired
            pairStatus.setText("Not paired");
            pairStatus.setTextColor(Color.RED);
        }
    }

    public void bluetoothStart(View view) {
        if (GlobalVariables.status == GlobalVariables.BTStatus.PAIRED) {
            Snackbar.make(view, "You're already connected!", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        updateStatus(GlobalVariables.BTStatus.ATTEMPTING);
        if (mBluetoothAdapter.isDiscovering())
            mBluetoothAdapter.cancelDiscovery();

        mBluetoothAdapter.startDiscovery();
    }

    public void bluetoothPair(View view) {
        if (GlobalVariables.status == GlobalVariables.BTStatus.ATTEMPTING || GlobalVariables.status == GlobalVariables.BTStatus.NOT_PAIRED) {
            Snackbar.make(view, "There was a problem discovering Adventure Tracker", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }

        // we are going to connect to the other device as a client
        // if we are already connected to a device, close connections
        if(Connected == true)
          closeConnection();	// user defined fn to close streams (Page23)

        CreateSerialBluetoothDeviceSocket(pairDevice) ;
        ConnectToSerialBlueToothDevice();	// user defined fn
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
            Toast.makeText(this, "Socket Creation Failed", Toast.LENGTH_LONG).show();
        }
        catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void ConnectToSerialBlueToothDevice() {
        //cancel discovery because it will slow down the connection
        mBluetoothAdapter.cancelDiscovery();

        try {
            //attempt connection to the device through the socket.
            mmSocket.connect();
            Toast.makeText(this, "Connection Made", Toast.LENGTH_LONG).show();
            updateStatus(GlobalVariables.BTStatus.PAIRED);
        }
        catch (IOException connectException) {
            Toast.makeText(this, "Connection Failed", Toast.LENGTH_LONG).show();
            return;
        }
        catch (NullPointerException e) {
            e.printStackTrace();
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

    public void syncFiles(View view) {
        int available = 0;
        if (GlobalVariables.status != GlobalVariables.BTStatus.PAIRED) {
            Snackbar.make(view, "Please connect to Adventure Tracker first.", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        //closeConnection();

        //initiate sync by sending
        String s = new String("sync");
        WriteToBTDevice(s);

        //wait for a bit and check if anything is available from DE2 - if none, return
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            available = mmInStream.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (available == 0) {
            Snackbar.make(view, "Adventure Tracker timed out.", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
        else {
            //check for an 'a' (97)
            try {
                available = mmInStream.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (available != 114) {
                Snackbar.make(view, "Adventure Tracker timed out.", Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            try {
                available = mmInStream.available();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (available == 0) {
                Snackbar.make(view, "Adventure Tracker timed out.", Snackbar.LENGTH_LONG)
                        .show();
                return;
            }
            else {
                //check for an 's' (114)
                //(paranoia check)
                try {
                    available = mmInStream.read();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (available != 114) {
                    Snackbar.make(view, "Adventure Tracker timed out.", Snackbar.LENGTH_LONG)
                            .show();
                    return;
                }
            }
        }


/*
        synchronized (waitConfirmation) {
            waitConfirmation.start();
            try {
                waitConfirmation.wait(500);

                if (waitConfirmation.isAlive()) {
                    Snackbar.make(view, "Adventure Tracker timed out.", Snackbar.LENGTH_LONG)
                            .show();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //for now, just close the app
                    this.finishAffinity();
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        //write logfile
        int result = 0;
        result = ReadFromBTDevice(view);
        if (result != 0) {
            Snackbar.make(view, "Error syncing files", Snackbar.LENGTH_LONG)
                    .show();
            return;
        }
    }

    public void nfcPage() {
        Intent intent = new Intent(this, NFCActivity.class);
        startActivity(intent);
    }

    public void pastPage() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    //
    // This function write a line of text (in the form of an array of bytes)
    // to the Bluetooth device and then sends the string “\r\n”
    // (required by the bluetooth dongle)
    //
    public void WriteToBTDevice (String message) {
        byte[] msgBuffer = message.getBytes();
        try {
            //just send "s" - that's all the DE2 is looking for
            mmOutStream.write(msgBuffer, 0, 1);
        }
        catch (IOException e) { }
        Log.i("ADV_FILE", "Sent!");
    }

    public int ReadFromBTDevice(View view) {
        byte c;
        String test = new String();

        //find out how many logs there are
        File directory = new File(MainActivity.path);

        File[] files = directory.listFiles();
        for (File file : files) {
            if (!file.getName().contains("total")) {
                numFiles++;
            }
        }

        //new logfile
        String logfile = new String("/log");
        logfile = logfile + numFiles;
        logfile = logfile + ".txt";

        Log.i("ADV_FILE", logfile);

        //set outFile to the new file
        outFile = new File(MainActivity.path + logfile);

        //set the output stream to the new file
        try
        {
            fos = new FileOutputStream(outFile);
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try { // Read from the InputStream using polling and timeout

            while (true) {
                //64 = @
                c = (byte) TitlePage.mmInStream.read();
                if (c == 64) {
                    Log.i("ADV_FILE", "Found the @!");
                    Snackbar.make(view, "Files successfully synced!", Snackbar.LENGTH_LONG)
                            .show();
                    try
                    {
                        fos.close();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                //output to the file
                //36 = $
                else if (c == 36) {
                    Log.i("ADV_FILE", "Found a $");
                    numFiles++;

                    //new logfile
                    logfile = new String("/log");
                    logfile = logfile + numFiles;
                    logfile = logfile + ".txt";

                    Log.i("ADV_FILE", logfile);

                    //set outFile to the new file
                    outFile = new File(MainActivity.path + logfile);

                    try
                    {
                        fos = new FileOutputStream(outFile);
                    }
                    catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    try {
                        test = test + (char)c;
                        Log.i("ADV_FILE", test);
                        fos.write(c);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            return -1;
        }
        return 0;
    }

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Toast toast = Toast.makeText(this, "BlueTooth Failed to Start ", Toast.LENGTH_LONG);
                toast.show();
                finish();
                return;
            }
        }
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2,
                           float velocityX, float velocityY) {
        Log.i(DEBUG_TAG, "onFling: " + event1.toString() + event2.toString());
        float x1 = event1.getX();
        float x2 = event2.getX();
        if (x2 > x1) {
            pastPage();
        }
        else if (x2 < x1) {
            nfcPage();
        }
        return true;
    }

    /*
    Thread waitConfirmation = new Thread(new Runnable() {
        public void run() {
            int read = 0;
            while (true) {
                try {
                    read = mmInStream.read();
                    //'r' = 114
                    if (read == 114) {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }); */

    //ignore the rest - they're just required to be included by the gesture detector

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent event) {
        //Log.d(DEBUG_TAG,"onDown: " + event.toString());
        return true;
    }

    @Override
    public void onLongPress(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onLongPress: " + event.toString());
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                            float distanceY) {
        //Log.d(DEBUG_TAG, "onScroll: " + e1.toString()+e2.toString());
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onShowPress: " + event.toString());
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onSingleTapUp: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onDoubleTap: " + event.toString());
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onDoubleTapEvent: " + event.toString());
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent event) {
        //Log.d(DEBUG_TAG, "onSingleTapConfirmed: " + event.toString());
        return true;
    }

}
