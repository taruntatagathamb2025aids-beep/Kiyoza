package kiyoza.model;

public class Ingredient implements Describable {
    private int id;
    private String name;
    private String allergenType; // null when the ingredient has no major allergen
    private String supplier;

    public Ingredient() { }

    public Ingredient(int id, String name, String allergenType, String supplier) {
        this.id = id;
        this.name = name;
        setAllergenType(allergenType);
        this.supplier = supplier;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAllergenType() { return allergenType; }
    public void setAllergenType(String allergenType) {
        this.allergenType = (allergenType == null || allergenType.isBlank()) ? null : allergenType.trim();
    }

    public String getSupplier() { return supplier; }
    public void setSupplier(String supplier) { this.supplier = supplier; }

    public boolean hasAllergen() {
        return allergenType != null;
    }

    @Override
    public String getDescription() {
        return hasAllergen() ? name + " (contains " + allergenType + ")" : name;
    }

    /** Used by Swing lists and combo boxes. */
    @Override
    public String toString() {
        return name;
    }
}
