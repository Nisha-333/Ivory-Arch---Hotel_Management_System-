package com.ivoryarch.service;
import com.ivoryarch.dao.RoomDAO;
import com.ivoryarch.model.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationService {
    private final RoomDAO roomDAO = new RoomDAO();

    public static class RecommendResult {
        public final List<Room> rooms;
        public final String message; // explains why fallback was used, or null if exact match
        public RecommendResult(List<Room> rooms, String message) {
            this.rooms = rooms; this.message = message;
        }
    }

    public RecommendResult recommend(double totalBudget, int guests, int nights,
                                     boolean wifiNeeded, boolean breakfastNeeded) {
        List<Room> available = roomDAO.getAvailableRooms();
        List<Room> matched = new ArrayList<>();
        List<Room> budgetOnly = new ArrayList<>(); // fits budget+capacity but not amenities
        List<Room> capacityOnly = new ArrayList<>(); // only fits capacity, over budget

        for (Room r : available) {
            double totalCost = r.getPricePerNight() * nights * 1.18; // incl. 18% GST
            boolean budgetOk   = totalCost <= totalBudget;
            boolean capacityOk = r.getCapacity() >= guests;
            boolean amenityOk  = (!wifiNeeded || r.isWifiAvailable())
                              && (!breakfastNeeded || r.isBreakfastIncluded());

            if (!capacityOk) continue; // never show rooms too small

            if (budgetOk && amenityOk) {
                matched.add(r);
            } else if (budgetOk) {
                budgetOnly.add(r); // within budget but missing an amenity
            } else if (amenityOk) {
                capacityOnly.add(r); // meets amenities but over budget
            }
        }

        matched.sort(Comparator.comparingDouble(Room::getPricePerNight));
        if (!matched.isEmpty()) {
            return new RecommendResult(matched, null);
        }

        // Fallback 1: fits budget but relaxed amenities
        budgetOnly.sort(Comparator.comparingDouble(Room::getPricePerNight));
        if (!budgetOnly.isEmpty()) {
            return new RecommendResult(budgetOnly,
                "No rooms exactly match your amenity preferences within budget.\n" +
                "Showing rooms within budget (some amenities may differ):");
        }

        // Fallback 2: meets amenities but over budget — show closest options
        capacityOnly.sort(Comparator.comparingDouble(Room::getPricePerNight));
        if (!capacityOnly.isEmpty()) {
            return new RecommendResult(capacityOnly.subList(0, Math.min(3, capacityOnly.size())),
                "No rooms available within your budget of Rs." + String.format("%,.0f", totalBudget) + ".\n" +
                "Showing the most affordable available options:");
        }

        // Nothing at all
        return new RecommendResult(Collections.emptyList(),
            "No available rooms found for " + guests + " guest(s). Try different dates.");
    }
}
