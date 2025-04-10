package com.example.goplaneticket;

import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SearchFlightActivity extends AppCompatActivity {

    EditText editFrom, editTo, editDeparture, editReturn, editClass, editAdults, editChildren;
    Button btnSearchFlights;
    EditText activeDateField;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_flight);

        editFrom = findViewById(R.id.editFrom);
        editTo = findViewById(R.id.editTo);
        editDeparture = findViewById(R.id.editDeparture);
        editReturn = findViewById(R.id.editReturn);
        editClass = findViewById(R.id.editClass);
        editAdults = findViewById(R.id.editAdults);
        editChildren = findViewById(R.id.editChildren);
        btnSearchFlights = findViewById(R.id.btnSearchFlights);

        sdf.setTimeZone(TimeZone.getDefault());

        editDeparture.setOnClickListener(v -> {
            activeDateField = editDeparture;
            showDepartureDatePicker();
        });

        editReturn.setOnClickListener(v -> {
            activeDateField = editReturn;
            showReturnDatePicker();
        });

        btnSearchFlights.setOnClickListener(view -> {
            String from = editFrom.getText().toString().trim();
            String to = editTo.getText().toString().trim();
            String departure = editDeparture.getText().toString().trim();
            String ret = editReturn.getText().toString().trim();
            String flightClass = editClass.getText().toString().trim();
            String adults = editAdults.getText().toString().trim();
            String children = editChildren.getText().toString().trim();

            if (from.isEmpty() || to.isEmpty() || departure.isEmpty() || flightClass.isEmpty()) {
                Toast.makeText(this, "Tölts ki minden mezőt!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (from.equalsIgnoreCase(to)) {
                Toast.makeText(this, "Az indulás és érkezés nem lehet ugyanaz!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!ret.isEmpty() && !departure.isEmpty()) {
                try {
                    Date departureDate = sdf.parse(departure);
                    Date returnDate = sdf.parse(ret);

                    if (returnDate.before(departureDate)) {
                        Toast.makeText(this, "A visszaút dátuma nem lehet az indulás előtt!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (adults.isEmpty()) adults = "1";
            if (children.isEmpty()) children = "0";

            Intent intent = new Intent(this, FlightResultsActivity.class);
            intent.putExtra("from", from);
            intent.putExtra("to", to);
            intent.putExtra("departure", departure);
            intent.putExtra("return", ret);
            intent.putExtra("flightClass", flightClass);
            intent.putExtra("adults", adults);
            intent.putExtra("children", children);
            startActivity(intent);
        });
    }

    private void showDepartureDatePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(today));

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Válassz indulási dátumot")
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.show(getSupportFragmentManager(), "DEPARTURE_DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String formattedDate = sdf.format(new Date(selection));
            editDeparture.setText(formattedDate);

            if (!editReturn.getText().toString().isEmpty()) {
                try {
                    Date departureDate = sdf.parse(formattedDate);
                    Date returnDate = sdf.parse(editReturn.getText().toString());

                    if (returnDate.before(departureDate)) {
                        editReturn.setText("");
                        Toast.makeText(this, "A visszaút dátuma törölve, mert az indulás előtt volt.", Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showReturnDatePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();

        CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder()
                .setValidator(DateValidatorPointForward.from(today));

        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Válassz visszaút dátumot")
                .setSelection(today)
                .setCalendarConstraints(constraintsBuilder.build())
                .build();

        datePicker.show(getSupportFragmentManager(), "RETURN_DATE_PICKER");

        datePicker.addOnPositiveButtonClickListener(selection -> {
            String formattedDate = sdf.format(new Date(selection));
            editReturn.setText(formattedDate);

            if (!editDeparture.getText().toString().isEmpty()) {
                try {
                    Date departureDate = sdf.parse(editDeparture.getText().toString());
                    Date returnDate = sdf.parse(formattedDate);

                    if (returnDate.before(departureDate)) {
                        Toast.makeText(this, "A visszaút nem lehet az indulás előtt!", Toast.LENGTH_SHORT).show();
                        editReturn.setText("");
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
