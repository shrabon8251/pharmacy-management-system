package com.example.pharmacy_management_system.services;

import com.example.pharmacy_management_system.models.User;
import com.example.pharmacy_management_system.utility.ConnectionSingleton;
import com.example.pharmacy_management_system.utility.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserService {

    private static final String SELECT_BY_USERNAME =
            "SELECT id, full_name, username, password, email, phone, security_question, security_answer FROM users WHERE username = ?";
    private static final String SELECT_BY_EMAIL =
            "SELECT id, full_name, username, password, email, phone, security_question, security_answer FROM users WHERE email = ?";
    private static final String SELECT_BY_PHONE =
            "SELECT id, full_name, username, password, email, phone, security_question, security_answer FROM users WHERE phone = ?";
    private static final String INSERT =
            "INSERT INTO users (full_name, username, password, email, phone, security_question, security_answer) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PASSWORD = "UPDATE users SET password = ? WHERE username = ?";

    public User findByUsername(String username) {
        List<User> users = executeQuery(SELECT_BY_USERNAME, username);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByEmail(String email) {
        List<User> users = executeQuery(SELECT_BY_EMAIL, email);
        return users.isEmpty() ? null : users.get(0);
    }

    public User findByPhone(String phone) {
        List<User> users = executeQuery(SELECT_BY_PHONE, phone);
        return users.isEmpty() ? null : users.get(0);
    }

    public void saveUser(User user) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setString(1, user.getFullName());
            ps.setString(2, user.getUsername());
            ps.setString(3, user.getPassword());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getSecurityQuestion());
            ps.setString(7, user.getSecurityAnswer());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public void updatePassword(String username, String newPassword) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_PASSWORD)) {
            ps.setString(1, newPassword);
            ps.setString(2, username);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public User authenticate(String username, String password) {
        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            throw new RuntimeException("Username and password are required.");
        }
        User user = findByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid username or password.");
        }
        return user;
    }

    public void register(User user) {
        if (ValidationUtil.isBlank(user.getFullName())
                || ValidationUtil.isBlank(user.getUsername())
                || ValidationUtil.isBlank(user.getPassword())
                || ValidationUtil.isBlank(user.getEmail())
                || ValidationUtil.isBlank(user.getPhone())
                || ValidationUtil.isBlank(user.getSecurityQuestion())
                || ValidationUtil.isBlank(user.getSecurityAnswer())) {
            throw new RuntimeException("All fields are required.");
        }
        if (user.getPassword().length() < 4) {
            throw new RuntimeException("Password must be at least 4 characters.");
        }
        if (!ValidationUtil.isValidEmail(user.getEmail())) {
            throw new RuntimeException("Please enter a valid email address.");
        }
        if (!ValidationUtil.isValidPhone(user.getPhone())) {
            throw new RuntimeException("Please enter a valid phone number (10-15 digits).");
        }
        if (findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Username already exists.");
        }
        if (findByEmail(user.getEmail()) != null) {
            throw new RuntimeException("Email already exists.");
        }
        if (findByPhone(user.getPhone()) != null) {
            throw new RuntimeException("Phone number already exists.");
        }
        saveUser(user);
    }

    public void resetPassword(String username, String securityQuestion, String securityAnswer, String newPassword) {
        if (ValidationUtil.isBlank(username)
                || ValidationUtil.isBlank(securityQuestion)
                || ValidationUtil.isBlank(securityAnswer)
                || ValidationUtil.isBlank(newPassword)) {
            throw new RuntimeException("All fields are required.");
        }
        if (newPassword.length() < 4) {
            throw new RuntimeException("Password must be at least 4 characters.");
        }
        User user = findByUsername(username);
        if (user == null) {
            throw new RuntimeException("User not found.");
        }
        if (!user.getSecurityQuestion().equals(securityQuestion)) {
            throw new RuntimeException("Security question does not match.");
        }
        if (!user.getSecurityAnswer().equalsIgnoreCase(securityAnswer)) {
            throw new RuntimeException("Security answer does not match.");
        }
        updatePassword(username, newPassword);
    }

    private List<User> executeQuery(String sql, Object... params) {
        List<User> list = new ArrayList<>();
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return list;
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setFullName(rs.getString("full_name"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setPhone(rs.getString("phone"));
        user.setSecurityQuestion(rs.getString("security_question"));
        user.setSecurityAnswer(rs.getString("security_answer"));
        return user;
    }
}
