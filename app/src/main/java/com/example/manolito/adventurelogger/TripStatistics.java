package com.example.manolito.adventurelogger;

import android.os.Bundle;
import java.text.NumberFormat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class TripStatistics extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(4);
        nf.setGroupingUsed(false);

        NumberFormat df = NumberFormat.getInstance();
        df.setMaximumFractionDigits(8);
        df.setGroupingUsed(false);

        setContentView(R.layout.activity_trip_statistics);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        double distance = getIntent().getDoubleExtra("distance",0.0);
        TextView distanceView = (TextView) findViewById(R.id.distance);
        distanceView.setText(nf.format(distance) + " Kilometers");

        double altitudeChange = getIntent().getDoubleExtra("altitude",0.0);
        TextView altitudeView = (TextView) findViewById(R.id.altitude);

        altitudeView.setText(nf.format(altitudeChange)+" Meters");

        String time = getIntent().getStringExtra("time");
        TextView timeView = (TextView) findViewById(R.id.time);
        timeView.setText(time);



        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }


}
