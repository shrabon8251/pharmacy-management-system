package com.example.pharmacy_management_system.services;

import com.example.pharmacy_management_system.models.Medicine;
import com.example.pharmacy_management_system.utility.ConnectionSingleton;
import com.example.pharmacy_management_system.utility.ValidationUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicineService {

    private static final String INSERT =
            "INSERT INTO medicines (medicine_id, name, category, manufacturer, description, buying_price, selling_price, quantity, expiry_date, image_path) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE medicines SET name = ?, category = ?, manufacturer = ?, description = ?, buying_price = ?, selling_price = ?, quantity = ?, expiry_date = ?, image_path = ? "
                    + "WHERE medicine_id = ?";
    private static final String DELETE = "DELETE FROM medicines WHERE medicine_id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM medicines WHERE medicine_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM medicines ORDER BY name";
    private static final String SEARCH_BY_NAME = "SELECT * FROM medicines WHERE name LIKE ? ORDER BY name";
    private static final String SELECT_LOW_STOCK = "SELECT * FROM medicines WHERE quantity < 20 ORDER BY quantity";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM medicines";
    private static final String COUNT_LOW_STOCK = "SELECT COUNT(*) FROM medicines WHERE quantity < 20";
    private static final String UPDATE_QUANTITY = "UPDATE medicines SET quantity = ? WHERE medicine_id = ?";

    public void saveMedicine(Medicine medicine) {
        validateMedicine(medicine);
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setInt(1, medicine.getMedicineId());
            ps.setString(2, medicine.getName());
            ps.setString(3, medicine.getCategory());
            ps.setString(4, medicine.getManufacturer());
            ps.setString(5, medicine.getDescription());
            ps.setBigDecimal(6, medicine.getBuyingPrice());
            ps.setBigDecimal(7, medicine.getSellingPrice());
            ps.setInt(8, medicine.getQuantity());
            ps.setDate(9, Date.valueOf(medicine.getExpiryDate()));
            ps.setString(10, medicine.getImagePath());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public void updateMedicine(Medicine medicine) {
        validateMedicine(medicine);
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, medicine.getName());
            ps.setString(2, medicine.getCategory());
            ps.setString(3, medicine.getManufacturer());
            ps.setString(4, medicine.getDescription());
            ps.setBigDecimal(5, medicine.getBuyingPrice());
            ps.setBigDecimal(6, medicine.getSellingPrice());
            ps.setInt(7, medicine.getQuantity());
            ps.setDate(8, Date.valueOf(medicine.getExpiryDate()));
            ps.setString(9, medicine.getImagePath());
            ps.setInt(10, medicine.getMedicineId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public void deleteMedicine(int medicineId) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, medicineId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public Medicine findMedicineById(int medicineId) {
        List<Medicine> medicines = executeQuery(SELECT_BY_ID, medicineId);
        return medicines.isEmpty() ? null : medicines.get(0);
    }

    public List<Medicine> loadMedicines() {
        return executeQuery(SELECT_ALL);
    }

    public List<Medicine> searchByName(String keyword) {
        return executeQuery(SEARCH_BY_NAME, "%" + keyword + "%");
    }

    public List<Medicine> findLowStock() {
        return executeQuery(SELECT_LOW_STOCK);
    }

    public int countAll() {
        return executeCount(COUNT_ALL);
    }

    public int countLowStock() {
        return executeCount(COUNT_LOW_STOCK);
    }

    public void updateQuantity(int medicineId, int newQuantity) {
        if (newQuantity < 0) {
            throw new RuntimeException("Stock quantity cannot be negative.");
        }
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE_QUANTITY)) {
            ps.setInt(1, newQuantity);
            ps.setInt(2, medicineId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    private void validateMedicine(Medicine medicine) {
        if (medicine == null) {
            throw new RuntimeException("Medicine data is required.");
        }
        if (medicine.getMedicineId() <= 0) {
            throw new RuntimeException("Medicine ID must be greater than 0.");
        }
        if (ValidationUtil.isBlank(medicine.getName())) {
            throw new RuntimeException("Medicine name is required.");
        }
        if (ValidationUtil.isBlank(medicine.getCategory())) {
            throw new RuntimeException("Category is required.");
        }
        if (ValidationUtil.isBlank(medicine.getManufacturer())) {
            throw new RuntimeException("Manufacturer / company is required.");
        }
        if (medicine.getExpiryDate() == null) {
            throw new RuntimeException("Expiry date is required.");
        }
        if (medicine.getBuyingPrice() == null || medicine.getBuyingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Buying price must be zero or positive.");
        }
        if (medicine.getSellingPrice() == null || medicine.getSellingPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Selling price must be zero or positive.");
        }
        if (medicine.getSellingPrice().compareTo(medicine.getBuyingPrice()) < 0) {
            throw new RuntimeException("Selling price must be greater than or equal to buying price.");
        }
        if (medicine.getQuantity() < 0) {
            throw new RuntimeException("Quantity cannot be negative.");
        }
    }

    private List<Medicine> executeQuery(String sql, Object... params) {
        List<Medicine> list = new ArrayList<>();
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

    private int executeCount(String sql) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return 0;
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineId(rs.getInt("medicine_id"));
        medicine.setName(rs.getString("name"));
        medicine.setCategory(rs.getString("category"));
        medicine.setManufacturer(rs.getString("manufacturer"));
        medicine.setDescription(rs.getString("description"));
        medicine.setBuyingPrice(rs.getBigDecimal("buying_price"));
        medicine.setSellingPrice(rs.getBigDecimal("selling_price"));
        medicine.setQuantity(rs.getInt("quantity"));
        medicine.setExpiryDate(rs.getDate("expiry_date").toLocalDate());
        medicine.setImagePath(rs.getString("image_path"));
        return medicine;
    }
}
