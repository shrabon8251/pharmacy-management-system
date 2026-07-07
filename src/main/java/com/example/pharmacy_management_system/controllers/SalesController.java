package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.controllers.MedicineCardController.MedicineCardListener;
import com.example.pharmacy_management_system.models.Medicine;
import com.example.pharmacy_management_system.services.MedicineService;
import com.example.pharmacy_management_system.services.SaleService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Sales Controller following SRP: manages the sales menu, cart, and checkout.
 */
public class SalesController implements Initializable, MedicineCardListener {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilterBox;
    @FXML private FlowPane medicineGrid;
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> cartMedicineColumn;
    @FXML private TableColumn<CartItem, Integer> cartQuantityColumn;
    @FXML private TableColumn<CartItem, BigDecimal> cartPriceColumn;
    @FXML private TableColumn<CartItem, BigDecimal> cartTotalColumn;
    @FXML private Label grandTotalLabel;
    @FXML private TextField payAmountField;
    @FXML private Label changeLabel;

    private final MedicineService medicineService = new MedicineService();
    private final SaleService saleService = new SaleService();
    private ObservableList<Medicine> allMedicines = FXCollections.observableArrayList();
    private ObservableList<Medicine> displayedMedicines = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private String currentSearchText = "";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupCartTable();
        loadCategories();
        loadMedicines();
        setupLiveSearch();
    }

    private void setupLiveSearch() {
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            performLiveSearch(newValue);
        });
    }

    private void performLiveSearch(String keyword) {
        currentSearchText = keyword == null ? "" : keyword.trim().toLowerCase();
        applyFilters();
    }

    private void setupCartTable() {
        cartMedicineColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getMedicine().getName()));
        cartQuantityColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());
        cartPriceColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getMedicine().getSellingPrice()));
        cartTotalColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getTotal()));
        cartTable.setItems(cartItems);
    }

    private void loadCategories() {
        try {
            ObservableList<String> categories = FXCollections.observableArrayList("All");
            for (Medicine m : medicineService.loadMedicines()) {
                if (!categories.contains(m.getCategory())) {
                    categories.add(m.getCategory());
                }
            }
            categories.sort(Comparator.naturalOrder());
            categoryFilterBox.setItems(categories);
            categoryFilterBox.getSelectionModel().selectFirst();
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void loadMedicines() {
        try {
            allMedicines.setAll(medicineService.loadMedicines());
            applyFilters();
        } catch (RuntimeException e) {
            AlertUtil.showError("Database Error", e.getMessage());
        }
    }

    private void renderMedicineGrid() {
        medicineGrid.getChildren().clear();
        for (Medicine medicine : displayedMedicines) {
            medicineGrid.getChildren().add(createMedicineCard(medicine));
        }
    }

    private void applyFilters() {
        String selectedCategory = categoryFilterBox.getValue();
        String category = selectedCategory == null ? "All" : selectedCategory;

        displayedMedicines.setAll(
                allMedicines.stream()
                        .filter(m -> {
                            boolean matchesSearch = currentSearchText.isEmpty()
                                    || m.getName().toLowerCase().contains(currentSearchText)
                                    || String.valueOf(m.getMedicineId()).contains(currentSearchText)
                                    || m.getManufacturer().toLowerCase().contains(currentSearchText);
                            boolean matchesCategory = category.equals("All") || category.equals(m.getCategory());
                            return matchesSearch && matchesCategory;
                        })
                        .collect(Collectors.toList())
        );
        renderMedicineGrid();
    }

    private AnchorPane createMedicineCard(Medicine medicine) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pharmacy_management_system/medicineCard.fxml"));
            AnchorPane card = loader.load();
            MedicineCardController controller = loader.getController();
            controller.setData(medicine, this);
            return card;
        } catch (IOException e) {
            AlertUtil.showError("Error", "Could not load medicine card: " + e.getMessage());
            e.printStackTrace();
            return new AnchorPane();
        }
    }

    @Override
    public void onAddToCart(Medicine medicine, int quantity) {
        if (quantity <= 0 || quantity > medicine.getQuantity()) {
            AlertUtil.showError("Error", "Invalid quantity. Available stock: " + medicine.getQuantity());
            return;
        }
        for (CartItem item : cartItems) {
            if (item.getMedicine().getMedicineId() == medicine.getMedicineId()) {
                int newQty = item.getQuantity() + quantity;
                if (newQty > medicine.getQuantity()) {
                    AlertUtil.showError("Error", "Not enough stock. Available: " + medicine.getQuantity());
                    return;
                }
                item.setQuantity(newQty);
                cartTable.refresh();
                updateTotals();
                return;
            }
        }
        cartItems.add(new CartItem(medicine, quantity));
        updateTotals();
    }

    @Override
    public void onViewDetails(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Medicine Details");
        alert.setHeaderText("ID: " + medicine.getMedicineId() + " - " + medicine.getName());
        alert.setContentText("Category: " + medicine.getCategory()
                + "\nManufacturer: " + medicine.getManufacturer()
                + "\nBuying Price: ৳" + medicine.getBuyingPrice()
                + "\nSelling Price: ৳" + medicine.getSellingPrice()
                + "\nStock: " + medicine.getQuantity()
                + "\nExpiry: " + medicine.getExpiryDate());
        alert.showAndWait();
    }

    @FXML
    private void removeFromCart() {
        CartItem selected = cartTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            cartItems.remove(selected);
            updateTotals();
        }
    }

    @FXML
    private void clearCart() {
        cartItems.clear();
        updateTotals();
    }

    @FXML
    private void checkout() {
        if (cartItems.isEmpty()) {
            AlertUtil.showError("Error", "Cart is empty.");
            return;
        }

        BigDecimal payAmount = parseAmount(payAmountField.getText());
        if (payAmount == null) {
            AlertUtil.showError("Error", "Please enter a valid payment amount.");
            return;
        }

        BigDecimal total = getGrandTotal();
        if (payAmount.compareTo(total) < 0) {
            AlertUtil.showError("Error", "Payment amount is less than total.");
            return;
        }

        try {
            String soldBy = PharmacyManagement.currentUser != null ? PharmacyManagement.currentUser : "unknown";
            for (CartItem item : cartItems) {
                saleService.recordSale(item.getMedicine().getMedicineId(), item.getQuantity(), soldBy);
            }
            changeLabel.setText(payAmount.subtract(total).toString());
            AlertUtil.showInfo("Success", "Sale completed! Change: ৳" + payAmount.subtract(total));
            cartItems.clear();
            payAmountField.clear();
            updateTotals();
            loadMedicines();
        } catch (RuntimeException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void searchMedicine() {
        // Kept for compatibility with any Search button binding.
        performLiveSearch(searchField.getText());
    }

    @FXML
    private void filterByCategory() {
        applyFilters();
    }

    private void updateTotals() {
        grandTotalLabel.setText(getGrandTotal().toString());
    }

    private BigDecimal getGrandTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem item : cartItems) {
            total = total.add(item.getTotal());
        }
        return total;
    }

    private BigDecimal parseAmount(String text) {
        try {
            if (text == null || text.trim().isEmpty()) return BigDecimal.ZERO;
            return new BigDecimal(text.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @FXML private void dashboard(ActionEvent event) { navigate("dashboard"); }
    @FXML private void medicine(ActionEvent event) { navigate("medicine"); }
    @FXML private void supplier(ActionEvent event) { navigate("supplier"); }
    @FXML private void sales(ActionEvent event) { }
    @FXML private void salesHistory(ActionEvent event) { navigate("salesHistory"); }
    @FXML private void signOut(ActionEvent event) { navigate("login"); }

    private void navigate(String fxml) {
        try {
            PharmacyManagement.sceneChange(fxml);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    public static class CartItem {
        private Medicine medicine;
        private int quantity;

        public CartItem(Medicine medicine, int quantity) {
            this.medicine = medicine;
            this.quantity = quantity;
        }

        public Medicine getMedicine() { return medicine; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getTotal() { return medicine.getSellingPrice().multiply(BigDecimal.valueOf(quantity)); }
    }
}
