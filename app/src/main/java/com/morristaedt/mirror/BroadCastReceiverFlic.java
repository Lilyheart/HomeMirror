package com.morristaedt.mirror;

import android.content.Context;
import android.util.Log;

import io.flic.lib.FlicBroadcastReceiver;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicManager;

/**
 * Created by Lilyheart on 4/23/2016.
 */
public class BroadCastReceiverFlic extends FlicBroadcastReceiver {
    @Override
    protected void onRequestAppCredentials(Context context) {
        FlicConfig.setFlicCredentials();
    }

    @Override
    public void onButtonRemoved(Context context, FlicButton button) {
        // Button was removed
        Log.d("Button Removed", "True");
    }

    @Override
    public void onButtonUpOrDown(Context context, FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
        super.onButtonUpOrDown(context, button, wasQueued, timeDiff, isUp, isDown);
        if (isUp) {
            Log.d("IS UP", "True");
        } else {
            Log.d("IS DOWN", "True");
        }
    }
}
