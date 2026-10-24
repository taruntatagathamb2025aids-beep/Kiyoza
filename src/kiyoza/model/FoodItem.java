package kiyoza.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class FoodItem implements Describable {
    private int id;
    private int restaurantId;
    private String name;
    private String category;
    private BigDecimal price;
    private String prepDetails;
    private boolean available = true;
    private final List<Ingredient> ingredients = new ArrayList<>();

    public FoodItem() { }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRestaurantId() { return restaurantId; }
    public void setRestaurantId(int restaurantId) { this.restaurantId = restaurantId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getPrepDetails() { return prepDetails; }
    public void setPrepDetails(String prepDetails) { this.prepDetails = prepDetails; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public List<Ingredient> getIngredients() {
        return Collections.unmodifiableList(ingredients);
    }

    public void addIngredient(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public void setIngredients(Collection<Ingredient> list) {
        ingredients.clear();
        ingredients.addAll(list);
    }

    public boolean containsAllergen(String allergen) {
        if (allergen == null) return false;
        for (Ingredient i : ingredients) {
            if (i.hasAllergen() && i.getAllergenType().equalsIgnoreCase(allergen)) return true;
        }
        return false;
    }

    public Set<String> getAllergens() {
        Set<String> result = new TreeSet<>();
        for (Ingredient i : ingredients) {
            if (i.hasAllergen()) result.add(i.getAllergenType());
        }
        return result;
    }

    @Override
    public String getDescription() {
        return name + " - " + category;
    }

    @Override
    public String toString() {
        return name;
    }
}
