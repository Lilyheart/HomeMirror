package com.morristaedt.mirror;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.morristaedt.mirror.configuration.ConfigurationSettings;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import io.flic.lib.FlicBroadcastReceiverFlags;
import io.flic.lib.FlicButton;
import io.flic.lib.FlicButtonCallback;
import io.flic.lib.FlicButtonCallbackFlags;
import io.flic.lib.FlicManager;
import io.flic.lib.FlicManagerInitializedCallback;

public class SetUpActivity extends AppCompatActivity {

    private static final String TAG = "SetUpActivity";
    private FlicManager manager;

    private static final long HOUR_MILLIS = 60 * 60 * 1000;
    private static final int METERS_MIN = 500;

    private ConfigurationSettings mConfigSettings;

    private LocationManager mLocationManager;

    @Nullable
    private LocationListener mLocationListener;

    @Nullable
    private Location mLocation;

    private RadioGroup mTemperatureChoice;
    private CheckBox mShowNextCaledarEventCheckbox;
    private CheckBox mXKCDCheckbox;
    private CheckBox mXKCDInvertCheckbox;
    private View mLocationView;
    private EditText mLatitude;
    private EditText mLongitude;
    List<String> stations = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        //Flic Button
        FlicManager.setAppCredentials(getString(R.string.flic_appID), getString(R.string.flic_appSecret), getString(R.string.flic_appName));

        FlicManager.getInstance(this, new FlicManagerInitializedCallback() {

            @Override
            public void onInitialized(FlicManager manager) {
                Log.d(TAG, "Ready to use manager");

                SetUpActivity.this.manager = manager;

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

        mConfigSettings = new ConfigurationSettings(this);

        mTemperatureChoice = (RadioGroup) findViewById(R.id.temperature_group);
        mTemperatureChoice.check(mConfigSettings.getIsCelsius() ? R.id.celsius : R.id.farenheit);

        mShowNextCaledarEventCheckbox = (CheckBox) findViewById(R.id.calendar_checkbox);
        mShowNextCaledarEventCheckbox.setChecked(mConfigSettings.showNextCalendarEvent());

        mXKCDCheckbox = (CheckBox) findViewById(R.id.xkcd_checkbox);
        mXKCDCheckbox.setChecked(mConfigSettings.showXKCD());

        mXKCDInvertCheckbox = (CheckBox) findViewById(R.id.xkcd_invert_checkbox);
        mXKCDInvertCheckbox.setChecked(mConfigSettings.invertXKCD());

        mLatitude = (EditText) findViewById(R.id.latitude);
        mLongitude = (EditText) findViewById(R.id.longitude);

        mLatitude.setText(String.valueOf(mConfigSettings.getLatitude()));
        mLongitude.setText(String.valueOf(mConfigSettings.getLongitude()));

        mLocationView = findViewById(R.id.location_view);
        setUpLocationMonitoring();

        setSeptaStations();
        Spinner dropdown = (Spinner)findViewById(R.id.railStations);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, stations);
        dropdown.setAdapter(adapter);

        findViewById(R.id.launch_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFields();

                Intent intent = new Intent(SetUpActivity.this, MirrorActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setSeptaStations() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected void onPostExecute(String s) {
            }

            @Override
            protected String doInBackground(Void... params) {
                try {
                    String[] nextLine;
                    HashMap<String, String> hmap = new HashMap<String, String>();

                    URL stationURL = new URL("http://www3.septa.org/hackathon/Arrivals/station_id_name.csv");
                    CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(stationURL.openStream())));
                    //clear first line
                    nextLine = reader.readNext();

                    while ((nextLine = reader.readNext()) != null) {
                        hmap.put(nextLine[0], nextLine[1]);
                        stations.add(nextLine[1]);
                    }

                    return null;
                } catch (Exception err) {
                    Log.d(TAG, "setSeptaStation: " + err);
                    return null;
                }
            }
        }.execute();
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
                saveFields();

                Intent intent = new Intent(SetUpActivity.this, MirrorActivity.class);
                startActivity(intent);

                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"Button Down");
                }
            });
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
                    Toast.makeText(SetUpActivity.this, "Grabbed a button", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SetUpActivity.this, "Did not grab any button", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null && mLocationListener != null) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void setUpLocationMonitoring() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        String provider = mLocationManager.getBestProvider(criteria, true);

        try {
            mLocation = mLocationManager.getLastKnownLocation(provider);

            if (mLocation == null) {
                mLocationView.setVisibility(View.VISIBLE);
                mLocationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Toast.makeText(SetUpActivity.this, R.string.found_location, Toast.LENGTH_SHORT).show();
                            mLocation = location;
                            mConfigSettings.setLatLon(String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
                            mLocationManager.removeUpdates(this);
                            if (mLocationView != null) {
                                mLocationView.setVisibility(View.GONE);
                            }
                        }
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {}

                    @Override
                    public void onProviderEnabled(String provider) {}

                    @Override
                    public void onProviderDisabled(String provider) {}
                };
                mLocationManager.requestLocationUpdates(provider, HOUR_MILLIS, METERS_MIN, mLocationListener);
            } else {
                mLocationView.setVisibility(View.GONE);
            }
        } catch (IllegalArgumentException e) {
            Log.e("SetUpActivity", "Location manager could not use provider", e);
        }
    }

    private void saveFields() {
        mConfigSettings.setIsCelsius(mTemperatureChoice.getCheckedRadioButtonId() == R.id.celsius);
        mConfigSettings.setShowNextCalendarEvent(mShowNextCaledarEventCheckbox.isChecked());
        mConfigSettings.setXKCDPreference(mXKCDCheckbox.isChecked(), mXKCDInvertCheckbox.isChecked());

        if (mLocation == null) {
            mConfigSettings.setLatLon(mLatitude.getText().toString(), mLongitude.getText().toString());
        } else {
            mConfigSettings.setLatLon(String.valueOf(mLocation.getLatitude()), String.valueOf(mLocation.getLongitude()));
        }

    }
}