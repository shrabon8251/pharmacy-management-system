package com.example.pharmacy_management_system.services;

import com.example.pharmacy_management_system.models.Medicine;
import com.example.pharmacy_management_system.models.Sale;
import com.example.pharmacy_management_system.utility.ConnectionSingleton;
import com.example.pharmacy_management_system.utility.ValidationUtil;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SaleService {

    private static final String INSERT =
            "INSERT INTO sales (medicine_id, quantity, unit_price, total_price, sold_by) VALUES (?, ?, ?, ?, ?)";
    private static final String SELECT_ALL =
            "SELECT s.sale_id, s.medicine_id, m.name AS medicine_name, s.quantity, s.unit_price, s.total_price, s.sold_by, s.sale_date "
                    + "FROM sales s JOIN medicines m ON s.medicine_id = m.medicine_id "
                    + "ORDER BY s.sale_date DESC";
    private static final String SELECT_TODAY =
            "SELECT s.sale_id, s.medicine_id, m.name AS medicine_name, s.quantity, s.unit_price, s.total_price, s.sold_by, s.sale_date "
                    + "FROM sales s JOIN medicines m ON s.medicine_id = m.medicine_id "
                    + "WHERE DATE(s.sale_date) = CURDATE() ORDER BY s.sale_date DESC";
    private static final String TOTAL_SALES = "SELECT COALESCE(SUM(total_price), 0) FROM sales";
    private static final String TODAY_SALES_TOTAL = "SELECT COALESCE(SUM(total_price), 0) FROM sales WHERE DATE(sale_date) = CURDATE()";
    private static final String COUNT_ALL = "SELECT COUNT(*) FROM sales";
    private static final String COUNT_BY_MEDICINE = "SELECT COUNT(*) FROM sales WHERE medicine_id = ?";

    private final MedicineService medicineService = new MedicineService();

    public void recordSale(int medicineId, int quantity, String soldBy) {
        if (medicineId <= 0) {
            throw new RuntimeException("Medicine ID is required.");
        }
        if (quantity <= 0) {
            throw new RuntimeException("Sale quantity must be greater than 0.");
        }
        if (ValidationUtil.isBlank(soldBy)) {
            throw new RuntimeException("Sold-by username is required.");
        }

        Medicine medicine = medicineService.findMedicineById(medicineId);
        if (medicine == null) {
            throw new RuntimeException("Medicine not found.");
        }
        if (quantity > medicine.getQuantity()) {
            throw new RuntimeException("Not enough stock. Available: " + medicine.getQuantity());
        }

        BigDecimal unitPrice = medicine.getSellingPrice();
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid selling price for medicine: " + medicine.getName());
        }
        BigDecimal totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
        if (totalPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Total price must be greater than 0.");
        }

        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, medicineId);
            ps.setInt(2, quantity);
            ps.setBigDecimal(3, unitPrice);
            ps.setBigDecimal(4, totalPrice);
            ps.setString(5, soldBy);
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    generatedKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }

        medicineService.updateQuantity(medicineId, medicine.getQuantity() - quantity);
    }

    public List<Sale> loadSales() {
        return executeQuery(SELECT_ALL);
    }

    public List<Sale> loadTodaySales() {
        return executeQuery(SELECT_TODAY);
    }

    public BigDecimal getTotalSales() {
        return executeSum(TOTAL_SALES);
    }

    public BigDecimal getTodaySalesTotal() {
        return executeSum(TODAY_SALES_TOTAL);
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

    public boolean hasSalesForMedicine(int medicineId) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(COUNT_BY_MEDICINE)) {
            ps.setInt(1, medicineId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return false;
    }

    private List<Sale> executeQuery(String sql) {
        List<Sale> list = new ArrayList<>();
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return list;
    }

    private BigDecimal executeSum(String sql) {
        try (Connection connection = ConnectionSingleton.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(ValidationUtil.translateSqlException(e), e);
        }
        return BigDecimal.ZERO;
    }

    private Sale mapRow(ResultSet rs) throws SQLException {
        Sale sale = new Sale();
        sale.setSaleId(rs.getInt("sale_id"));
        sale.setMedicineId(rs.getInt("medicine_id"));
        sale.setMedicineName(rs.getString("medicine_name"));
        sale.setQuantity(rs.getInt("quantity"));
        sale.setUnitPrice(rs.getBigDecimal("unit_price"));
        sale.setTotalPrice(rs.getBigDecimal("total_price"));
        sale.setSoldBy(rs.getString("sold_by"));
        sale.setSaleDate(rs.getTimestamp("sale_date").toLocalDateTime());
        return sale;
    }
}
