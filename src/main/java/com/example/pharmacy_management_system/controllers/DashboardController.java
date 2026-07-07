package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.models.Medicine;
import com.example.pharmacy_management_system.services.MedicineService;
import com.example.pharmacy_management_system.services.SaleService;
import com.example.pharmacy_management_system.services.SupplierService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label totalMedicinesLabel;

    @FXML
    private Label lowStockLabel;

    @FXML
    private Label totalSuppliersLabel;

    @FXML
    private Label totalSalesLabel;

    @FXML
    private Label todaySalesLabel;

    @FXML
    private TableView<Medicine> lowStockTable;

    @FXML
    private TableColumn<Medicine, Integer> medicineIdColumn;

    @FXML
    private TableColumn<Medicine, String> medicineNameColumn;

    @FXML
    private TableColumn<Medicine, String> categoryColumn;

    @FXML
    private TableColumn<Medicine, Integer> quantityColumn;

    @FXML
    private TableColumn<Medicine, String> expiryDateColumn;

    private final MedicineService medicineService = new MedicineService();
    private final SupplierService supplierService = new SupplierService();
    private final SaleService saleService = new SaleService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadDashboardStats();
    }

    private void setupTableColumns() {
        medicineIdColumn.setCellValueFactory(new PropertyValueFactory<>("medicineId"));
        medicineNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        expiryDateColumn.setCellValueFactory(new PropertyValueFactory<>("expiryDate"));
    }

    private void loadDashboardStats() {
        try {
            totalMedicinesLabel.setText(String.valueOf(medicineService.countAll()));
            lowStockLabel.setText(String.valueOf(medicineService.countLowStock()));
            totalSuppliersLabel.setText(String.valueOf(supplierService.countAll()));
            totalSalesLabel.setText(formatCurrency(saleService.getTotalSales()));
            todaySalesLabel.setText(formatCurrency(saleService.getTodaySalesTotal()));

            ObservableList<Medicine> lowStockList = FXCollections.observableArrayList(medicineService.findLowStock());
            lowStockTable.setItems(lowStockList);
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private String formatCurrency(BigDecimal value) {
        return String.format("%.2f", value);
    }

    @FXML
    private void dashboard(ActionEvent event) {
    }

    @FXML
    private void medicine(ActionEvent event) {
        navigate("medicine");
    }

    @FXML
    private void supplier(ActionEvent event) {
        navigate("supplier");
    }

    @FXML
    private void sales(ActionEvent event) {
        navigate("sales");
    }

    @FXML
    private void salesHistory(ActionEvent event) {
        navigate("salesHistory");
    }

    @FXML
    private void signOut(ActionEvent event) {
        navigate("login");
    }

    private void navigate(String fxml) {
        try {
            PharmacyManagement.sceneChange(fxml);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
