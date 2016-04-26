package com.morristaedt.mirror;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.TextView;

import com.morristaedt.mirror.configuration.ConfigurationSettings;
import com.morristaedt.mirror.modules.MoodModule;
import com.morristaedt.mirror.modules.SeptaModule;
import com.morristaedt.mirror.receiver.AlarmReceiver;
import com.squareup.picasso.Picasso;

/**
 * Created by Lee on 4/24/2016.
 *
 */
public class MirrorSepta extends AppCompatActivity {

    @NonNull
    private ConfigurationSettings mConfigSettings;

    private TextView mSeptaAlert;

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
        setContentView(R.layout.screen_septa);
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

        mSeptaAlert = (com.morristaedt.mirror.VerticalTextView) findViewById(R.id.septa_alert);

        //http://stackoverflow.com/questions/18999601/how-can-i-programmatically-include-layout-in-android
        ViewStub stub = (ViewStub) findViewById(R.id.layout_stub);
        stub.setLayoutResource(R.layout.screen_septa);
        View inflated = stub.inflate();

//        TODO Fix code for next button
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
        SeptaModule.getSingleAlert(mSeptaListener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AlarmReceiver.stopMirrorUpdates(this);
        Intent intent = new Intent(this, SetUpActivity.class);
        startActivity(intent);
    }
}