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

/**
 * Created by Lilyheart on 4/23/2016.
 */
public class MirrorXKCD extends ActionBarActivity {

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private ImageView mXKCDImage;

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

        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.screen_xkcd);
        View inflated = stub.inflate();

//        TextView mainTextView = (TextView) findViewById(R.id.main_textview);
//        mainTextView.setText("Button pressed!");

        mXKCDImage = (ImageView) findViewById(R.id.xkcd_image);

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

        setViewState();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setViewState();
    }

    private void setViewState() {
        if (mConfigSettings.showXKCD()) {
            XKCDModule.getXKCDForToday(mXKCDListener);
        } else {
            mXKCDImage.setVisibility(View.GONE);
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
