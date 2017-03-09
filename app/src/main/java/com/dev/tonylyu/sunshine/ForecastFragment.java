package com.dev.tonylyu.sunshine;


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
import android.widget.ListView;

import com.dev.tonylyu.sunshine.data.WeatherContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int WEATHER_LOADER_ID = 101;
    private final String LOG_TAG = ForecastFragment.class.getSimpleName();
    ForecastAdapter mForecastAdapter;

    public ForecastFragment() {
        // Required empty public constructor
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
                             Bundle savedInstanceState) {

        // Now that we have some dummy forecast data, create an ArrayAdapter.
        // The ArrayAdapter will take data from a source (like our dummy forecast) and
        // use it to populate the ListView it's attached to.
//        mForecastAdapter =
//                new ArrayAdapter<>(
//                        getActivity(), // The current context (this activity)
//                        R.layout.list_item_forecast, // The name of the layout ID.
//                        R.id.list_item_forecast_textview, // The ID of the textview to populate.
//                        new ArrayList<String>());

//        String location_setting = Utility.getPreferredLocation(getContext());
//
//        String sort_order = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
//        Uri weatherLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
//                location_setting, System.currentTimeMillis());
//        Cursor cursor = getContext().getContentResolver().query(
//                weatherLocationUri,
//                null, null, null, sort_order
//        );
//
//        mForecastAdapter = new ForecastAdapter(getContext(), cursor, 0);

        mForecastAdapter = new ForecastAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_view_forecast);
        listView.setAdapter(mForecastAdapter);
//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String forecast = mForecastAdapter.getItem(position);
//                Intent intent = new Intent(getActivity(), DetailActivity.class);
//                intent.putExtra(Intent.EXTRA_TEXT, forecast);
//                startActivity(intent);
//            }
//        });

        return rootView;
    }

    private void updateWeather() {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "updateWeather Start.");
        }

        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity());
        String location = Utility.getPreferredLocation(getActivity());

        weatherTask.execute(location);
    }

    /**
     * Learn to Create a Loader
     * 1. Create a Loader ID;
     * 2. Fill-in Loader Callback;
     * 3. Initialize the Loader with LoaderManager;
     */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String locationSetting = Utility.getPreferredLocation(getActivity());
        String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherForLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithDate(
                locationSetting, System.currentTimeMillis());
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "weatherForLocationUri = " + weatherForLocationUri.toString());
        }

        return new CursorLoader(getActivity(),
                weatherForLocationUri,
                null,
                null,
                null,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }

}