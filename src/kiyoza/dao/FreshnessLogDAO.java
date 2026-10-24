package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.FreshnessLog;
import kiyoza.model.FreshnessStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FreshnessLogDAO {
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String BASE =
            "SELECT l.log_id, l.ingredient_id, i.name AS ingredient_name, u.name AS checker, "
            + "DATE_FORMAT(l.check_date, '%Y-%m-%d %H:%i:%s') AS checked_text, l.status, l.remarks "
            + "FROM freshness_logs l "
            + "JOIN ingredients i ON i.ingredient_id = l.ingredient_id "
            + "LEFT JOIN users u ON u.user_id = l.checked_by ";

    public List<FreshnessLog> findAll() throws SQLException {
        List<FreshnessLog> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(BASE + "ORDER BY l.check_date DESC, l.log_id DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** The most recent check for every ingredient that has at least one, keyed by ingredient id. */
    public Map<Integer, FreshnessLog> latestByIngredient() throws SQLException {
        String sql = BASE + "WHERE l.log_id = (SELECT MAX(l2.log_id) FROM freshness_logs l2 "
                + "WHERE l2.ingredient_id = l.ingredient_id)";
        Map<Integer, FreshnessLog> result = new HashMap<>();
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                FreshnessLog log = map(rs);
                result.put(log.getIngredientId(), log);
            }
        }
        return result;
    }

    public void add(int ingredientId, int checkedByUserId, FreshnessStatus status, String remarks)
            throws SQLException {
        String sql = "INSERT INTO freshness_logs (ingredient_id, checked_by, status, remarks) VALUES (?, ?, ?, ?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ingredientId);
            ps.setInt(2, checkedByUserId);
            ps.setString(3, status.name());
            ps.setString(4, remarks);
            ps.executeUpdate();
        }
    }

    public void delete(int logId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM freshness_logs WHERE log_id = ?")) {
            ps.setInt(1, logId);
            ps.executeUpdate();
        }
    }

    private static FreshnessLog map(ResultSet rs) throws SQLException {
        String checked = rs.getString("checked_text");
        return new FreshnessLog(
                rs.getInt("log_id"),
                rs.getInt("ingredient_id"),
                rs.getString("ingredient_name"),
                rs.getString("checker"),
                checked == null ? null : LocalDateTime.parse(checked, TS),
                FreshnessStatus.valueOf(rs.getString("status")),
                rs.getString("remarks"));
    }
}
