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
import java.io.File;

public class PastTripsFragment extends Fragment {
    private ArrayAdapter<String> pastTripsItemAdapter;
    public static final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AdventureLogger";

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public File[] GetFiles(String DirectoryPath) {
        File f = new File(DirectoryPath);
        f.mkdirs();
        File[] file = f.listFiles();
        return file;
    }

    public ArrayList<String> getFileNames(File[] file){
        ArrayList<String> arrayFiles = new ArrayList<String>();
        if (file.length == 0)
            return null;
        else {
            for (int i=0; i<file.length; i++)
                arrayFiles.add(file[i].getName());
        }

        return arrayFiles;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        File[] tripFiles = GetFiles(path);
        ArrayList<String> fileNames = getFileNames(tripFiles);


        View rootView = inflater.inflate(R.layout.fragment_past_trips, container,false);

        List<String> tripsItems = new ArrayList<String>();

        for (int i = 0; i <fileNames.size() ; i++) {
            tripsItems.add(fileNames.get(i));
        }

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
