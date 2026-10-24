package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.Restaurant;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RestaurantDAO {

    /** The app serves a single restaurant: the first row of the table. */
    public Restaurant getDefault() throws SQLException {
        String sql = "SELECT restaurant_id, name, address, phone FROM restaurants ORDER BY restaurant_id LIMIT 1";
        try (Connection c = DBConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new Restaurant(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
        return new Restaurant(FoodItemDAO.DEFAULT_RESTAURANT_ID, "Kiyoza", "", "");
    }
}
