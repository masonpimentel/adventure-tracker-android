package com.example.manolito.adventurelogger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class PastTripsFragment extends Fragment {
    private ArrayAdapter<String> pastTripsItemAdapter;
    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdventureLogger";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        String tripItem1= "Whistler";
        String tripItem2= "Cypress";
        String tripItem3= "Trip3 (Date)";
        String tripItem4= "Trip4 (Date)";
        String tripItem5= "Trip5 (Date)";
        String tripItem6= "Trip6 (Date)";
        String tripItem7= "Trip7 (Date)";
        String tripItem8= "Trip8 (Date)";
        String tripItem9= "Trip9 (Date)";
        String tripItem10= "Trip10 (Date)";

        View rootView = inflater.inflate(R.layout.fragment_past_trips, container,false);

        rootView.setBackgroundResource(R.drawable.cypress);


        List<String> tripsItems = new ArrayList<String>();

        tripsItems.add(tripItem1);
        tripsItems.add(tripItem2);
        tripsItems.add(tripItem3);
        tripsItems.add(tripItem4);
        tripsItems.add(tripItem5);
        tripsItems.add(tripItem6);
        tripsItems.add(tripItem7);
        tripsItems.add(tripItem8);
        tripsItems.add(tripItem9);
        tripsItems.add(tripItem10);

        pastTripsItemAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.list_item_trips,
                R.id.list_item_trips_textview,
                tripsItems);
        ListView listView = (ListView) rootView.findViewById(R.id.listview_pastTrips);

        listView.setAdapter(pastTripsItemAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(getActivity(), MapsActivity.class);
                intent.putExtra("map",i);
                intent.putExtra("path",path);
                startActivity(intent);
            }
        });


        return rootView;

    }
}
