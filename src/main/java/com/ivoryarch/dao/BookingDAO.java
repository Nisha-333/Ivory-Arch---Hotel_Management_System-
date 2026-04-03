package com.ivoryarch.dao;
import com.ivoryarch.model.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

public class BookingDAO {
    public int createBooking(Booking b) {
        String sql = "INSERT INTO bookings (customer_id,room_id,room_number,customer_name,checkin_date,checkout_date,number_of_days,num_guests,status,total_amount,special_req) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,b.getCustomerId()); ps.setInt(2,b.getRoomId()); ps.setInt(3,b.getRoomNumber());
            ps.setString(4,b.getCustomerName()); ps.setString(5,b.getCheckInDate().toString());
            ps.setString(6,b.getCheckOutDate().toString()); ps.setInt(7,b.getNumberOfDays());
            ps.setInt(8,b.getNumberOfGuests()); ps.setString(9,b.getStatus());
            ps.setDouble(10,b.getTotalAmount()); ps.setString(11,b.getSpecialRequests());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { System.err.println("createBooking: " + e.getMessage()); }
        return -1;
    }

    public List<Booking> getAllBookings() {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bookings ORDER BY booking_id DESC")) {
            while (rs.next()) list.add(mapBooking(rs));
        } catch (SQLException e) { System.err.println("getAllBookings: " + e.getMessage()); }
        return list;
    }

    public List<Booking> getBookingsByCustomer(int customerId) {
        List<Booking> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bookings WHERE customer_id=? ORDER BY booking_id DESC")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBooking(rs));
        } catch (SQLException e) { System.err.println("getBookingsByCustomer: " + e.getMessage()); }
        return list;
    }

    public boolean updateBookingStatus(int bookingId, String status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bookings SET status=? WHERE booking_id=?")) {
            ps.setString(1, status); ps.setInt(2, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    private Booking mapBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setBookingId(rs.getInt("booking_id")); b.setCustomerId(rs.getInt("customer_id"));
        b.setRoomId(rs.getInt("room_id")); b.setRoomNumber(rs.getInt("room_number"));
        b.setCustomerName(rs.getString("customer_name"));
        b.setCheckInDate(LocalDate.parse(rs.getString("checkin_date")));
        b.setCheckOutDate(LocalDate.parse(rs.getString("checkout_date")));
        b.setNumberOfDays(rs.getInt("number_of_days")); b.setNumberOfGuests(rs.getInt("num_guests"));
        b.setStatus(rs.getString("status")); b.setTotalAmount(rs.getDouble("total_amount"));
        b.setSpecialRequests(rs.getString("special_req"));
        return b;
    }
}
