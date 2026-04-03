package com.ivoryarch.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:data/ivoryarch.db";
    private static Connection connection;

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
        }
        return connection;
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS users (
                    user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL, email TEXT UNIQUE NOT NULL,
                    password TEXT NOT NULL, role TEXT NOT NULL,
                    phone TEXT, address TEXT, id_proof TEXT)""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS rooms (
                    room_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    room_number INTEGER UNIQUE NOT NULL, room_type TEXT NOT NULL,
                    price_per_night REAL NOT NULL, capacity INTEGER DEFAULT 2,
                    floor_number TEXT, status TEXT DEFAULT 'AVAILABLE',
                    wifi INTEGER DEFAULT 0, breakfast INTEGER DEFAULT 0, parking INTEGER DEFAULT 0)""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bookings (
                    booking_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    customer_id INTEGER, room_id INTEGER, room_number INTEGER,
                    customer_name TEXT, checkin_date TEXT, checkout_date TEXT,
                    number_of_days INTEGER, num_guests INTEGER,
                    status TEXT DEFAULT 'CONFIRMED', total_amount REAL, special_req TEXT)""");
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS bills (
                    bill_id INTEGER PRIMARY KEY AUTOINCREMENT,
                    booking_id INTEGER, customer_id INTEGER, customer_name TEXT,
                    room_number INTEGER, num_days INTEGER, room_charge REAL,
                    food_charge REAL, laundry_charge REAL, service_charge REAL,
                    extra_bed_charge REAL, discount_amount REAL, gst_amount REAL,
                    late_checkout REAL, total_amount REAL, payment_mode TEXT,
                    payment_status TEXT DEFAULT 'PENDING', coupon_code TEXT, generated_at TEXT)""");

            String correctHash = "240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9";
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users WHERE role='ADMIN'");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO users (name,email,password,role,phone) VALUES " +
                    "('Admin','admin@ivoryarch.com','" + correctHash + "','ADMIN','9999999999')");
            } else {
                // Fix wrong hash if DB already exists
                stmt.execute("UPDATE users SET password='" + correctHash + "' WHERE role='ADMIN'");
            }
            rs = stmt.executeQuery("SELECT COUNT(*) FROM rooms");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO rooms (room_number,room_type,price_per_night,capacity,floor_number,status,wifi,breakfast) VALUES " +
                    "(101,'STANDARD',2500,2,'1','AVAILABLE',1,0)," +
                    "(102,'STANDARD',2500,2,'1','AVAILABLE',1,0)," +
                    "(201,'DELUXE',4500,2,'2','AVAILABLE',1,1)," +
                    "(202,'DELUXE',4500,2,'2','AVAILABLE',1,1)," +
                    "(301,'SUITE',8000,4,'3','AVAILABLE',1,1)," +
                    "(401,'EXECUTIVE',12000,4,'4','AVAILABLE',1,1)," +
                    "(501,'PRESIDENTIAL',25000,6,'5','AVAILABLE',1,1)");
            }
            System.out.println("[DB] Ivory Arch database initialized.");
        } catch (SQLException e) { System.err.println("[DB] Error: " + e.getMessage()); }
    }

    public static void closeConnection() {
        try { if (connection != null && !connection.isClosed()) connection.close(); }
        catch (SQLException e) { System.err.println("[DB] Close error: " + e.getMessage()); }
    }
}
