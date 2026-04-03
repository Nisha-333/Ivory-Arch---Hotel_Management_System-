package com.ivoryarch.model;

public enum RoomStatus {
    AVAILABLE("Available"),
    BOOKED("Booked"),
    CLEANING("Cleaning"),
    MAINTENANCE("Maintenance");

    private final String label;
    RoomStatus(String label) { this.label = label; }
    public String getLabel() { return label; }
    @Override public String toString() { return label; }
}
