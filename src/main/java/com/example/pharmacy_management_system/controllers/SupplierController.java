package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.models.Supplier;
import com.example.pharmacy_management_system.services.SupplierService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SupplierController implements Initializable {

    @FXML
    private TextField supplierIdField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField addressField;
    @FXML
    private TextField searchField;

    @FXML
    private TableView<Supplier> supplierTable;
    @FXML
    private TableColumn<Supplier, Integer> idColumn;
    @FXML
    private TableColumn<Supplier, String> nameColumn;
    @FXML
    private TableColumn<Supplier, String> phoneColumn;
    @FXML
    private TableColumn<Supplier, String> emailColumn;
    @FXML
    private TableColumn<Supplier, String> addressColumn;

    private final SupplierService supplierService = new SupplierService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadSuppliers();
        supplierTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                populateFields(newSelection);
            }
        });
    }

    private void setupTableColumns() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("supplierId"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
    }

    private void loadSuppliers() {
        try {
            ObservableList<Supplier> suppliers = FXCollections.observableArrayList(supplierService.loadSuppliers());
            supplierTable.setItems(suppliers);
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void populateFields(Supplier supplier) {
        supplierIdField.setText(String.valueOf(supplier.getSupplierId()));
        nameField.setText(supplier.getName());
        phoneField.setText(supplier.getPhone());
        emailField.setText(supplier.getEmail());
        addressField.setText(supplier.getAddress());
    }

    @FXML
    private void addSupplier(ActionEvent event) {
        try {
            Supplier supplier = buildSupplierFromFields();
            supplierService.saveSupplier(supplier);
            AlertUtil.showInfo("Success", "Supplier added successfully.");
            clearFields();
            loadSuppliers();
        } catch (RuntimeException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void updateSupplier(ActionEvent event) {
        try {
            Supplier supplier = buildSupplierFromFields();
            supplierService.updateSupplier(supplier);
            AlertUtil.showInfo("Success", "Supplier updated successfully.");
            clearFields();
            loadSuppliers();
        } catch (RuntimeException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void deleteSupplier(ActionEvent event) {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtil.showError("Error", "Please select a supplier to delete.");
            return;
        }
        if (AlertUtil.showConfirmation("Confirm Delete", "Are you sure you want to delete this supplier?")) {
            try {
                supplierService.deleteSupplier(selected.getSupplierId());
                AlertUtil.showInfo("Success", "Supplier deleted successfully.");
                clearFields();
                loadSuppliers();
            } catch (RuntimeException e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    @FXML
    private void clearFields(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        supplierIdField.clear();
        nameField.clear();
        phoneField.clear();
        emailField.clear();
        addressField.clear();
        searchField.clear();
    }

    @FXML
    private void searchSupplier(ActionEvent event) {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadSuppliers();
            return;
        }
        try {
            ObservableList<Supplier> suppliers = FXCollections.observableArrayList(supplierService.searchByName(keyword));
            supplierTable.setItems(suppliers);
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private Supplier buildSupplierFromFields() {
        Supplier supplier = new Supplier();
        supplier.setSupplierId(Integer.parseInt(supplierIdField.getText().trim()));
        supplier.setName(nameField.getText().trim());
        supplier.setPhone(phoneField.getText().trim());
        supplier.setEmail(emailField.getText().trim());
        supplier.setAddress(addressField.getText().trim());
        return supplier;
    }

    @FXML
    private void dashboard(ActionEvent event) {
        navigate("dashboard");
    }

    @FXML
    private void medicine(ActionEvent event) {
        navigate("medicine");
    }

    @FXML
    private void supplier(ActionEvent event) {
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
