package kiyoza.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/** Stock record for one ingredient. */
public class Inventory {
    public static final double LOW_STOCK_LEVEL = 5.0;

    private final int ingredientId;
    private final String ingredientName;
    private double quantity;
    private String unit;
    private LocalDate expiryDate;
    private final LocalDateTime lastUpdated;

    public Inventory(int ingredientId, String ingredientName, double quantity, String unit,
                     LocalDate expiryDate, LocalDateTime lastUpdated) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
        this.lastUpdated = lastUpdated;
    }

    public int getIngredientId() { return ingredientId; }
    public String getIngredientName() { return ingredientName; }
    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public LocalDateTime getLastUpdated() { return lastUpdated; }

    public boolean isLowStock() {
        return quantity <= LOW_STOCK_LEVEL;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public long daysToExpiry() {
        return expiryDate == null ? Long.MAX_VALUE : ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    public String getStatus() {
        if (isExpired()) return "EXPIRED";
        if (isLowStock()) return "LOW STOCK";
        if (daysToExpiry() <= 3) return "EXPIRES SOON";
        return "OK";
    }
}
