package com.morristaedt.mirror;

import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.CalendarModule;
import com.morristaedt.mirror.modules.DayModule;
import com.morristaedt.mirror.modules.XKCDModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;
import com.squareup.picasso.Picasso;

import java.util.List;

import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

/**
 * Created by Lilyheart on 4/23/2016.
 */
public class MirrorXKCD extends ActionBarActivity {

    private static final String TAG = "MirrorXKCD";
    private FlicManager manager;

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private ImageView mXKCDImage;
    private VerticalTextView mDayText;
    private VerticalTextView mCalendarTitleText;
    private VerticalTextView mCalendarDetailsText;

    private XKCDModule.XKCDListener mXKCDListener = new XKCDModule.XKCDListener() {
        @Override
        public void onNewXKCDToday(String url) {
            if (TextUtils.isEmpty(url)) {
                mXKCDImage.setVisibility(View.GONE);
            } else {
                Picasso.with(MirrorXKCD.this).load(url).into(mXKCDImage);
                mXKCDImage.setVisibility(View.VISIBLE);
            }
        }
    };

    private CalendarModule.CalendarListener mCalendarListener = new CalendarModule.CalendarListener() {
        @Override
        public void onCalendarUpdate(String title, String details) {
            mCalendarTitleText.setVisibility(title != null ? View.VISIBLE : View.GONE);
            mCalendarTitleText.setText(title);
            mCalendarDetailsText.setVisibility(details != null ? View.VISIBLE : View.GONE);
            mCalendarDetailsText.setText(details);

            //Make marquee effect work for long text
            mCalendarTitleText.setSelected(true);
            mCalendarDetailsText.setSelected(true);
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
        FlicManager.setAppCredentials(getString(R.string.flic_appID), getString(R.string.flic_appSecret), getString(R.string.flic_appName));

        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

            @Override
            public void onInitialized(FlicManager manager) {
                Log.d(TAG, "Ready to use manager");

                MirrorXKCD.this.manager = manager;

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

        //http://stackoverflow.com/questions/18999601/how-can-i-programmatically-include-layout-in-android
        ViewStub stubXKCD = (ViewStub) findViewById(R.id.layout_stub);
        stubXKCD.setLayoutResource(R.layout.screen_xkcd);
        stubXKCD.inflate();

//        TextView mainTextView = (TextView) findViewById(R.id.main_textview);
//        mainTextView.setText("Button pressed!");

        mXKCDImage = (ImageView) findViewById(R.id.xkcd_image);
        mDayText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.day_text);
        mCalendarTitleText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.calendar_title);
        mCalendarDetailsText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.calendar_details);


        if (mConfigSettings.invertXKCD()) {
            //Negative of XKCD image
            float[] colorMatrixNegative = {
                    -1.0f, 0, 0, 0, 255, //red
                    0, -1.0f, 0, 0, 255, //green
                    0, 0, -1.0f, 0, 255, //blue
                    0, 0, 0, 1.0f, 0 //alpha
            };
            ColorFilter colorFilterNegative = new ColorMatrixColorFilter(colorMatrixNegative);
            mXKCDImage.setColorFilter(colorFilterNegative); // not inverting for now
        }

//        findViewById(R.id.xkcd_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MirrorXKCD.this, MirrorSepta.class);
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

        if (mConfigSettings.showNextCalendarEvent()) {
            CalendarModule.getCalendarEvents(this, mCalendarListener);
        } else {
            mCalendarTitleText.setVisibility(View.GONE);
            mCalendarDetailsText.setVisibility(View.GONE);
        }

        if (mConfigSettings.showXKCD()) {
            XKCDModule.getXKCDForToday(mXKCDListener);
        } else {
            mXKCDImage.setVisibility(View.GONE);
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        AlarmReceiver.stopMirrorUpdates(this);
//        Intent intent = new Intent(this, SetUpActivity.class);
//        startActivity(intent);
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
                Intent intent = new Intent(MirrorXKCD.this, MirrorSepta.class);
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
                    Toast.makeText(MirrorXKCD.this, "Grabbed a button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MirrorXKCD.this, "Did not grab any button", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
