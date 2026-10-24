package kiyoza.model;

public enum FreshnessStatus {
    FRESH("Fresh"),
    NEAR_EXPIRY("Near expiry"),
    SPOILED("Spoiled");

    private final String label;

    FreshnessStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
