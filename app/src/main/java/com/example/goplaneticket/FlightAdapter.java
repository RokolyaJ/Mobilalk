package com.example.goplaneticket;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class FlightAdapter extends RecyclerView.Adapter<FlightAdapter.FlightViewHolder> {

    private Context context;
    private ArrayList<Flight> flightList;
    private int totalPeople = 1;

    public FlightAdapter(Context context, ArrayList<Flight> flightList) {
        this.context = context;
        this.flightList = flightList;
    }

    public void setTotalPeople(int totalPeople) {
        this.totalPeople = totalPeople;
    }

    @NonNull
    @Override
    public FlightViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.flight_item, parent, false);
        return new FlightViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FlightViewHolder holder, int position) {
        Flight flight = flightList.get(position);
        holder.airline.setText(flight.getAirline());
        holder.time.setText(flight.getDepartureTime() + " - " + flight.getArrivalTime());
        holder.route.setText(flight.getFrom() + " ➡ " + flight.getTo());
        holder.price.setText(flight.getPrice() + " Ft");
        holder.flightClass.setText("Osztály: " + flight.getFlightClass());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, SeatSelectionActivity.class);
            intent.putExtra("flight", flight);
            intent.putExtra("maxSeats", totalPeople);
            intent.putExtra("flightId", flight.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return flightList.size();
    }

    static class FlightViewHolder extends RecyclerView.ViewHolder {
        TextView airline, time, route, price, flightClass;

        public FlightViewHolder(@NonNull View itemView) {
            super(itemView);
            airline = itemView.findViewById(R.id.tvAirline);
            time = itemView.findViewById(R.id.tvTime);
            route = itemView.findViewById(R.id.tvRoute);
            price = itemView.findViewById(R.id.tvPrice);
            flightClass = itemView.findViewById(R.id.tvClass);
        }
    }
}
