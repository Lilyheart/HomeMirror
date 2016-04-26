package com.morristaedt.mirror;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrixColorFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.CalendarModule;
import com.morristaedt.mirror.modules.DayModule;
import com.morristaedt.mirror.modules.ForecastModule;
import com.morristaedt.mirror.modules.MoodModule;
import com.morristaedt.mirror.modules.XKCDModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;
import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;

public class MirrorActivity extends AppCompatActivity {

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private TextView mDayText;
    private TextView mWeatherSummary;
    private TextView mBikeTodayText;
    private TextView mMoodText;
    private MoodModule mMoodModule;
    private TextView mCalendarTitleText;
    private TextView mCalendarDetailsText;

    private ScreenRotation ScreenButton = new ScreenRotation();

    private ForecastModule.ForecastListener mForecastListener = new ForecastModule.ForecastListener() {
        @Override
        public void onWeatherToday(String weatherToday) {
            if (!TextUtils.isEmpty(weatherToday)) {
                mWeatherSummary.setVisibility(View.VISIBLE);
                mWeatherSummary.setText(weatherToday);
            }
        }

        @Override
        public void onShouldBike(boolean showToday, boolean shouldBike) {
            if (mConfigSettings.showBikingHint()) {
                mBikeTodayText.setVisibility(showToday ? View.VISIBLE : View.GONE);
                mBikeTodayText.setText(shouldBike ? R.string.bike_today : R.string.no_bike_today);
            } else {
                mBikeTodayText.setVisibility(View.GONE);
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

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //Gets the data to display
        mDayText = (TextView) findViewById(R.id.day_text);
        mWeatherSummary = (TextView) findViewById(R.id.weather_summary);
        mBikeTodayText = (TextView) findViewById(R.id.can_bike);
        mMoodText = (TextView) findViewById(R.id.mood_text);
        mCalendarTitleText = (TextView) findViewById(R.id.calendar_title);
        mCalendarDetailsText = (TextView) findViewById(R.id.calendar_details);

        setViewState();

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.screen_septa);
        View inflated = stub.inflate();

        findViewById(R.id.hello_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MirrorActivity.this, MirrorXKCD.class);
                startActivity(intent);
            }
        });
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

//TODO        mDayText.setText(DayModule.getDay());
        // Get the API key for whichever weather service API key is available
        // These should be declared as a string in xml
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
//TODO            mCalendarTitleText.setVisibility(View.GONE);
//TODO            mCalendarDetailsText.setVisibility(View.GONE);
        }

        if (mConfigSettings.showMoodDetection()) {
            mMoodModule = new MoodModule(new WeakReference<Context>(this));
            mMoodModule.getCurrentMood(mMoodListener);
        } else {
//TODO            mMoodText.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlarmReceiver.stopMirrorUpdates(this);
        Intent intent = new Intent(this, SetUpActivity.class);
        startActivity(intent);
    }
}
