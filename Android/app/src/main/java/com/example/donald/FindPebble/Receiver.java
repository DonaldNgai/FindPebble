package com.example.donald.FindPebble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.getpebble.android.kit.Constants;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import org.json.JSONException;

import java.util.UUID;

/**
 * Created by donald on 01/12/15.
 */
public class Receiver extends BroadcastReceiver{

    private static final int KEY_BUTTON_UP = 0;
    private static final int KEY_BUTTON_SELECT = 1;
    private static final int KEY_BUTTON_DOWN = 2;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Constants.INTENT_APP_RECEIVE)) {
            final UUID receivedUuid = (UUID) intent.getSerializableExtra(Constants.APP_UUID);
            final UUID appUUID = UUID.fromString("b19073a4-2b69-4a20-8bf7-3df7d4deed25");

            // Pebble-enabled apps are expected to be good citizens and only inspect broadcasts containing their UUID
            if (!appUUID.equals(receivedUuid)) {
                Log.i("UUID Confirmation","Not my UUID");
                return;
            }

            final int transactionId = intent.getIntExtra(Constants.TRANSACTION_ID, -1);
            final String jsonData = intent.getStringExtra(Constants.MSG_DATA);
            if (jsonData == null || jsonData.isEmpty()) {
                Log.i("Pebble Receiver", "jsonData null");
                return;
            }

            try {
                final PebbleDictionary data = PebbleDictionary.fromJson(jsonData);
                // do what you need with the data
                Log.i("Pebble Receiver", "Data Received in receiver" + data.toJsonString());
                PebbleKit.sendAckToPebble(context, transactionId);
                Intent activityIntent = new Intent(context,Vibrate.class);;


                String name = "PEBBLE_DATA";
                String value = "";
                if(data.getInteger(KEY_BUTTON_UP) != null) {
                    activityIntent = new Intent(context,MainActivity.class);
                    value = Integer.toString(KEY_BUTTON_UP);
                }
                if(data.getInteger(KEY_BUTTON_SELECT) != null) {

                    value = Integer.toString(KEY_BUTTON_SELECT);
                }
                if(data.getInteger(KEY_BUTTON_DOWN) != null) {

                    value = Integer.toString(KEY_BUTTON_DOWN);

                }

                activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                activityIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

                activityIntent.putExtra(name, value);

                context.startActivity(activityIntent);
            } catch (JSONException e) {
                Log.i("Pebble Receiver", "failed reived -> dict" + e);
            }
        }
    }

}

