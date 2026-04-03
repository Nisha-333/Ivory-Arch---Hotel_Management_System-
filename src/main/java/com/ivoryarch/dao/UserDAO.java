package com.ivoryarch.dao;
import com.ivoryarch.model.*;
import java.sql.*;
import java.util.*;

public class UserDAO {
    public boolean registerUser(User user) {
        String sql = "INSERT INTO users (name,email,password,role,phone,address,id_proof) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getName()); ps.setString(2, user.getEmail());
            ps.setString(3, user.getPassword()); ps.setString(4, user.getRole());
            ps.setString(5, user.getPhone());
            if (user instanceof Customer c) { ps.setString(6, c.getAddress()); ps.setString(7, c.getIdProof()); }
            else { ps.setNull(6, Types.VARCHAR); ps.setNull(7, Types.VARCHAR); }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("registerUser: " + e.getMessage()); return false; }
    }

    public User getUserByEmailAndPassword(String email, String hashedPassword) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM users WHERE email=? AND password=?")) {
            ps.setString(1, email); ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapUser(rs);
        } catch (SQLException e) { System.err.println("getUser: " + e.getMessage()); }
        return null;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE role='CUSTOMER'")) {
            while (rs.next()) {
                Customer c = new Customer();
                c.setUserId(rs.getInt("user_id")); c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email")); c.setPhone(rs.getString("phone"));
                c.setRole("CUSTOMER"); list.add(c);
            }
        } catch (SQLException e) { System.err.println("getAllCustomers: " + e.getMessage()); }
        return list;
    }

    public boolean updateUser(User user) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE users SET name=?,phone=? WHERE user_id=?")) {
            ps.setString(1, user.getName()); ps.setString(2, user.getPhone()); ps.setInt(3, user.getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("updateUser: " + e.getMessage()); return false; }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        String role = rs.getString("role");
        User user;
        if ("ADMIN".equals(role)) { user = new Admin(); }
        else { Customer c = new Customer(); c.setAddress(rs.getString("address")); user = c; }
        user.setUserId(rs.getInt("user_id")); user.setName(rs.getString("name"));
        user.setEmail(rs.getString("email")); user.setPassword(rs.getString("password"));
        user.setPhone(rs.getString("phone")); user.setRole(role);
        return user;
    }
}
