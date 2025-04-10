package com.example.goplaneticket;

import java.util.List;

public class Ticket {
    private String flightId;
    private List<String> seats;
    private long timestamp;
    private String documentId;

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
}
