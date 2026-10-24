package kiyoza.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Restaurant {
    private final int id;
    private final String name;
    private final String address;
    private final String phone;
    private final List<FoodItem> menu = new ArrayList<>();

    public Restaurant(int id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPhone() { return phone; }

    public void addItem(FoodItem item) {
        menu.add(item);
    }

    public List<FoodItem> getMenu() {
        return Collections.unmodifiableList(menu);
    }

    /** Menu items that do not contain the given allergen (null means no filter). */
    public List<FoodItem> getItemsWithoutAllergen(String allergen) {
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem item : menu) {
            if (allergen == null || !item.containsAllergen(allergen)) result.add(item);
        }
        return result;
    }
}
