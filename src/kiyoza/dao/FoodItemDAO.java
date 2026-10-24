package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.FoodItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FoodItemDAO {
    public static final int DEFAULT_RESTAURANT_ID = 1;

    /** Loads dishes together with their ingredients. */
    public List<FoodItem> findAll(boolean onlyAvailable) throws SQLException {
        String sql = "SELECT food_id, restaurant_id, name, category, price, prep_details, is_available "
                + "FROM food_items" + (onlyAvailable ? " WHERE is_available = TRUE" : "")
                + " ORDER BY category, name";
        String links = "SELECT fi.food_id, i.ingredient_id, i.name, i.allergen_type, i.supplier "
                + "FROM food_ingredients fi JOIN ingredients i ON i.ingredient_id = fi.ingredient_id "
                + "ORDER BY i.name";
        Map<Integer, FoodItem> byId = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection()) {
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    FoodItem f = new FoodItem();
                    f.setId(rs.getInt("food_id"));
                    f.setRestaurantId(rs.getInt("restaurant_id"));
                    f.setName(rs.getString("name"));
                    f.setCategory(rs.getString("category"));
                    f.setPrice(rs.getBigDecimal("price"));
                    f.setPrepDetails(rs.getString("prep_details"));
                    f.setAvailable(rs.getBoolean("is_available"));
                    byId.put(f.getId(), f);
                }
            }
            try (Statement st = c.createStatement(); ResultSet rs = st.executeQuery(links)) {
                while (rs.next()) {
                    FoodItem f = byId.get(rs.getInt("food_id"));
                    if (f != null) f.addIngredient(IngredientDAO.map(rs));
                }
            }
        }
        return new ArrayList<>(byId.values());
    }

    public int add(FoodItem f, List<Integer> ingredientIds) throws SQLException {
        String sql = "INSERT INTO food_items (restaurant_id, name, category, price, prep_details, is_available) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, DEFAULT_RESTAURANT_ID);
                ps.setString(2, f.getName());
                ps.setString(3, f.getCategory());
                ps.setBigDecimal(4, f.getPrice());
                ps.setString(5, f.getPrepDetails());
                ps.setBoolean(6, f.isAvailable());
                ps.executeUpdate();
                int id;
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    keys.next();
                    id = keys.getInt(1);
                }
                replaceLinks(c, id, ingredientIds);
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void update(FoodItem f, List<Integer> ingredientIds) throws SQLException {
        String sql = "UPDATE food_items SET name = ?, category = ?, price = ?, prep_details = ?, is_available = ? "
                + "WHERE food_id = ?";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, f.getName());
                ps.setString(2, f.getCategory());
                ps.setBigDecimal(3, f.getPrice());
                ps.setString(4, f.getPrepDetails());
                ps.setBoolean(5, f.isAvailable());
                ps.setInt(6, f.getId());
                ps.executeUpdate();
                replaceLinks(c, f.getId(), ingredientIds);
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public void delete(int foodId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM food_items WHERE food_id = ?")) {
            ps.setInt(1, foodId);
            ps.executeUpdate();
        }
    }

    private void replaceLinks(Connection c, int foodId, List<Integer> ingredientIds) throws SQLException {
        try (PreparedStatement del = c.prepareStatement("DELETE FROM food_ingredients WHERE food_id = ?")) {
            del.setInt(1, foodId);
            del.executeUpdate();
        }
        try (PreparedStatement ins = c.prepareStatement(
                "INSERT INTO food_ingredients (food_id, ingredient_id) VALUES (?, ?)")) {
            for (int ingredientId : ingredientIds) {
                ins.setInt(1, foodId);
                ins.setInt(2, ingredientId);
                ins.addBatch();
            }
            ins.executeBatch();
        }
    }
}
