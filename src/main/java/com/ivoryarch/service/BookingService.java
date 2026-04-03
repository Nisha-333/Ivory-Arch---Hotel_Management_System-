package com.ivoryarch.service;
import com.ivoryarch.dao.*;
import com.ivoryarch.model.*;
import java.util.List;

public class BookingService {
    private static final RoomDAO roomDAO = new RoomDAO();
    private static final BookingDAO bookingDAO = new BookingDAO();

    // WEEK 1: Method overloading
    public boolean bookRoom(int roomNumber) { return bookRoom(roomNumber, 1, 0); }
    public boolean bookRoom(int roomNumber, int days) { return bookRoom(roomNumber, days, 0); }

    // WEEK 4: synchronized method
    public synchronized boolean bookRoom(int roomNumber, int days, double discount) {
        Room room = roomDAO.getRoomByNumber(roomNumber);
        if (room == null || room.getStatus() != RoomStatus.AVAILABLE) return false;
        synchronized (room) {
            if (room.getStatus() != RoomStatus.AVAILABLE) return false;
            roomDAO.updateRoomStatus(roomNumber, RoomStatus.BOOKED);
            return true;
        }
    }

    private final Object lock = new Object();
    public void requestRoom() throws InterruptedException {
        synchronized (lock) { System.out.println("Waiting for room..."); lock.wait(); }
    }
    public void releaseRoom() { synchronized (lock) { lock.notify(); } }

    public int createBooking(Booking b) { return bookingDAO.createBooking(b); }
    public List<Booking> getAllBookings() { return bookingDAO.getAllBookings(); }
    public List<Booking> getBookingsByCustomer(int id) { return bookingDAO.getBookingsByCustomer(id); }

    public boolean cancelBooking(int bookingId, int roomNumber) {
        bookingDAO.updateBookingStatus(bookingId, "CANCELLED");
        return roomDAO.updateRoomStatus(roomNumber, RoomStatus.AVAILABLE);
    }

    public boolean checkout(int bookingId, int roomNumber) {
        bookingDAO.updateBookingStatus(bookingId, "CHECKED_OUT");
        roomDAO.updateRoomStatus(roomNumber, RoomStatus.CLEANING);
        new com.ivoryarch.thread.CleaningThread(roomNumber, roomDAO).start();
        return true;
    }
}
