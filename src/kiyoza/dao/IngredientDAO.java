package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.Ingredient;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IngredientDAO {

    public List<Ingredient> findAll() throws SQLException {
        String sql = "SELECT ingredient_id, name, allergen_type, supplier FROM ingredients ORDER BY name";
        List<Ingredient> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public int add(Ingredient i) throws SQLException {
        String sql = "INSERT INTO ingredients (name, allergen_type, supplier) VALUES (?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, i.getName());
            ps.setString(2, i.getAllergenType());
            ps.setString(3, i.getSupplier());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : -1;
            }
        }
    }

    public void update(Ingredient i) throws SQLException {
        String sql = "UPDATE ingredients SET name = ?, allergen_type = ?, supplier = ? WHERE ingredient_id = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, i.getName());
            ps.setString(2, i.getAllergenType());
            ps.setString(3, i.getSupplier());
            ps.setInt(4, i.getId());
            ps.executeUpdate();
        }
    }

    /** Also removes the ingredient's links to dishes, its stock row and its freshness logs (ON DELETE CASCADE). */
    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM ingredients WHERE ingredient_id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    static Ingredient map(ResultSet rs) throws SQLException {
        return new Ingredient(rs.getInt("ingredient_id"), rs.getString("name"),
                rs.getString("allergen_type"), rs.getString("supplier"));
    }
}
