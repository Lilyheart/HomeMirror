package com.morristaedt.mirror;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.DayModule;
import com.morristaedt.mirror.modules.SeptaModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;

import java.util.List;

import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

/**
 * Created by Lee on 4/24/2016.
 */
public class MirrorSepta extends AppCompatActivity {

    private static final String TAG = "MirrorSepta";
    private FlicManager manager;

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private TextView mSeptaAlert;
    private VerticalTextView mDayText;

    private SeptaModule.SeptaListener mSeptaListener = new SeptaModule.SeptaListener() {
        @Override
        public void onNewAlert(String alert) {
            if (TextUtils.isEmpty(alert)) {
                mSeptaAlert.setVisibility(View.GONE);
            } else {
                mSeptaAlert.setVisibility(View.VISIBLE);
                mSeptaAlert.setText(alert);
                mSeptaAlert.setSelected(true);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mirror);
        mConfigSettings = new ConfigurationSettings(this);
        AlarmReceiver.startMirrorUpdates(this);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_IMMERSIVE;
            decorView.setSystemUiVisibility(uiOptions);
            ActionBar actionBar = getSupportActionBar();
            actionBar.hide();

        }

        //Flic Button
        FlicConfig.setFlicCredentials();

        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

            @Override
            public void onInitialized(FlicManager manager) {
                Log.d(TAG, "Ready to use manager");

                MirrorSepta.this.manager = manager;

                // Restore buttons grabbed in a previous run of the activity
                List<FlicButton> buttons = manager.getKnownButtons();
                for (FlicButton button : buttons) {
                    String status = null;
                    switch (button.getConnectionStatus()) {
                        case FlicButton.BUTTON_DISCONNECTED:
                            status = "disconnected";
                            break;
                        case FlicButton.BUTTON_CONNECTION_STARTED:
                            status = "connection started";
                            break;
                        case FlicButton.BUTTON_CONNECTION_COMPLETED:
                            status = "connection completed";
                            break;
                    }
                    Log.d(TAG, "Found an existing button: " + button + ", status: " + status);
                    setButtonCallback(button);
                }
            }
        });

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        ViewStub stubSepta = (ViewStub) findViewById(R.id.layout_stub);
        stubSepta.setLayoutResource(R.layout.screen_septa);
        stubSepta.inflate();

        mSeptaAlert = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.septa_alert);
        mDayText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.day_text);


        //http://stackoverflow.com/questions/18999601/how-can-i-programmatically-include-layout-in-android

//        findViewById(R.id.septa_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MirrorSepta.this, MirrorActivity.class);
//                startActivity(intent);
//            }
//        });

        setViewState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setViewState();
    }

    private void setViewState() {

        mDayText.setText(DayModule.getDay());

        SeptaModule.getSingleAlert(mSeptaListener);
    }

//    @Override
//    public void onBackPressed() {
////        super.onBackPressed();
////        AlarmReceiver.stopMirrorUpdates(this);
////        Intent intent = new Intent(this, SetUpActivity.class);
////        startActivity(intent);
//    }

    private void setButtonCallback(FlicButton button) {
        button.removeAllFlicButtonCallbacks();
        button.addFlicButtonCallback(buttonCallback);
        button.setFlicButtonCallbackFlags(FlicButtonCallbackFlags.UP_OR_DOWN);
        button.setActiveMode(true);
    }

    private FlicButtonCallback buttonCallback = new FlicButtonCallback() {
        @Override
        public void onButtonUpOrDown(FlicButton button, boolean wasQueued, int timeDiff, boolean isUp, boolean isDown) {
            final String text = button + " was " + (isDown ? "pressed" : "released");
            Log.d(TAG, text);

            if (!isDown)
                return;
                Intent intent = new Intent(MirrorSepta.this, MirrorActivity.class);
                startActivity(intent);

//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    Log.d(TAG, "Button Down");
//                }
//            });
        }
    };

    public void grabButton(View v) {
        if (manager != null) {
            manager.initiateGrabButton(this);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Log.d("OnActivity", "True");
        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {
            @Override
            public void onInitialized(FlicManager manager) {
                FlicButton button = manager.completeGrabButton(requestCode, resultCode, data);
                if (button != null) {
                    button.registerListenForBroadcast(FlicBroadcastReceiverFlags.UP_OR_DOWN | FlicBroadcastReceiverFlags.REMOVED);
                    Toast.makeText(MirrorSepta.this, "Grabbed a button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MirrorSepta.this, "Did not grab any button", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}