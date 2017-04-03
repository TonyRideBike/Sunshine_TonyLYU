package com.dev.tonylyu.sunshine;

/*
  Created by tony lyu on 2017/3/8.
  Learn to use Loader.
 */

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * {@link ForecastAdapter} exposes a list of weather forecasts
 * from a {@link android.database.Cursor} to a {@link android.widget.ListView}.
 */
class ForecastAdapter extends CursorAdapter {

    private final String LOG_TAG = ForecastAdapter.class.getSimpleName();
    private final int VIEW_TYPE_TODAY = 0;
    private final int VIEW_TYPE_FUTURE = 1;

    ForecastAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    /*
        Remember that these views are reused as needed.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, "newView");
        }

        int viewType = getItemViewType(cursor.getPosition());
        int layoutID = -1;
        switch (viewType) {
            case VIEW_TYPE_TODAY:
                layoutID = R.layout.list_item_forecast_today;
                break;
            case VIEW_TYPE_FUTURE:
                layoutID = R.layout.list_item_forecast;
                break;
        }

        View view = LayoutInflater.from(context).inflate(layoutID, parent, false);
        ForecastViewHolder viewHolder = new ForecastViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? VIEW_TYPE_TODAY : VIEW_TYPE_FUTURE;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /*
                This is where we fill-in the views with the contents of the cursor.
             */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // our view is pretty simple here --- just a text view
        // we'll keep the UI functional with a simple (and slow!) binding.

        // Read weather icon ID from cursor
        int weatherId = cursor.getInt(ForecastFragment.COL_WEATHER_ID);
        // Use placeholder image for now
        ForecastViewHolder viewHolder = (ForecastViewHolder) view.getTag();
        viewHolder.weatherIconImageView.setImageResource(R.drawable.ic_clear);

        long dateMills = cursor.getLong(ForecastFragment.COL_WEATHER_DATE);
        viewHolder.dateTextView.setText(Utility.getFriendlyDateString(context, dateMills));

        String weatherCondition = cursor.getString(ForecastFragment.COL_WEATHER_DESC);
        viewHolder.weatherDescTextView.setText(weatherCondition);

        // Read user preference for metric or imperial temperature units
        boolean isMetric = Utility.isMetric(context);

        // Read high temperature from cursor
        double high = cursor.getDouble(ForecastFragment.COL_WEATHER_MAX_TEMP);

        viewHolder.highTempTextView.setText(Utility.formatTemperature(context, high, isMetric));

        double low = cursor.getDouble(ForecastFragment.COL_WEATHER_MIN_TEMP);
        viewHolder.lowTempTextView.setText(Utility.formatTemperature(context, low, isMetric));
    }

    /**
     * Cache of the children views for a forecast list item.
     */

    static class ForecastViewHolder {
        final ImageView weatherIconImageView;
        final TextView weatherDescTextView;
        final TextView dateTextView;
        final TextView highTempTextView;
        final TextView lowTempTextView;

        public ForecastViewHolder(View view) {
            this.weatherIconImageView = (ImageView) view.findViewById(R.id.list_item_icon);
            this.weatherDescTextView = (TextView) view.findViewById(R.id.list_item_forecast_textview);
            this.dateTextView = (TextView) view.findViewById(R.id.list_item_date_textview);
            this.highTempTextView = (TextView) view.findViewById(R.id.list_item_high_textview);
            this.lowTempTextView = (TextView) view.findViewById(R.id.list_item_low_textview);
        }
    }
}