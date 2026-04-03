package com.ivoryarch.model;

public class DeluxeRoom extends Room {
    public DeluxeRoom() { super(); }
    public DeluxeRoom(int roomNumber, RoomType type, double price) {
        super(roomNumber, type, price);
    }

    @Override
    public double calculateTariff(int days) {
        double base = getPricePerNight() * days;
        Double tariff = base; // autoboxing
        if (days >= 7)  tariff = tariff * 0.90;
        if (days >= 30) tariff = tariff * 0.80;
        return tariff; // unboxing
    }
}
