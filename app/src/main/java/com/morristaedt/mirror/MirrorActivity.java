package com.morristaedt.mirror;

import android.content.Context;
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
import android.widget.Toast;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.CalendarModule;
import com.morristaedt.mirror.modules.DayModule;
import com.morristaedt.mirror.modules.ForecastModule;
import com.morristaedt.mirror.modules.MoodModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;

import java.lang.ref.WeakReference;
import java.util.List;

import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class MirrorActivity extends AppCompatActivity {

    private static final String TAG = "MirrorActivity";
    private FlicManager manager;

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private VerticalTextView mDayText;
    private VerticalTextView mWeatherSummary;
    private VerticalTextView mMoodText;
    private MoodModule mMoodModule;
    private VerticalTextView mCalendarTitleText;
    private VerticalTextView mCalendarDetailsText;

    private ScreenRotation ScreenButton = new ScreenRotation();

    private ForecastModule.ForecastListener mForecastListener = new ForecastModule.ForecastListener() {
        @Override
        public void onWeatherToday(String weatherToday) {
            if (!TextUtils.isEmpty(weatherToday)) {
                mWeatherSummary.setVisibility(View.VISIBLE);
                mWeatherSummary.setText(weatherToday);
            }
        }
    };

    private MoodModule.MoodListener mMoodListener = new MoodModule.MoodListener() {
        @Override
        public void onShouldGivePositiveAffirmation(final String affirmation) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mMoodText.setVisibility(affirmation == null ? View.GONE : View.VISIBLE);
                    mMoodText.setText(affirmation);
                }
            });
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

                MirrorActivity.this.manager = manager;

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

        //Gets the data to display
        mDayText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.day_text);
        mWeatherSummary = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.weather_summary);
        //mMoodText = (VerticalTextView) findViewById(R.id.mood_text);
        mCalendarTitleText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.calendar_title);
        mCalendarDetailsText = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.calendar_details);


        ViewStub stubHello = (ViewStub) findViewById(R.id.layout_stub);
        stubHello.setLayoutResource(R.layout.screen_hello);
        stubHello.inflate();

//        findViewById(R.id.hello_button).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MirrorActivity.this, MirrorXKCD.class);
//                startActivity(intent);
//            }
//        });

        setViewState();

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mMoodModule != null) {
            mMoodModule.release();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setViewState();
    }

    private void setViewState() {

        mDayText.setText(DayModule.getDay());

        int forecastApiKeyRes = getResources().getIdentifier("dark_sky_api_key", "string", getPackageName());
        int openWeatherApiKeyRes = getResources().getIdentifier("open_weather_api_key", "string", getPackageName());

        if (forecastApiKeyRes != 0) {
            ForecastModule.getForecastIOHourlyForecast(getString(forecastApiKeyRes), mConfigSettings.getForecastUnits(), mConfigSettings.getLatitude(), mConfigSettings.getLongitude(), mForecastListener);
        } else if (openWeatherApiKeyRes != 0) {
            ForecastModule.getOpenWeatherForecast(getString(openWeatherApiKeyRes), mConfigSettings.getForecastUnits(), mConfigSettings.getLatitude(), mConfigSettings.getLongitude(), mForecastListener);
        }

        if (mConfigSettings.showNextCalendarEvent()) {
            CalendarModule.getCalendarEvents(this, mCalendarListener);
        } else {
            mCalendarTitleText.setVisibility(View.GONE);
            mCalendarDetailsText.setVisibility(View.GONE);
        }

        if (mConfigSettings.showMoodDetection()) {
            mMoodModule = new MoodModule(new WeakReference<Context>(this));
            mMoodModule.getCurrentMood(mMoodListener);
        } //else {
//TODO            mMoodText.setVisibility(View.GONE);
//        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlarmReceiver.stopMirrorUpdates(this);
        Intent intent = new Intent(this, SetUpActivity.class);
        startActivity(intent);
    }

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

            if (!isDown) {
                Intent intent = new Intent(MirrorActivity.this, MirrorXKCD.class);
                startActivity(intent);
                return;
            }

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
                    Toast.makeText(MirrorActivity.this, "Grabbed a button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MirrorActivity.this, "Did not grab any button", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
