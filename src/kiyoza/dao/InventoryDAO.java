package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.Inventory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class InventoryDAO {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Dates are read as text (DATE_FORMAT) so JDBC time-zone settings can never shift them.
    private static final String SELECT =
            "SELECT inv.ingredient_id, i.name AS ingredient_name, inv.quantity, inv.unit, "
            + "DATE_FORMAT(inv.expiry_date, '%Y-%m-%d') AS expiry_text, "
            + "DATE_FORMAT(inv.last_updated, '%Y-%m-%d %H:%i:%s') AS updated_text "
            + "FROM inventory inv JOIN ingredients i ON i.ingredient_id = inv.ingredient_id "
            + "ORDER BY i.name";

    public List<Inventory> findAll() throws SQLException {
        List<Inventory> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(SELECT)) {
            while (rs.next()) {
                String expiry = rs.getString("expiry_text");
                String updated = rs.getString("updated_text");
                list.add(new Inventory(
                        rs.getInt("ingredient_id"),
                        rs.getString("ingredient_name"),
                        rs.getDouble("quantity"),
                        rs.getString("unit"),
                        expiry == null ? null : LocalDate.parse(expiry),
                        updated == null ? null : LocalDateTime.parse(updated, TS)));
            }
        }
        return list;
    }

    /** Insert a stock row, or update it if the ingredient already has one. */
    public void save(Inventory inv) throws SQLException {
        String sql = "INSERT INTO inventory (ingredient_id, quantity, unit, expiry_date) VALUES (?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE quantity = VALUES(quantity), unit = VALUES(unit), "
                + "expiry_date = VALUES(expiry_date), last_updated = CURRENT_TIMESTAMP";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, inv.getIngredientId());
            ps.setDouble(2, inv.getQuantity());
            ps.setString(3, inv.getUnit());
            ps.setString(4, inv.getExpiryDate() == null ? null : inv.getExpiryDate().toString());
            ps.executeUpdate();
        }
    }

    public void delete(int ingredientId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM inventory WHERE ingredient_id = ?")) {
            ps.setInt(1, ingredientId);
            ps.executeUpdate();
        }
    }
}
