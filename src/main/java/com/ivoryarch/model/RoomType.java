package com.ivoryarch.model;

public enum RoomType {
    STANDARD("Standard Room", 2500.0, 2, "Comfortable room with all essentials"),
    DELUXE("Deluxe Room", 4500.0, 2, "Premium room with city view"),
    SUITE("Suite", 8000.0, 4, "Spacious suite with living area"),
    EXECUTIVE("Executive Suite", 12000.0, 4, "Business class with lounge access"),
    PRESIDENTIAL("Presidential Suite", 25000.0, 6, "Top-floor luxury suite");

    private final String displayName;
    private final double basePrice;
    private final int defaultCapacity;
    private final String description;

    RoomType(String displayName, double basePrice, int defaultCapacity, String description) {
        this.displayName = displayName; this.basePrice = basePrice;
        this.defaultCapacity = defaultCapacity; this.description = description;
    }

    public String getDisplayName() { return displayName; }
    public double getBasePrice() { return basePrice; }
    public int getDefaultCapacity() { return defaultCapacity; }
    public String getDescription() { return description; }
    public double getWeekendPrice() { return basePrice * 1.20; }
    public double getSeasonalPrice() { return basePrice * 1.35; }
    @Override public String toString() { return displayName; }
}
