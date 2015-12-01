package com.example.donald.FindPebble;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final UUID APP_UUID = UUID.fromString("b19073a4-2b69-4a20-8bf7-3df7d4deed25");
    private PebbleKit.PebbleDataReceiver mDataReceiver;
    private static final int KEY_BUTTON_UP = 0;
    private static final int KEY_BUTTON_SELECT = 1;
    private static final int KEY_BUTTON_DOWN = 2;
    private static final int RESULT_KEY = 0;
    private static final int START_BUTTON = 0;
    private static final int VIBRATE_BUTTON = 1;
    private static final int STOP_BUTTON = 2;

    private Button launchButton;
    private Button findButton;
    private Button notificationButton;
    private Button stopButton;
    private TextView textView;

    private Handler mHandler = new Handler();
    private Uri alert;
    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private Vibrator v;

    @Override
    protected void onResume(){
        super.onResume();

        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mDataReceiver == null) {
            mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID) {

                @Override
                public void receiveData(Context context, int transactionId, PebbleDictionary dict) {
                    // Always ACK
                    PebbleKit.sendAckToPebble(context, transactionId);
                    Log.i("receiveData", "Got message from Pebble!");

                    // Up received?
                    if(dict.getInteger(KEY_BUTTON_UP) != null) {
                        textView.setText("Up");
                    }

                    // Down received?
                    if(dict.getInteger(KEY_BUTTON_DOWN) != null) {
                        textView.setText("Down");
                        mMediaPlayer.stop();
                        v.cancel();
                    }

                    //Select Received
                    if (dict.getInteger(KEY_BUTTON_SELECT) != null){
                        textView.setText("Select");
                        forceRing();
                    }
                }

            };
            PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);
        }

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
        setContentView(com.example.donald.pebbleexample.R.layout.activity_main);
        launchButton = (Button) findViewById(com.example.donald.pebbleexample.R.id.start_app);
        findButton = (Button) findViewById(com.example.donald.pebbleexample.R.id.find_button);
        notificationButton = (Button) findViewById(com.example.donald.pebbleexample.R.id.sendNotification);
        stopButton = (Button) findViewById(com.example.donald.pebbleexample.R.id.stop_button);
        textView = (TextView)findViewById(com.example.donald.pebbleexample.R.id.text_view);

        //Ringtone Variables
        alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(this, alert);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.example.donald.pebbleexample.R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.example.donald.pebbleexample.R.id.action_settings) {
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

    public void startAppOnClick(View v) {
        Context context = getApplicationContext();

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
        Context context = getApplicationContext();

        if(checkIfConnected()){

            Toast.makeText(context, "Finding Phone", Toast.LENGTH_SHORT).show();

            stopButton.setVisibility(View.VISIBLE);

            // Send a time and distance to the sports app
            PebbleDictionary outgoing = new PebbleDictionary();
            outgoing.addInt32(RESULT_KEY, VIBRATE_BUTTON);
            PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, outgoing);

        }
    }

    public void stopButtonOnClick(View v) {
        Context context = getApplicationContext();

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

    public void forceRing(){


        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 500};
        v.vibrate(pattern,0);

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(true);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            try {
                mMediaPlayer.prepare();
                mMediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
