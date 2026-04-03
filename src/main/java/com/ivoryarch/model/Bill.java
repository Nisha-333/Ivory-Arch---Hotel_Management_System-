package com.ivoryarch.model;
import java.io.Serializable;

public class Bill implements Serializable {
    private int billId;
    private int bookingId;
    private int customerId;
    private String customerName;
    private int roomNumber;
    private int numberOfDays;

    // WEEK 2: Wrapper class fields (explicit autoboxing/unboxing)
    private Double roomCharge;
    private Double foodCharge;
    private Double laundryCharge;
    private Double serviceCharge;
    private Double extraBedCharge;
    private Double discountAmount;
    private Double gstAmount;
    private Double totalAmount;

    private double lateCheckoutFee;
    private String paymentMode;
    private String paymentStatus;
    private String couponCode;

    public Bill() {}
    public Bill(int bookingId, int customerId, int roomNumber, int days, double pricePerNight) {
        this.bookingId = bookingId; this.customerId = customerId;
        this.roomNumber = roomNumber; this.numberOfDays = days;
        this.roomCharge = pricePerNight * days; // autoboxing
        this.foodCharge = 0.0; this.laundryCharge = 0.0;
        this.serviceCharge = 0.0; this.extraBedCharge = 0.0;
        this.discountAmount = 0.0; this.lateCheckoutFee = 0.0;
        this.paymentStatus = "PENDING";
    }

    public void calculateTotal() {
        double room    = roomCharge    != null ? (double) roomCharge    : 0; // unboxing
        double food    = foodCharge    != null ? (double) foodCharge    : 0;
        double laundry = laundryCharge != null ? (double) laundryCharge : 0;
        double service = serviceCharge != null ? (double) serviceCharge : 0;
        double extra   = extraBedCharge!= null ? (double) extraBedCharge: 0;
        double discount= discountAmount!= null ? (double) discountAmount: 0;
        double subtotal = room + food + laundry + service + extra + lateCheckoutFee - discount;
        Double gst = subtotal * 0.18; // autoboxing
        this.gstAmount = gst;
        this.totalAmount = subtotal + gst;
    }

    public int getBillId() { return billId; }
    public void setBillId(int billId) { this.billId = billId; }
    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }
    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public int getRoomNumber() { return roomNumber; }
    public void setRoomNumber(int roomNumber) { this.roomNumber = roomNumber; }
    public int getNumberOfDays() { return numberOfDays; }
    public void setNumberOfDays(int numberOfDays) { this.numberOfDays = numberOfDays; }
    public Double getRoomCharge() { return roomCharge; }
    public void setRoomCharge(Double roomCharge) { this.roomCharge = roomCharge; }
    public Double getFoodCharge() { return foodCharge; }
    public void setFoodCharge(Double foodCharge) { this.foodCharge = foodCharge; }
    public Double getLaundryCharge() { return laundryCharge; }
    public void setLaundryCharge(Double laundryCharge) { this.laundryCharge = laundryCharge; }
    public Double getServiceCharge() { return serviceCharge; }
    public void setServiceCharge(Double serviceCharge) { this.serviceCharge = serviceCharge; }
    public Double getExtraBedCharge() { return extraBedCharge; }
    public void setExtraBedCharge(Double extraBedCharge) { this.extraBedCharge = extraBedCharge; }
    public Double getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(Double discountAmount) { this.discountAmount = discountAmount; }
    public Double getGstAmount() { return gstAmount; }
    public void setGstAmount(Double gstAmount) { this.gstAmount = gstAmount; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public double getLateCheckoutFee() { return lateCheckoutFee; }
    public void setLateCheckoutFee(double lateCheckoutFee) { this.lateCheckoutFee = lateCheckoutFee; }
    public String getPaymentMode() { return paymentMode; }
    public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
