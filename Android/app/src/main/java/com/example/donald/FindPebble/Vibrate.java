package com.example.donald.FindPebble;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class Vibrate extends AppCompatActivity {

    private static final int KEY_BUTTON_UP = 0;
    private static final int KEY_BUTTON_SELECT = 1;
    private static final int KEY_BUTTON_DOWN = 2;

    private Intent intent;
    private String pebbleData;

    private Uri alert;
    private MediaPlayer mMediaPlayer;
    private AudioManager audioManager;
    private Vibrator v;
    private int buttonPressed = 0;
    private int oldVolume = 100;

    @Override
    protected void onNewIntent(Intent intent) {
        pebbleData = intent.getStringExtra("PEBBLE_DATA");
        buttonPressed = Integer.parseInt(pebbleData);
        Log.d("FindPebble", "onNewIntent called" + pebbleData);

        if (buttonPressed == KEY_BUTTON_DOWN){
            stopRing();
        }

        super.onNewIntent(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibrate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Ringtone Variables
        alert = RingtoneManager
                .getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        mMediaPlayer = new MediaPlayer();

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        intent = getIntent();
        pebbleData = intent.getStringExtra("PEBBLE_DATA");
        Log.d("Vibrate Class", "Pebble-data: " + pebbleData);
        buttonPressed = Integer.parseInt(pebbleData);

        switch (buttonPressed) {
            case KEY_BUTTON_UP:
                Log.d("Vibrate Class", "Up");
                Context context = getApplicationContext();

                Intent activityIntent = new Intent(context,Vibrate.class);

                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(activityIntent);
                break;
            case KEY_BUTTON_DOWN:
                //textView.setText("Down");
                Log.d("Vibrate Class", "Down");
                stopRing();
                break;
            case KEY_BUTTON_SELECT:
                //textView.setText("Select");
                Log.d("Vibrate Class", "Select");
                forceRing();
                break;
        }

    }

    public void stopVibrateButtonOnClick(View v) {
        stopRing();
    }

    public void stopRing(){
        if (mMediaPlayer.isPlaying()) {
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, oldVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            mMediaPlayer.stop();
        }
        v.cancel();
        finish();
    }

    public void forceRing(){
        // Start without a delay
        // Vibrate for 100 milliseconds
        // Sleep for 1000 milliseconds
        long[] pattern = {0, 1000, 500};
        v.vibrate(pattern,0);

        try {
            mMediaPlayer.reset();
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

        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            oldVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setLooping(true);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);
            try {
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }

            //mp3 will be started after completion of preparing...
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

                @Override
                public void onPrepared(MediaPlayer player) {
                    player.start();
                }

            });

        }
    }

}


