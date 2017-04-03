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
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dev.tonylyu.sunshine.data.WeatherContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // These indices are tied to FORECAST_COLUMNS.  If FORECAST_COLUMNS changes, these
    // must change.
    static final int COL_WEATHER_ID = 0;
    static final int COL_WEATHER_DATE = 1;
    static final int COL_WEATHER_DESC = 2;
    static final int COL_WEATHER_MAX_TEMP = 3;
    static final int COL_WEATHER_MIN_TEMP = 4;
    static final int COL_WEATHER_HUMIDITY = 5;
    static final int COL_WEATHER_PRESSURE = 6;
    static final int COL_WEATHER_WIND_SPEED = 7;
    static final int COL_WEATHER_DEGREES = 8;
    static final int COL_WEATHER_CONDITION_ID = 9;
    private static final String LOG_TAG = DetailFragment.class.getSimpleName();
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
            WeatherContract.WeatherEntry.COLUMN_HUMIDITY,
            WeatherContract.WeatherEntry.COLUMN_PRESSURE,
            WeatherContract.WeatherEntry.COLUMN_WIND_SPEED,
            WeatherContract.WeatherEntry.COLUMN_DEGREES,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
            WeatherContract.LocationEntry.COLUMN_LOCATION_SETTING
    };

    private String mForecastStr;
    private ShareActionProvider mShareActionProvider;
    private CursorLoader mCursorLoader;

    private TextView mDay;
    private TextView mDate;
    private TextView mHighTemp;
    private TextView mLowTemp;
    private TextView mDescription;
    private TextView mHumidity;
    private TextView mWind;
    private TextView mPressure;
    private ImageView mWeatherIcon;

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

        View rootview = inflater.inflate(R.layout.fragment_detail, container, false);

        mDay = (TextView) rootview.findViewById(R.id.detail_fragment_textview_day);
        mDate = (TextView) rootview.findViewById(R.id.detail_fragment_textview_date);
        mHighTemp = (TextView) rootview.findViewById(R.id.detail_fragment_textview_hightemp);
        mLowTemp = (TextView) rootview.findViewById(R.id.detail_fragment_textview_lowtemp);
        mDescription = (TextView) rootview.findViewById(R.id.detail_fragment_textview_description);
        mHumidity = (TextView) rootview.findViewById(R.id.detail_fragment_textview_humid);
        mWind = (TextView) rootview.findViewById(R.id.detail_fragment_textview_wind);
        mPressure = (TextView) rootview.findViewById(R.id.detail_fragment_textview_pressure);
        mWeatherIcon = (ImageView) rootview.findViewById(R.id.detail_fragment_iconart_weather);

        return rootview;
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
            Log.d(LOG_TAG, "data good?" + data.toString());
        }

        if (!data.moveToFirst()) {
            Log.d(LOG_TAG, "cursor data error" + data.toString());
            return;
        }

        long date = data.getLong(COL_WEATHER_DATE);
        String dayText = Utility.getDayName(getContext(), date);
        String dateText = Utility.formatDate(date);
        mDay.setText(dayText);
        mDate.setText(dateText);

        String description = data.getString(COL_WEATHER_DESC);
        mDescription.setText(description);

        boolean isMetric = Utility.isMetric(getContext());
        double high = data.getDouble(COL_WEATHER_MAX_TEMP);
        double low = data.getDouble(COL_WEATHER_MIN_TEMP);
        String highTempText = Utility.formatTemperature(getContext(), high, isMetric);
        String lowTempText = Utility.formatTemperature(getContext(), low, isMetric);
        mHighTemp.setText(highTempText);
        mLowTemp.setText(lowTempText);

        float humidity = data.getFloat(COL_WEATHER_HUMIDITY);
        mHumidity.setText(String.format(getContext().getString(R.string.format_humidity), humidity));

        float windSpeed = data.getFloat(COL_WEATHER_WIND_SPEED);
        float windDirection = data.getFloat(COL_WEATHER_DEGREES);
        String windText;
        if (isMetric) {
            windText = String.format(getContext().getString(R.string.format_wind_kmh), windSpeed,
                    Utility.getDirection(windDirection));
        } else {
            windText = String.format(getContext().getString(R.string.format_wind_mph), windSpeed,
                    Utility.getDirection(windDirection));
        }
        mWind.setText(windText);

        float pressure = data.getFloat(COL_WEATHER_PRESSURE);
        mPressure.setText(String.format(getContext().getString(R.string.format_pressure), pressure));

        mForecastStr = String.format("%s - %s - %s/%s", dateText, description, highTempText, lowTempText);
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
