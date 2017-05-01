package com.dev.tonylyu.sunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements ForecastFragment.ForecastFragmentCallback {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String DETAILFRAGMENT_TAG = "DFTAG";
    private String mLocation;
    private boolean mTwoPane;
    private DetailFragment detailFragment;
    private ForecastFragment forecastFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onCreate");
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLocation = Utility.getPreferredLocation(this);
        forecastFragment = (ForecastFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_forecast);

        if (findViewById(R.id.weather_detail_container) != null) {
            detailFragment = new DetailFragment();
            mTwoPane = true;
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction().replace(
                        R.id.weather_detail_container,
                        detailFragment,
                        DETAILFRAGMENT_TAG
                ).commit();
            }
        } else {
            mTwoPane = false;
        }
    }

    @Override
    protected void onResume() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onResume");
        }
        super.onResume();

        String location = Utility.getPreferredLocation(this);
        if (!Objects.equals(mLocation, location) && location != null) {
            if (forecastFragment != null) {
                forecastFragment.onLocationChanged();
            }

            if (detailFragment != null) {
                detailFragment.onLocationChanged(location);
            }

            mLocation = location;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_open_map) {
            openPreferredLocationInMap();
        }

        return super.onOptionsItemSelected(item);
    }

    private void openPreferredLocationInMap() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String location = pref.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));
        Uri uri = Uri.parse("geo:0,0?").buildUpon()
                .appendQueryParameter("q", location).build();
        Log.d(LOG_TAG, "uri = " + uri.toString());

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    @Override
    public void onItemSelected(Uri dateUri) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onItemSelected");
        }

        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.URI_KEY, dateUri);

            detailFragment = new DetailFragment();
            detailFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.weather_detail_container, detailFragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).setData(dateUri);
            startActivity(intent);
        }
    }
}
