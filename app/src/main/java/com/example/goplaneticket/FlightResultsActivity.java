package com.example.goplaneticket;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Random;

public class FlightResultsActivity extends AppCompatActivity {

    private static final String TAG = "FlightResultsActivity";
    private RecyclerView recyclerView;
    private FlightAdapter flightAdapter;
    private ArrayList<Flight> flightList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        overridePendingTransition(R.anim.fade_in, R.anim.fade_in);

        setContentView(R.layout.activity_flight_results);

        ImageView backButton = findViewById(R.id.backButtonFlightResults);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        TextView toolbarTitle = findViewById(R.id.toolbarTitleFlightResults);
        if (toolbarTitle != null) {
            toolbarTitle.setText("Találati lista");
        }

        String from = getIntent().getStringExtra("from");
        String to = getIntent().getStringExtra("to");
        String departure = getIntent().getStringExtra("departure");
        String returnDate = getIntent().getStringExtra("return");
        String flightClass = getIntent().getStringExtra("flightClass");
        String adults = getIntent().getStringExtra("adults");
        String children = getIntent().getStringExtra("children");

        Log.d(TAG, "Received data: from=" + from + ", to=" + to + ", departure=" + departure + ", return=" + returnDate);
        TextView tvSearchParams = findViewById(R.id.tvSearchParams);
        if (tvSearchParams != null) {
            String searchInfo = "From: " + from + "\n" +
                    "To: " + to + "\n" +
                    "Departure: " + departure + "\n" +
                    "Return: " + (returnDate != null && !returnDate.isEmpty() ? returnDate : "N/A") + "\n" +
                    "Class: " + flightClass + "\n" +
                    "Adults: " + adults + "\n" +
                    "Children: " + children;
            tvSearchParams.setText(searchInfo);
        }

        int totalPeople = Integer.parseInt(adults) + Integer.parseInt(children);
        Log.d(TAG, "Összes utas: " + totalPeople);

        recyclerView = findViewById(R.id.recyclerViewFlights);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        flightList = generateSampleFlights(from, to, flightClass);

        flightAdapter = new FlightAdapter(this, flightList);
        flightAdapter.setTotalPeople(totalPeople);
        recyclerView.setAdapter(flightAdapter);
    }

    private ArrayList<Flight> generateSampleFlights(String from, String to, String flightClass) {
        ArrayList<Flight> flights = new ArrayList<>();
        String[] airlines = {"Wizz Air", "Ryanair", "Lufthansa", "KLM", "Emirates"};
        Random random = new Random();

        for (int i = 0; i < 5; i++) {
            String airline = airlines[random.nextInt(airlines.length)];

            int departureHour = 6 + random.nextInt(12);
            int departureMinute = random.nextInt(60);
            String departureTime = String.format("%02d:%02d", departureHour, departureMinute);

            int flightDuration = 1 + random.nextInt(4);
            int arrivalHour = (departureHour + flightDuration) % 24;
            String arrivalTime = String.format("%02d:%02d", arrivalHour, departureMinute);

            double basePrice = 20000 + random.nextInt(80000);
            if (flightClass.equalsIgnoreCase("Business")) basePrice *= 2.5;
            if (flightClass.equalsIgnoreCase("First")) basePrice *= 4;

            Flight flight = new Flight(from, to, flightClass, airline, departureTime, arrivalTime, basePrice);
            flights.add(flight);
        }

        return flights;
    }
}
