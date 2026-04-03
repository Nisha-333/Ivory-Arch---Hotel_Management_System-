package com.ivoryarch.dao;
import com.ivoryarch.model.*;
import java.sql.*;
import java.util.*;

public class RoomDAO {
    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number,room_type,price_per_night,capacity,floor_number,status,wifi,breakfast,parking) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, room.getRoomNumber()); ps.setString(2, room.getRoomType().name());
            ps.setDouble(3, room.getPricePerNight()); ps.setInt(4, room.getCapacity());
            ps.setString(5, room.getFloorNumber()); ps.setString(6, room.getStatus().name());
            ps.setInt(7, room.isWifiAvailable()?1:0); ps.setInt(8, room.isBreakfastIncluded()?1:0);
            ps.setInt(9, room.isParkingAvailable()?1:0);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("addRoom: " + e.getMessage()); return false; }
    }

    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms ORDER BY room_number")) {
            while (rs.next()) list.add(mapRoom(rs));
        } catch (SQLException e) { System.err.println("getAllRooms: " + e.getMessage()); }
        return list;
    }

    public List<Room> getAvailableRooms() {
        List<Room> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM rooms WHERE status='AVAILABLE'")) {
            while (rs.next()) list.add(mapRoom(rs));
        } catch (SQLException e) { System.err.println("getAvailableRooms: " + e.getMessage()); }
        return list;
    }

    public List<Room> getRoomsByType(RoomType type) {
        List<Room> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE room_type=?")) {
            ps.setString(1, type.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRoom(rs));
        } catch (SQLException e) { System.err.println("getRoomsByType: " + e.getMessage()); }
        return list;
    }

    public Room getRoomByNumber(int roomNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM rooms WHERE room_number=?")) {
            ps.setInt(1, roomNumber);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRoom(rs);
        } catch (SQLException e) { System.err.println("getRoomByNumber: " + e.getMessage()); }
        return null;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_type=?,price_per_night=?,capacity=?,floor_number=?,status=?,wifi=?,breakfast=?,parking=? WHERE room_number=?";
        try (Connection conn = DatabaseConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, room.getRoomType().name()); ps.setDouble(2, room.getPricePerNight());
            ps.setInt(3, room.getCapacity()); ps.setString(4, room.getFloorNumber());
            ps.setString(5, room.getStatus().name()); ps.setInt(6, room.isWifiAvailable()?1:0);
            ps.setInt(7, room.isBreakfastIncluded()?1:0); ps.setInt(8, room.isParkingAvailable()?1:0);
            ps.setInt(9, room.getRoomNumber());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("updateRoom: " + e.getMessage()); return false; }
    }

    public boolean updateRoomStatus(int roomNumber, RoomStatus status) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE rooms SET status=? WHERE room_number=?")) {
            ps.setString(1, status.name()); ps.setInt(2, roomNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("updateRoomStatus: " + e.getMessage()); return false; }
    }

    public boolean deleteRoom(int roomNumber) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM rooms WHERE room_number=?")) {
            ps.setInt(1, roomNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { System.err.println("deleteRoom: " + e.getMessage()); return false; }
    }

    public Map<String, Long> getRoomTypeStats() {
        Map<String, Long> stats = new HashMap<>();
        List<Room> rooms = getAllRooms();
        Iterator<Room> it = rooms.iterator();
        while (it.hasNext()) {
            Room r = it.next();
            String type = r.getRoomType().getDisplayName();
            stats.put(type, stats.getOrDefault(type, 0L) + 1);
        }
        return stats;
    }

    public List<Room> getSortedRoomsByPrice() {
        List<Room> rooms = getAllRooms();
        Collections.sort(rooms);
        return rooms;
    }

    private Room mapRoom(ResultSet rs) throws SQLException {
        DeluxeRoom room = new DeluxeRoom();
        room.setRoomId(rs.getInt("room_id")); room.setRoomNumber(rs.getInt("room_number"));
        room.setRoomType(RoomType.valueOf(rs.getString("room_type")));
        room.setPricePerNight(rs.getDouble("price_per_night")); room.setCapacity(rs.getInt("capacity"));
        room.setFloorNumber(rs.getString("floor_number"));
        room.setStatus(RoomStatus.valueOf(rs.getString("status")));
        room.setWifiAvailable(rs.getInt("wifi")==1); room.setBreakfastIncluded(rs.getInt("breakfast")==1);
        room.setParkingAvailable(rs.getInt("parking")==1);
        return room;
    }
}
