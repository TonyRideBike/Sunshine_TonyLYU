package com.dev.tonylyu.sunshine;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dev.tonylyu.sunshine.data.WeatherContract;

/**
 * A simple {@link Fragment} subclass.
 */
public class ForecastFragment extends Fragment {

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

        String location_setting = Utility.getPreferredLocation(getContext());

        String sort_order = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
        Uri weatherLocationUri = WeatherContract.WeatherEntry.buildWeatherLocationWithStartDate(
                location_setting, System.currentTimeMillis());
        Cursor cursor = getContext().getContentResolver().query(
                weatherLocationUri,
                null, null, null, sort_order
        );

        mForecastAdapter = new ForecastAdapter(getContext(), cursor, 0);

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
        FetchWeatherTask weatherTask = new FetchWeatherTask(getContext());
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());
        String location = sharedPreferences.getString(getString(R.string.pref_location_key),
                getString(R.string.pref_location_default));

        weatherTask.execute(location);
    }

}