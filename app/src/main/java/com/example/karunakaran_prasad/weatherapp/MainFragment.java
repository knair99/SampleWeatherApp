package com.example.karunakaran_prasad.weatherapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;


/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {

    public ArrayAdapter<String> mForeCastAdapter;
    public static final String URL_STRING_BASE = "http://api.openweathermap.org/data/2.5/forecast/daily?q=";
    public static final String URL_CITY ="Queens";
    public static final String URL_PARAMS = "&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e&units=imperial";



    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        UpdateWeatherInfo();
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.menu_refresh){


            UpdateWeatherInfo();

        }
        else if(id == R.id.menu_settings){
            Intent settings_intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(settings_intent);
        }
        

        return super.onOptionsItemSelected(item);
    }

    private void UpdateWeatherInfo() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String weather_city = prefs.getString("location_preference_key", URL_CITY);
        if (!weather_city.equals(null)) {
            new FetchWeatherTask().execute(weather_city);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View root_view = inflater.inflate(R.layout.fragment_main, container, false);

        Log.d("MainFragment", "onCreateView");

        // Inflate the layout for this fragment
        String[] dummy_weather_strings = {
        "Today - Sunny - 78/63",
        "Tomorrow - Rainy - 72/60",
        "Wednesday - Sunny - 77/59"};

        List<String> dummy_weather_data = new ArrayList<>(Arrays.asList(dummy_weather_strings));

        mForeCastAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                dummy_weather_data);

        ListView forecast_list_view = (ListView) root_view.findViewById (R.id.listview_forecast);
        forecast_list_view.setAdapter(mForeCastAdapter);

        forecast_list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = null;
                s = mForeCastAdapter.getItem(position);
                //Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                Intent detailIntent = new Intent(getContext(), DetailActivity.class);
                detailIntent.putExtra("DETAIL_DAY", s);
                startActivity(detailIntent);
            }
        });

        return root_view;
    }

    private class FetchWeatherTask extends AsyncTask<String, Integer, String[]> {

        @Override
        protected String[] doInBackground(String... args){
            String[] weatherStrings = null;

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            String url_weather = URL_STRING_BASE + args[0] + URL_PARAMS;

            Log.d("MainFragment", "FetchWeatherTask: doInBackground : "  + url_weather);
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                URL url = new URL(url_weather);

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
                Log.d("MainFragment", forecastJsonStr);


                weatherStrings = getWeatherDataFromJson(forecastJsonStr, 7);

                for(String s: weatherStrings){

                    Log.d("MainFragment", s);
                }

            } catch (IOException e) {
                Log.e("MainFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } catch (JSONException e) {
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("MainFragment", "Error closing stream", e);
                    }
                }
            }

        return weatherStrings;//for success
        }//End of doInBackground

        protected void onPostExecute(String[] result) {
            Log.d("MainFragment", "Done executing in the background");
            //Toast.makeText(this, "Downloaded " + result + " bytes");
            //return result;
            if(!result.equals(null)) {
                mForeCastAdapter.clear();
                for (String s : result) {
                    mForeCastAdapter.add(s);
                }
            }
        }

    }//End of async task

    //Helper methods for JSON parsing
            /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
    private String getReadableDateString(long time){
        // Because the API returns a unix timestamp (measured in seconds),
        // it must be converted to milliseconds in order to be converted to valid date.
        SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
        return shortenedDateFormat.format(time);
    }

    /**
     * Prepare the weather high/lows for presentation.
     */
    private String formatHighLows(double high, double low) {
        // For presentation, assume the user doesn't care about tenths of a degree.
        long roundedHigh = Math.round(high);
        long roundedLow = Math.round(low);

        String highLowStr = roundedHigh + "/" + roundedLow;
        return highLowStr;
    }

    /**
     * Take the String representing the complete forecast in JSON Format and
     * pull out the data we need to construct the Strings needed for the wireframes.
     *
     * Fortunately parsing is easy:  constructor takes the JSON string and converts it
     * into an Object hierarchy for us.
     */
    private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
            throws JSONException {

        // These are the names of the JSON objects that need to be extracted.
        final String OWM_LIST = "list";
        final String OWM_WEATHER = "weather";
        final String OWM_TEMPERATURE = "temp";
        final String OWM_MAX = "max";
        final String OWM_MIN = "min";
        final String OWM_DESCRIPTION = "main";

        JSONObject forecastJson = new JSONObject(forecastJsonStr);
        JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

        // OWM returns daily forecasts based upon the local time of the city that is being
        // asked for, which means that we need to know the GMT offset to translate this data
        // properly.

        // Since this data is also sent in-order and the first day is always the
        // current day, we're going to take advantage of that to get a nice
        // normalized UTC date for all of our weather.
        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        String[] resultStrs = new String[numDays];
        for(int i = 0; i < weatherArray.length(); i++) {
            // For now, using the format "Day, description, hi/low"
            String day;
            String description;
            String highAndLow;

            // Get the JSON object representing the day
            JSONObject dayForecast = weatherArray.getJSONObject(i);

            // The date/time is returned as a long.  We need to convert that
            // into something human-readable, since most people won't read "1400356800" as
            // "this saturday".
            long dateTime;
            // Cheating to convert this to UTC time, which is what we want anyhow
            dateTime = dayTime.setJulianDay(julianStartDay+i);
            day = getReadableDateString(dateTime);

            // description is in a child array called "weather", which is 1 element long.
            JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
            description = weatherObject.getString(OWM_DESCRIPTION);

            // Temperatures are in a child object called "temp".  Try not to name variables
            // "temp" when working with temperature.  It confuses everybody.
            JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
            double high = temperatureObject.getDouble(OWM_MAX);
            double low = temperatureObject.getDouble(OWM_MIN);

            highAndLow = formatHighLows(high, low);
            resultStrs[i] = day + " - " + description + " - " + highAndLow;
        }

        for (String s : resultStrs) {
            Log.v("MainFragment", "Forecast entry: " + s);
        }
        return resultStrs;

    }

}

//http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e
//http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e&units=metric
