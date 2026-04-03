package com.ivoryarch.model;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Booking implements Serializable {
    private int bookingId;
    private int customerId;
    private int roomId;
    private int roomNumber;
    private String customerName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int numberOfDays;
    private int numberOfGuests;
    private String status;
    private double totalAmount;
    private String specialRequests;

    public Booking() {}
    public Booking(int customerId, int roomId, LocalDate checkIn, LocalDate checkOut, int guests) {
        this.customerId = customerId; this.roomId = roomId;
        this.checkInDate = checkIn; this.checkOutDate = checkOut;
        this.numberOfGuests = guests;
        this.numberOfDays = (int) ChronoUnit.DAYS.between(checkIn, checkOut);
        this.status = "CONFIRMED";
    }

    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) { this.numberOfDays = numberOfDays; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public void setNumberOfGuests(int numberOfGuests) { this.numberOfGuests = numberOfGuests; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
}
