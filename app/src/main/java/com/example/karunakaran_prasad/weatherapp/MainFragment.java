package com.example.karunakaran_prasad.weatherapp;

import android.view.View;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.karunakaran_prasad.weatherapp.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class MainFragment extends Fragment {


    public MainFragment() {
        // Required empty public constructor
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

        return root_view;
    }

}
