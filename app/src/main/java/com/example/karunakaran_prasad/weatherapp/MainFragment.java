package com.example.karunakaran_prasad.weatherapp;

import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.view.View;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.Menu;
import android.view.MenuInflater;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;




/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public static final String URL_STRING = "http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e&units=metric";

    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);
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


        ArrayAdapter<String> mForeCastAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.list_item_forecast,
                R.id.list_item_forecast_textview,
                dummy_weather_data);

        ListView forecast_list_view = (ListView) root_view.findViewById (R.id.listview_forecast);
        forecast_list_view.setAdapter(mForeCastAdapter);

        new FetchWeatherTask().execute(URL_STRING);


        return root_view;
    }

    private class FetchWeatherTask extends AsyncTask<String, Integer, Long> {

        @Override
        protected Long doInBackground(String... args){

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            String url_weather = args[0];
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
            } catch (IOException e) {
                Log.e("MainFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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

        return 0L;//for success
        }//End of doInBackground

        protected void onPostExecute(Long result) {
            Log.d("MainFragment", "Done executing in the background");
            //Toast.makeText(this, "Downloaded " + result + " bytes");
            //return result;
        }

    }//End of async task

}

//http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e
//http://api.openweathermap.org/data/2.5/forecast/daily?id=5375480&cnt=7&APPID=e3226c55f0b50d304e1a7e408117354e&units=metric
