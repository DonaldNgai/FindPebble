package com.example.donald.FindPebble;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID APP_UUID = UUID.fromString("b19073a4-2b69-4a20-8bf7-3df7d4deed25");
    private static final int RESULT_KEY = 0;
    private static final int START_BUTTON = 0;
    private static final int VIBRATE_BUTTON = 1;
    private static final int STOP_BUTTON = 2;

    private Button findButton;
    private Button notificationButton;
    private Button stopButton;
    private TextView textView;

    private Handler mHandler = new Handler();

    private Context context;

    @Override
    protected void onResume(){
        super.onResume();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //Check if Bluetooth is Enabled
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(
                        BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);

            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findButton = (Button) findViewById(R.id.find_button);
        notificationButton = (Button) findViewById(R.id.sendNotification);
        stopButton = (Button) findViewById(R.id.stop_button);
        textView = (TextView)findViewById(R.id.text_view);

        context = getApplicationContext();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Button Functions
    public void notificationOnClick(View v) {
        // Push a notification
        final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

        final Map data = new HashMap();
        data.put("title", "Test Message");
        data.put("body", "Whoever said nothing was impossible never tried to slam a revolving door.");
        final JSONObject jsonData = new JSONObject(data);
        final String notificationData = new JSONArray().put(jsonData).toString();

        i.putExtra("messageType", "PEBBLE_ALERT");
        i.putExtra("sender", "PebbleKit Android");
        i.putExtra("notificationData", notificationData);
        sendBroadcast(i);
        Toast.makeText(this, "Notification Sent", Toast.LENGTH_LONG).show();
    }

    public void startAppOnClick() {
//        Context context = getApplicationContext();

        if(checkIfConnected()) {
            // Launch the sports app
            PebbleKit.startAppOnPebble(context, APP_UUID);

            Toast.makeText(context, "Launching...", Toast.LENGTH_SHORT).show();

            findButton.setVisibility(View.VISIBLE);
            notificationButton.setVisibility(View.VISIBLE);

            // Send data 5s after launch
            mHandler.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // Send a time and distance to the sports app
                    PebbleDictionary outgoing = new PebbleDictionary();
//                    outgoing.addString(RESULT_KEY, START_BUTTON);
                    outgoing.addInt32(RESULT_KEY, START_BUTTON);
                    PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, outgoing);
                }

            }, 5000L);
        }
    }

    public void findButtonOnClick(View v) {
//        Context context = getApplicationContext();

        if(checkIfConnected()){

            Toast.makeText(context, "Finding Pebble Watch", Toast.LENGTH_SHORT).show();

            stopButton.setVisibility(View.VISIBLE);

            // Send a time and distance to the sports app
            PebbleDictionary outgoing = new PebbleDictionary();
            outgoing.addInt32(RESULT_KEY, VIBRATE_BUTTON);
            PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, outgoing);

        }
    }

    public void stopButtonOnClick(View v) {
//        Context context = getApplicationContext();

        if(checkIfConnected()){

            Toast.makeText(context, "Stopping Vibrations", Toast.LENGTH_SHORT).show();

            // Send a time and distance to the sports app
            PebbleDictionary outgoing = new PebbleDictionary();
            outgoing.addInt32(RESULT_KEY, STOP_BUTTON);
            PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, outgoing);

        }
    }

    public boolean checkIfConnected(){
        Context context = getApplicationContext();
        boolean isConnected = PebbleKit.isWatchConnected(context);
        if(isConnected) {
            return true;
        }
        else {
            Toast.makeText(context, "Watch is not connected!", Toast.LENGTH_LONG).show();
            return false;
        }
    }

}
