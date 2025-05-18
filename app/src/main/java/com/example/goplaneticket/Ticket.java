package com.example.goplaneticket;

import java.util.List;

public class Ticket {
    private String flightId;
    private List<String> seats;
    private long timestamp;
    private String documentId;


    private String from;
    private String to;
    private String departureTime;


    public Ticket() {
    }


    public String getFlightId() {
        return flightId;
    }

    public List<String> getSeats() {
        return seats;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getDepartureTime() {
        return departureTime;
    }


    public void setFlightId(String flightId) {
        this.flightId = flightId;
    }

    public void setSeats(List<String> seats) {
        this.seats = seats;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }
}
