package com.ivoryarch.model;
import java.io.Serializable;
//condtructor overloading , abstract
public abstract class Room implements Serializable, Comparable<Room>, Amenities {
    private int roomId;
    private int roomNumber;
    private RoomType roomType;
    private double pricePerNight;
    private int capacity;
    private String floorNumber;
    private RoomStatus status;
    private boolean wifiAvailable;
    private boolean breakfastIncluded;
    private boolean parkingAvailable;

    public Room() { this.status = RoomStatus.AVAILABLE; }
    public Room(int roomNumber, RoomType type, double price) {
        this.roomNumber = roomNumber; this.roomType = type;
        this.pricePerNight = price; this.status = RoomStatus.AVAILABLE;
        this.capacity = type.getDefaultCapacity();
    }

    public abstract double calculateTariff(int days);

    public double calculateBillWithTax(int days) {
        double tariff = calculateTariff(days);
        return tariff + (tariff * 0.18);
    }

    @Override public int compareTo(Room other) {
        return Double.compare(this.pricePerNight, other.pricePerNight);
    }

    @Override public boolean provideWifi() { return wifiAvailable; }
    @Override public boolean provideBreakfast() { return breakfastIncluded; }
    @Override public boolean provideParking() { return parkingAvailable; }
    @Override public String getAmenitiesSummary() {
        return (wifiAvailable ? "WiFi " : "") + (breakfastIncluded ? "Breakfast " : "") + (parkingAvailable ? "Parking" : "");
    }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public RoomType getRoomType() { return roomType; }
    public void setRoomType(RoomType roomType) { this.roomType = roomType; }
    public double getPricePerNight() { return pricePerNight; }
    public void setPricePerNight(double pricePerNight) { this.pricePerNight = pricePerNight; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public String getFloorNumber() { return floorNumber; }
    public void setFloorNumber(String floorNumber) { this.floorNumber = floorNumber; }
    public RoomStatus getStatus() { return status; }
    public void setStatus(RoomStatus status) { this.status = status; }
    public boolean isWifiAvailable() { return wifiAvailable; }
    public void setWifiAvailable(boolean wifiAvailable) { this.wifiAvailable = wifiAvailable; }
    public boolean isBreakfastIncluded() { return breakfastIncluded; }
    public void setBreakfastIncluded(boolean breakfastIncluded) { this.breakfastIncluded = breakfastIncluded; }
    public boolean isParkingAvailable() { return parkingAvailable; }
    public void setParkingAvailable(boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }
}
