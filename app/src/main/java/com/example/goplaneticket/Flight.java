package com.example.goplaneticket;

import java.io.Serializable;
import java.util.UUID;

public class Flight implements Serializable {
    private String id;
    private String from;
    private String to;
    private String flightClass;
    private String airline;
    private String departureTime;
    private String arrivalTime;
    private double price;

    public Flight() {

    }

    public Flight(String from, String to, String flightClass, String airline,
                  String departureTime, String arrivalTime, double price) {
        this.id = UUID.randomUUID().toString();
        this.from = from;
        this.to = to;
        this.flightClass = flightClass;
        this.airline = airline;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
    }


    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getFlightClass() {
        return flightClass;
    }

    public String getAirline() {
        return airline;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public double getPrice() {
        return price;
    }


    public void setId(String id) {
        this.id = id;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setFlightClass(String flightClass) {
        this.flightClass = flightClass;
    }

    public void setAirline(String airline) {
        this.airline = airline;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
