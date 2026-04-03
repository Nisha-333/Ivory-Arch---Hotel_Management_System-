package com.ivoryarch.dao;
import com.ivoryarch.model.Bill;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BillDAO {
    public int saveBill(Bill b) {
        String sql = "INSERT INTO bills (booking_id,customer_id,customer_name,room_number,num_days,room_charge,food_charge,laundry_charge,service_charge,extra_bed_charge,discount_amount,gst_amount,late_checkout,total_amount,payment_mode,payment_status,coupon_code,generated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,b.getBookingId()); ps.setInt(2,b.getCustomerId()); ps.setString(3,b.getCustomerName());
            ps.setInt(4,b.getRoomNumber()); ps.setInt(5,b.getNumberOfDays());
            ps.setDouble(6,b.getRoomCharge()!=null?b.getRoomCharge():0);
            ps.setDouble(7,b.getFoodCharge()!=null?b.getFoodCharge():0);
            ps.setDouble(8,b.getLaundryCharge()!=null?b.getLaundryCharge():0);
            ps.setDouble(9,b.getServiceCharge()!=null?b.getServiceCharge():0);
            ps.setDouble(10,b.getExtraBedCharge()!=null?b.getExtraBedCharge():0);
            ps.setDouble(11,b.getDiscountAmount()!=null?b.getDiscountAmount():0);
            ps.setDouble(12,b.getGstAmount()!=null?b.getGstAmount():0);
            ps.setDouble(13,b.getLateCheckoutFee());
            ps.setDouble(14,b.getTotalAmount()!=null?b.getTotalAmount():0);
            ps.setString(15,b.getPaymentMode()); ps.setString(16,b.getPaymentStatus());
            ps.setString(17,b.getCouponCode()); ps.setString(18,LocalDateTime.now().toString());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) return keys.getInt(1);
        } catch (SQLException e) { System.err.println("saveBill: " + e.getMessage()); }
        return -1;
    }

    public List<Bill> getAllBills() {
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM bills ORDER BY bill_id DESC")) {
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) { System.err.println("getAllBills: " + e.getMessage()); }
        return list;
    }

    public Bill getBillByBookingId(int bookingId) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bills WHERE booking_id=?")) {
            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapBill(rs);
        } catch (SQLException e) { System.err.println("getBillByBookingId: " + e.getMessage()); }
        return null;
    }

    public boolean updatePaymentStatus(int billId, String status, String mode) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE bills SET payment_status=?,payment_mode=? WHERE bill_id=?")) {
            ps.setString(1,status); ps.setString(2,mode); ps.setInt(3,billId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    public List<Bill> getBillsByCustomer(int customerId) {
        List<Bill> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM bills WHERE customer_id=? ORDER BY bill_id DESC")) {
            ps.setInt(1, customerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapBill(rs));
        } catch (SQLException e) { System.err.println("getBillsByCustomer: " + e.getMessage()); }
        return list;
    }

    public double getTotalRevenue() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT SUM(total_amount) FROM bills WHERE payment_status='PAID'")) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { System.err.println("getTotalRevenue: " + e.getMessage()); }
        return 0;
    }

    // Returns map of short month name -> total revenue for that month (paid bills only)
    public java.util.Map<String, Double> getMonthlyRevenue() {
        java.util.Map<String, Double> map = new java.util.LinkedHashMap<>();
        String[] monthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        String sql = "SELECT strftime('%m', generated_at) as mon, SUM(total_amount) as rev " +
                     "FROM bills WHERE payment_status='PAID' GROUP BY mon ORDER BY mon";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int m = Integer.parseInt(rs.getString("mon")) - 1;
                map.put(monthNames[m], rs.getDouble("rev"));
            }
        } catch (SQLException e) { System.err.println("getMonthlyRevenue: " + e.getMessage()); }
        return map;
    }

    private Bill mapBill(ResultSet rs) throws SQLException {
        Bill b = new Bill();
        b.setBillId(rs.getInt("bill_id")); b.setBookingId(rs.getInt("booking_id"));
        b.setCustomerId(rs.getInt("customer_id")); b.setCustomerName(rs.getString("customer_name"));
        b.setRoomNumber(rs.getInt("room_number")); b.setNumberOfDays(rs.getInt("num_days"));
        b.setRoomCharge(rs.getDouble("room_charge")); b.setFoodCharge(rs.getDouble("food_charge"));
        b.setLaundryCharge(rs.getDouble("laundry_charge")); b.setServiceCharge(rs.getDouble("service_charge"));
        b.setExtraBedCharge(rs.getDouble("extra_bed_charge")); b.setDiscountAmount(rs.getDouble("discount_amount"));
        b.setGstAmount(rs.getDouble("gst_amount")); b.setLateCheckoutFee(rs.getDouble("late_checkout"));
        b.setTotalAmount(rs.getDouble("total_amount")); b.setPaymentMode(rs.getString("payment_mode"));
        b.setPaymentStatus(rs.getString("payment_status")); b.setCouponCode(rs.getString("coupon_code"));
        return b;
    }
}
