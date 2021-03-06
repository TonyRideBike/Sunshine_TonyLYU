package com.dev.tonylyu.sunshine;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dev.tonylyu.sunshine.data.WeatherContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

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
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();

    private final String SELECTED_POSITION = "selected_position";
    private int mPosistion = ListView.INVALID_POSITION;
    private ListView mListview;

    private ForecastAdapter mForecastAdapter;
    private CursorLoader mCursorLoader;
    private ForecastFragmentCallback mCallbackListener;

    public ForecastFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallbackListener = (ForecastFragment.ForecastFragmentCallback) context;
        } catch (ClassCastException e) {
            Log.e(LOG_TAG, " MainActivity must implement ForecastFragmentCallback", e);
            throw e;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        getLoaderManager().initLoader(WEATHER_LOADER_ID, null, this);
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onActivityCreated");
        }
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mListview = (ListView) rootView.findViewById(R.id.list_view_forecast);
        mListview.setAdapter(mForecastAdapter);

        mListview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                if (BuildConfig.DEBUG) {
                    Log.d(LOG_TAG, "forecast list item onClick: number " + Integer.toString(position));
                }

                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor == null) {
                    return;
                }
                String locationSetting = Utility.getPreferredLocation(getActivity());
                Uri uri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                        locationSetting, cursor.getLong(COL_WEATHER_DATE));
                mCallbackListener.onItemSelected(uri);
                mPosistion = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_POSITION)) {
            mPosistion = savedInstanceState.getInt(SELECTED_POSITION);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onSaveInstanceState");
        }

        if (mPosistion != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_POSITION, mPosistion);
        }
        super.onSaveInstanceState(outState);
    }

    private void updateWeather() {

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "updateWeather Start.");
        }

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());

        weatherTask.execute(location);
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location, System.currentTimeMillis()
        );

        if (mCursorLoader == null) {
            Log.e(LOG_TAG, "mCursorLoader == NULL ???");
            getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
            Log.e(LOG_TAG, Boolean.toString(mCursorLoader == null));
        }

        mCursorLoader.setUri(weatherForLocationUri);
    }

    /**
     * Learn to Create a Loader
     * 1. Create a Loader ID;
     * 2. Fill-in Loader Callback;
     * 3. Initialize the Loader with LoaderManager;
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onCreateLoader");
        }
        String locationSetting = Utility.getPreferredLocation(getContext());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis()
        );

        mCursorLoader = new CursorLoader(getContext(),
                weatherForLocationUri,
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
        mForecastAdapter.swapCursor(data);

        if (mPosistion != ListView.INVALID_POSITION) {
            mListview.smoothScrollToPosition(mPosistion);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onLoaderReset");
        }
        mForecastAdapter.swapCursor(null);
    }

    public void onLocationChanged() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "onLocationChanged");
        }
        updateWeather();
//        getLoaderManager().restartLoader(WEATHER_LOADER_ID, null, this);
    }


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    interface ForecastFragmentCallback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri dateUri);
    }

}