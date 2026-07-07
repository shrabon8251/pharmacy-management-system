package com.example.pharmacy_management_system.services;

import com.example.pharmacy_management_system.models.Supplier;
import com.example.pharmacy_management_system.utility.ConnectionSingleton;
import com.example.pharmacy_management_system.utility.ValidationUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierService {

    private static final String INSERT = "INSERT INTO suppliers (supplier_id, name, phone, email, address) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE = "UPDATE suppliers SET name = ?, phone = ?, email = ?, address = ? WHERE supplier_id = ?";
    private static final String DELETE = "DELETE FROM suppliers WHERE supplier_id = ?";
    private static final String SELECT_BY_ID = "SELECT * FROM suppliers WHERE supplier_id = ?";
    private static final String SELECT_ALL = "SELECT * FROM suppliers ORDER BY name";
    private static final String SEARCH_BY_NAME = "SELECT * FROM suppliers WHERE name LIKE ? ORDER BY name";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM suppliers";

    public void saveSupplier(Supplier supplier) {
        validateSupplier(supplier);
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT)) {
            ps.setInt(1, supplier.getSupplierId());
            ps.setString(2, supplier.getName());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getEmail());
            ps.setString(5, supplier.getAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public void updateSupplier(Supplier supplier) {
        validateSupplier(supplier);
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getPhone());
            ps.setString(3, supplier.getEmail());
            ps.setString(4, supplier.getAddress());
            ps.setInt(5, supplier.getSupplierId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public void deleteSupplier(int supplierId) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(DELETE)) {
            ps.setInt(1, supplierId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
    }

    public Supplier findSupplierById(int supplierId) {
        List<Supplier> suppliers = executeQuery(SELECT_BY_ID, supplierId);
        return suppliers.isEmpty() ? null : suppliers.get(0);
    }

    public List<Supplier> loadSuppliers() {
        return executeQuery(SELECT_ALL);
    }

    public List<Supplier> searchByName(String keyword) {
        return executeQuery(SEARCH_BY_NAME, "%" + keyword + "%");
    }

    public int countAll() {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(COUNT_ALL);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return 0;
    }

    private void validateSupplier(Supplier supplier) {
        if (supplier == null) {
            throw new RuntimeException("Supplier data is required.");
        }
        if (supplier.getSupplierId() <= 0) {
            throw new RuntimeException("Supplier ID must be greater than 0.");
        }
        if (ValidationUtil.isBlank(supplier.getName())) {
            throw new RuntimeException("Supplier name is required.");
        }
        if (ValidationUtil.isBlank(supplier.getAddress())) {
            throw new RuntimeException("Supplier address is required.");
        }
        if (!ValidationUtil.isValidPhone(supplier.getPhone())) {
            throw new RuntimeException("Please enter a valid phone number (10-15 digits).");
        }
        if (!ValidationUtil.isValidEmail(supplier.getEmail())) {
            throw new RuntimeException("Please enter a valid email address.");
        }
    }

    private List<Supplier> executeQuery(String sql, Object... params) {
        List<Supplier> list = new ArrayList<>();
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

    private Supplier mapRow(ResultSet rs) throws SQLException {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(rs.getInt("supplier_id"));
        supplier.setName(rs.getString("name"));
        supplier.setPhone(rs.getString("phone"));
        supplier.setEmail(rs.getString("email"));
        supplier.setAddress(rs.getString("address"));
        return supplier;
    }
}
