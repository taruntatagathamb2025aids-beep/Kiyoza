package kiyoza.dao;

import kiyoza.db.DBConnection;
import kiyoza.model.Admin;
import kiyoza.model.Customer;
import kiyoza.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    /** Returns an Admin or Customer when the credentials match, otherwise null. */
    public User authenticate(String email, String password) throws SQLException {
        String sql = "SELECT user_id, name, email, role FROM users WHERE email = ? AND password_hash = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim());
            ps.setString(2, hash(password));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("user_id");
                    String name = rs.getString("name");
                    String mail = rs.getString("email");
                    return "ADMIN".equals(rs.getString("role"))
                            ? new Admin(id, name, mail)
                            : new Customer(id, name, mail);
                }
            }
        }
        return null;
    }

    /** SHA-256 as lowercase hex, the same format as MySQL's SHA2(x, 256). */
    public static String hash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
