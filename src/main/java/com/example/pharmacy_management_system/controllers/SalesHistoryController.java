package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.models.Sale;
import com.example.pharmacy_management_system.services.SaleService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Sales history controller following SRP: displays all sales with seller information.
 */
public class SalesHistoryController implements Initializable {

    @FXML private TableView<Sale> salesTable;
    @FXML private TableColumn<Sale, Integer> saleIdColumn;
    @FXML private TableColumn<Sale, String> medicineNameColumn;
    @FXML private TableColumn<Sale, Integer> quantityColumn;
    @FXML private TableColumn<Sale, String> unitPriceColumn;
    @FXML private TableColumn<Sale, String> totalPriceColumn;
    @FXML private TableColumn<Sale, String> soldByColumn;
    @FXML private TableColumn<Sale, String> saleDateColumn;
    @FXML private Label totalSalesLabel;

    private final SaleService saleService = new SaleService();
    private ObservableList<Sale> salesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTableColumns();
        loadSalesHistory();
    }

    private void setupTableColumns() {
        saleIdColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getSaleId()).asObject());
        medicineNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMedicineName()));
        quantityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());
        unitPriceColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUnitPrice().toString()));
        totalPriceColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTotalPrice().toString()));
        soldByColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSoldBy()));
        saleDateColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getSaleDate().toString()));
        salesTable.setItems(salesList);
    }

    private void loadSalesHistory() {
        try {
            salesList.setAll(saleService.loadSales());
            totalSalesLabel.setText(String.valueOf(saleService.getTotalSales()));
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    @FXML private void dashboard(ActionEvent event) { navigate("dashboard"); }
    @FXML private void medicine(ActionEvent event) { navigate("medicine"); }
    @FXML private void supplier(ActionEvent event) { navigate("supplier"); }
    @FXML private void sales(ActionEvent event) { navigate("sales"); }
    @FXML private void salesHistory(ActionEvent event) { }
    @FXML private void signOut(ActionEvent event) { navigate("login"); }

    private void navigate(String fxml) {
        try {
            PharmacyManagement.sceneChange(fxml);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
