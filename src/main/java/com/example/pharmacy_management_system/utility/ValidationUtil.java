package com.example.pharmacy_management_system.utility;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

public class ValidationUtil {

    private ValidationUtil() {
    }

    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isValidEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }

    public static boolean isValidPhone(String phone) {
        if (isBlank(phone)) {
            return false;
        }
        String phoneRegex = "^[0-9]{10,15}$";
        return phone.matches(phoneRegex);
    }

    public static String translateSqlException(SQLException e) {
        String message = e.getMessage();
        if (message == null) {
            message = "";
        }

        if (e instanceof SQLIntegrityConstraintViolationException) {
            if (message.contains("Duplicate entry")) {
                if (message.contains("username")) {
                    return "Username already exists. Please choose a different username.";
                }
                if (message.contains("email")) {
                    return "Email already exists. Please use a different email.";
                }
                if (message.contains("phone")) {
                    return "Phone number already exists. Please use a different phone number.";
                }
                return "This record already exists. Please use unique values.";
            }
            if (message.contains("foreign key constraint fails") || message.contains("Cannot delete")) {
                return "Cannot delete or update this record because it is used by other records.";
            }
            return "Data validation failed. Please check your input.";
        }

        if (message.contains("Communications link failure")) {
            return "Could not connect to database. Please make sure MySQL is running.";
        }

        if (message.contains("Access denied")) {
            return "Database access denied. Please check your username and password.";
        }

        if (message.contains("Unknown database")) {
            return "Database not found. Please create the database first.";
        }

        return "An unexpected error occurred. Please try again.";
    }
}
