/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dev.tonylyu.sunshine;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dev.tonylyu.sunshine.data.WeatherContract;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new DetailFragment())
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        Log.d(LOG_TAG, "onResume");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(LOG_TAG, "onPause");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(LOG_TAG, "onStop");
        super.onStop();
    }

    @Override
    protected void onStart() {
        Log.d(LOG_TAG, "onStart");
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

        // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
        // must change.
        static final int COL_WEATHER_ID = 0;
        static final int COL_WEATHER_DATE = 1;
        static final int COL_WEATHER_DESC = 2;
        static final int COL_WEATHER_MAX_TEMP = 3;
        static final int COL_WEATHER_MIN_TEMP = 4;
        static final int COL_LOCATION_SETTING = 5;
        static final int COL_WEATHER_CONDITION_ID = 6;
        static final int COL_COORD_LAT = 7;
        static final int COL_COORD_LONG = 8;
        private static final String SHARE_TEXT_HASHTAG = " #SunshineApp";
        private static final int DETAIL_LOADER = 0;
        private static final int WEATHER_LOADER_ID = 101;
        private static final String[] FORECAST_COLUMNS = {
                // In this case the id needs to be fully qualified with a table name, since
                // the content provider joins the location & weather tables in the background
                // (both have an _id column)
                // On the one hand, that's annoying.  On the other, you can search the weather table
                // using the location set by the user, which is only in the Location table.
                // So the convenience is worth it.
                WeatherContract.WeatherEntry.TABLE_NAME + "." + WeatherContract.WeatherEntry._ID,
                WeatherContract.WeatherEntry.COLUMN_DATE,
                WeatherContract.WeatherEntry.COLUMN_SHORT_DESC,
                WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
                WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
                WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING,
                WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
                WeatherContract.LocationEntry.COLUMN_COORD_LAT,
                WeatherContract.LocationEntry.COLUMN_COORD_LONG
        };
        private String LOG_TAG = DetailFragment.class.getSimpleName();
        private String mForecastStr;
        private ShareActionProvider mShareActionProvider;
        private CursorLoader mCursorLoader;

        public DetailFragment() {
            setHasOptionsMenu(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onCreateView");
            }

            setHasOptionsMenu(true);

            return inflater.inflate(R.layout.fragment_detail, container, false);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onCreateOptionsMenu");
            }

            inflater.inflate(R.menu.detailfragment, menu);

            /*
              menu.getItem() use index as param
              should use findItem() using id as param
             */
            MenuItem menuItem = menu.findItem(R.id.action_share);

            /*
              interesting MenuItemCompat.
             */
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
            setShareIntent(makeShareIntent());

            super.onCreateOptionsMenu(menu, inflater);
        }

        private void setShareIntent(Intent shareIntent) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "setShareIntent");
            }

            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(shareIntent);
            } else {
                Log.e(LOG_TAG, "ShareActionProvider is null?");
            }
        }

        private Intent makeShareIntent() {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            // TODO: 5/11/2016 do more research on MIME
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mForecastStr + SHARE_TEXT_HASHTAG);
            return shareIntent;
        }

        @Override
        public void onActivityCreated(@Nullable Bundle savedInstanceState) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onActivityCreated");
            }

            getLoaderManager().initLoader(DETAIL_LOADER, null, this);
            super.onActivityCreated(savedInstanceState);
        }

        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onCreateLoader");
            }

            Intent intent = getActivity().getIntent();
            if (null == intent) {
                return null;
            }

            String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
            mCursorLoader = new CursorLoader(getContext(),
                    intent.getData(),
                    FORECAST_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
            return mCursorLoader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onLoadFinished");
            }

            TextView textView = (TextView) getActivity().findViewById(R.id.textview_detail);

            if (!data.moveToFirst()) {
                return;
            }

            String dateString = Utility.formatDate(data.getLong(DetailFragment.COL_WEATHER_DATE));
            String weatherDescription = data.getString(DetailFragment.COL_WEATHER_DESC);
            boolean isMetric = Utility.isMetric(getActivity());
            String tempHigh = Utility.formatTemperature(data.getDouble(COL_WEATHER_MAX_TEMP), isMetric);
            String tempLow = Utility.formatTemperature(data.getDouble(COL_WEATHER_MIN_TEMP), isMetric);
            mForecastStr = String.format("%s - %s -%s/%s", dateString, weatherDescription, tempHigh, tempLow);

            textView.setText(mForecastStr);

            if (null != mShareActionProvider) {
                mShareActionProvider.setShareIntent(makeShareIntent());
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            if (BuildConfig.DEBUG) {
                Log.d(LOG_TAG, "onLoaderReset");
            }

        }
    }
}