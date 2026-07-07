package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.models.Medicine;
import com.example.pharmacy_management_system.services.MedicineService;
import com.example.pharmacy_management_system.services.SaleService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Medicine controller following SRP: manages medicine grid, add/edit dialogs, search, and filters.
 */
public class MedicineController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane cardsContainer;
    @FXML private ComboBox<String> categoryFilterBox;

    private final MedicineService medicineService = new MedicineService();
    private final SaleService saleService = new SaleService();
    private ObservableList<Medicine> allMedicines = FXCollections.observableArrayList();
    private ObservableList<Medicine> displayedMedicines = FXCollections.observableArrayList();
    private String currentSearchText = "";
    private String filterManufacturer = "All";
    private String filterStockStatus = "All";
    private LocalDate filterExpiryFrom = null;
    private LocalDate filterExpiryTo = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
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

    private void renderCards() {
        cardsContainer.getChildren().clear();
        for (Medicine medicine : displayedMedicines) {
            cardsContainer.getChildren().add(createMedicineCard(medicine));
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
                            boolean matchesManufacturer = filterManufacturer.equals("All") || filterManufacturer.equals(m.getManufacturer());
                            boolean matchesStock = switch (filterStockStatus) {
                                case "In Stock" -> m.getQuantity() > 20;
                                case "Low Stock" -> m.getQuantity() > 0 && m.getQuantity() <= 20;
                                case "Out of Stock" -> m.getQuantity() == 0;
                                default -> true;
                            };
                            boolean matchesExpiry = true;
                            if (filterExpiryFrom != null) {
                                matchesExpiry = !m.getExpiryDate().isBefore(filterExpiryFrom);
                            }
                            if (filterExpiryTo != null) {
                                matchesExpiry = matchesExpiry && !m.getExpiryDate().isAfter(filterExpiryTo);
                            }
                            return matchesSearch && matchesCategory && matchesManufacturer && matchesStock && matchesExpiry;
                        })
                        .collect(Collectors.toList())
        );
        renderCards();
    }

    private VBox createMedicineCard(Medicine medicine) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(250);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 0, 3); -fx-padding: 15; -fx-cursor: hand;");

        Label idLabel = new Label("ID: " + medicine.getMedicineId());
        idLabel.setFont(Font.font("System Bold", 10));
        idLabel.setTextFill(Color.web("#78909c"));

        ImageView imageView = new ImageView(loadMedicineImage(medicine.getImagePath()));
        imageView.setFitHeight(100);
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);

        Label nameLabel = new Label(medicine.getName());
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(160);
        nameLabel.setFont(Font.font("System Bold", 14));
        nameLabel.setTextFill(Color.web("#37474f"));

        Label priceLabel = new Label("৳ " + medicine.getSellingPrice());
        priceLabel.setFont(Font.font("System Bold", 16));
        priceLabel.setTextFill(Color.web("#43a047"));

        Label quantityLabel = new Label("Stock: " + medicine.getQuantity());
        quantityLabel.setFont(Font.font(12));
        quantityLabel.setTextFill(Color.web("#78909c"));
        if (medicine.getQuantity() < 20) {
            quantityLabel.setText("Low Stock: " + medicine.getQuantity());
            quantityLabel.setTextFill(Color.web("#ff9800"));
        }

        card.getChildren().addAll(idLabel, imageView, nameLabel, priceLabel, quantityLabel);
        card.setOnMouseClicked(event -> showMedicineDetails(medicine));
        return card;
    }

    @FXML
    private void searchMedicine() {
        // Kept for compatibility with the Search button; live search handles typing.
        performLiveSearch(searchField.getText());
    }

    @FXML
    private void filterByCategory() {
        applyFilters();
    }

    @FXML
    private void showAddDialog() {
        showMedicineDialog(null);
    }

    @FXML
    private void showAdvancedFilterDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Advanced Filter");
        dialog.setHeaderText("Choose filter options");
        ButtonType applyType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        ButtonType resetType = new ButtonType("Reset", ButtonBar.ButtonData.OTHER);
        dialog.getDialogPane().getButtonTypes().addAll(applyType, resetType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 20));

        ComboBox<String> categoryBox = new ComboBox<>();
        categoryBox.setItems(categoryFilterBox.getItems());
        categoryBox.setValue(categoryFilterBox.getValue());
        categoryBox.setPrefWidth(200);

        ComboBox<String> manufacturerBox = new ComboBox<>();
        ObservableList<String> manufacturers = FXCollections.observableArrayList("All");
        manufacturers.addAll(allMedicines.stream().map(Medicine::getManufacturer).distinct().sorted().collect(Collectors.toList()));
        manufacturerBox.setItems(manufacturers);
        manufacturerBox.setValue(filterManufacturer);
        manufacturerBox.setPrefWidth(200);

        ComboBox<String> stockBox = new ComboBox<>(FXCollections.observableArrayList("All", "In Stock", "Low Stock", "Out of Stock"));
        stockBox.setValue(filterStockStatus);
        stockBox.setPrefWidth(200);

        DatePicker expiryFrom = new DatePicker(filterExpiryFrom);
        DatePicker expiryTo = new DatePicker(filterExpiryTo);

        grid.add(new Label("Category:"), 0, 0); grid.add(categoryBox, 1, 0);
        grid.add(new Label("Manufacturer/Company:"), 0, 1); grid.add(manufacturerBox, 1, 1);
        grid.add(new Label("Stock Status:"), 0, 2); grid.add(stockBox, 1, 2);
        grid.add(new Label("Expiry From:"), 0, 3); grid.add(expiryFrom, 1, 3);
        grid.add(new Label("Expiry To:"), 0, 4); grid.add(expiryTo, 1, 4);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return;
        }
        ButtonType data = result.get();
        if (data == resetType) {
            resetAdvancedFilters();
        } else if (data == applyType) {
            applyAdvancedFilters(
                    categoryBox.getValue(),
                    manufacturerBox.getValue(),
                    stockBox.getValue(),
                    expiryFrom.getValue(),
                    expiryTo.getValue()
            );
        }
    }

    private void resetAdvancedFilters() {
        filterManufacturer = "All";
        filterStockStatus = "All";
        filterExpiryFrom = null;
        filterExpiryTo = null;
        categoryFilterBox.setValue("All");
        searchField.clear();
        currentSearchText = "";
        applyFilters();
    }

    private void applyAdvancedFilters(String category, String manufacturer, String stockStatus, LocalDate expiryFrom, LocalDate expiryTo) {
        categoryFilterBox.setValue(category == null || category.isEmpty() ? "All" : category);
        filterManufacturer = manufacturer == null || manufacturer.isEmpty() ? "All" : manufacturer;
        filterStockStatus = stockStatus == null || stockStatus.isEmpty() ? "All" : stockStatus;
        filterExpiryFrom = expiryFrom;
        filterExpiryTo = expiryTo;
        applyFilters();
    }

    private void showMedicineDetails(Medicine medicine) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Medicine Details");
        alert.setHeaderText("ID: " + medicine.getMedicineId() + " - " + medicine.getName());
        alert.setContentText("Category: " + medicine.getCategory()
                + "\nManufacturer: " + medicine.getManufacturer()
                + "\nDescription: " + (medicine.getDescription() != null ? medicine.getDescription() : "N/A")
                + "\nBuying Price: ৳" + medicine.getBuyingPrice()
                + "\nSelling Price: ৳" + medicine.getSellingPrice()
                + "\nStock: " + medicine.getQuantity()
                + "\nExpiry: " + medicine.getExpiryDate());

        ButtonType editButton = new ButtonType("Edit");
        ButtonType deleteButton = new ButtonType("Delete");
        ButtonType closeButton = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(editButton, deleteButton, closeButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent()) {
            if (result.get() == editButton) {
                showMedicineDialog(medicine);
            } else if (result.get() == deleteButton) {
                deleteMedicine(medicine);
            }
        }
    }

    private void showMedicineDialog(Medicine existing) {
        Dialog<Medicine> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Add Medicine" : "Edit Medicine");
        dialog.setHeaderText(existing == null ? "Enter medicine details" : "Update medicine details");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 20, 10, 10));

        TextField idField = new TextField(); idField.setPromptText("Auto / ID");
        TextField nameField = new TextField(); nameField.setPromptText("Name");

        ComboBox<String> categoryField = new ComboBox<>();
        categoryField.setEditable(true);
        categoryField.setPromptText("Category");
        categoryField.setPrefWidth(300);
        categoryField.setMaxWidth(Double.MAX_VALUE);
        ObservableList<String> categories = FXCollections.observableArrayList(
                allMedicines.stream().map(Medicine::getCategory).distinct().sorted().collect(Collectors.toList())
        );
        categoryField.setItems(categories);

        ComboBox<String> manufacturerField = new ComboBox<>();
        manufacturerField.setEditable(true);
        manufacturerField.setPromptText("Manufacturer / Company");
        manufacturerField.setPrefWidth(300);
        manufacturerField.setMaxWidth(Double.MAX_VALUE);
        ObservableList<String> manufacturers = FXCollections.observableArrayList(
                allMedicines.stream().map(Medicine::getManufacturer).distinct().sorted().collect(Collectors.toList())
        );
        manufacturerField.setItems(manufacturers);

        TextArea descriptionArea = new TextArea(); descriptionArea.setPromptText("Description"); descriptionArea.setPrefRowCount(3);
        TextField buyingPriceField = new TextField(); buyingPriceField.setPromptText("0.00");
        TextField sellingPriceField = new TextField(); sellingPriceField.setPromptText("0.00");
        TextField quantityField = new TextField(); quantityField.setPromptText("Stock");
        DatePicker expiryDatePicker = new DatePicker();
        TextField imagePathField = new TextField(); imagePathField.setPromptText("images/name.png");
        Button browseButton = new Button("Browse");
        browseButton.setOnAction(e -> browseImage(imagePathField));

        if (existing != null) {
            idField.setText(String.valueOf(existing.getMedicineId()));
            nameField.setText(existing.getName());
            categoryField.setValue(existing.getCategory());
            categoryField.getEditor().setText(existing.getCategory());
            manufacturerField.setValue(existing.getManufacturer());
            manufacturerField.getEditor().setText(existing.getManufacturer());
            descriptionArea.setText(existing.getDescription());
            buyingPriceField.setText(existing.getBuyingPrice().toString());
            sellingPriceField.setText(existing.getSellingPrice().toString());
            quantityField.setText(String.valueOf(existing.getQuantity()));
            expiryDatePicker.setValue(existing.getExpiryDate());
            imagePathField.setText(existing.getImagePath());
        }

        GridPane.setHgrow(idField, Priority.ALWAYS);
        GridPane.setHgrow(nameField, Priority.ALWAYS);
        GridPane.setHgrow(categoryField, Priority.ALWAYS);
        GridPane.setHgrow(manufacturerField, Priority.ALWAYS);
        GridPane.setHgrow(descriptionArea, Priority.ALWAYS);
        GridPane.setHgrow(buyingPriceField, Priority.ALWAYS);
        GridPane.setHgrow(sellingPriceField, Priority.ALWAYS);
        GridPane.setHgrow(quantityField, Priority.ALWAYS);
        GridPane.setHgrow(expiryDatePicker, Priority.ALWAYS);
        GridPane.setHgrow(imagePathField, Priority.ALWAYS);

        grid.add(new Label("Medicine ID:"), 0, 0); grid.add(idField, 1, 0);
        grid.add(new Label("Name:"), 0, 1); grid.add(nameField, 1, 1);
        grid.add(new Label("Category:"), 0, 2); grid.add(categoryField, 1, 2);
        grid.add(new Label("Manufacturer:"), 0, 3); grid.add(manufacturerField, 1, 3);
        grid.add(new Label("Description:"), 0, 4); grid.add(descriptionArea, 1, 4);
        grid.add(new Label("Buying Price:"), 0, 5); grid.add(buyingPriceField, 1, 5);
        grid.add(new Label("Selling Price:"), 0, 6); grid.add(sellingPriceField, 1, 6);
        grid.add(new Label("Quantity:"), 0, 7); grid.add(quantityField, 1, 7);
        grid.add(new Label("Expiry Date:"), 0, 8); grid.add(expiryDatePicker, 1, 8);
        grid.add(new Label("Image Path:"), 0, 9); grid.add(new HBox(8, imagePathField, browseButton), 1, 9);

        dialog.getDialogPane().setContent(grid);

        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.addEventFilter(ActionEvent.ACTION, event -> {
            try {
                Medicine medicine = new Medicine();
                medicine.setMedicineId(Integer.parseInt(idField.getText().trim()));
                medicine.setName(nameField.getText().trim());
                String category = categoryField.getEditor().getText().trim();
                String manufacturer = manufacturerField.getEditor().getText().trim();
                medicine.setCategory(category.isEmpty() ? categoryField.getValue() : category);
                medicine.setManufacturer(manufacturer.isEmpty() ? manufacturerField.getValue() : manufacturer);
                medicine.setDescription(descriptionArea.getText().trim());
                medicine.setBuyingPrice(new BigDecimal(buyingPriceField.getText().trim()));
                medicine.setSellingPrice(new BigDecimal(sellingPriceField.getText().trim()));
                medicine.setQuantity(Integer.parseInt(quantityField.getText().trim()));
                medicine.setExpiryDate(expiryDatePicker.getValue());
                String imagePath = imagePathField.getText().trim();
                medicine.setImagePath(imagePath.isEmpty() ? "images/default_medicine.png" : imagePath);

                if (existing == null) {
                    medicineService.saveMedicine(medicine);
                    AlertUtil.showInfo("Success", "Medicine added successfully.");
                } else {
                    medicineService.updateMedicine(medicine);
                    AlertUtil.showInfo("Success", "Medicine updated successfully.");
                }
                loadMedicines();
            } catch (Exception e) {
                AlertUtil.showError("Error", e.getMessage());
                event.consume();
            }
        });

        dialog.setResultConverter(dialogButton -> null);
        dialog.showAndWait();
    }

    private void deleteMedicine(Medicine medicine) {
        if (AlertUtil.showConfirmation("Confirm Delete", "Delete " + medicine.getName() + "?")) {
            try {
                if (saleService.hasSalesForMedicine(medicine.getMedicineId())) {
                    AlertUtil.showError("Cannot Delete", "This medicine has sales history. Delete sales records first or set stock to 0.");
                    return;
                }
                medicineService.deleteMedicine(medicine.getMedicineId());
                AlertUtil.showInfo("Success", "Medicine deleted successfully.");
                loadMedicines();
            } catch (RuntimeException e) {
                AlertUtil.showError("Error", e.getMessage());
            }
        }
    }

    private Image loadMedicineImage(String path) {
        String defaultPath = System.getProperty("user.dir") + "/src/main/resources/images/default_medicine.png";
        if (path == null || path.trim().isEmpty()) {
            path = defaultPath;
        } else if (!path.startsWith("file:") && !isAbsolutePath(path)) {
            // Backward compatibility for old relative paths stored as "images/filename.png"
            path = System.getProperty("user.dir") + "/src/main/resources/" + path;
        }
        try {
            return new Image("file:" + path);
        } catch (Exception e) {
            return new Image("file:" + defaultPath);
        }
    }

    private boolean isAbsolutePath(String path) {
        return path.startsWith("/") || (path.length() > 1 && path.charAt(1) == ':');
    }

    private void browseImage(TextField imagePathField) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Medicine Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );
        Stage stage = (Stage) imagePathField.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            imagePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML private void dashboard(ActionEvent event) { navigate("dashboard"); }
    @FXML private void medicine(ActionEvent event) { }
    @FXML private void supplier(ActionEvent event) { navigate("supplier"); }
    @FXML private void sales(ActionEvent event) { navigate("sales"); }
    @FXML private void salesHistory(ActionEvent event) { navigate("salesHistory"); }
    @FXML private void signOut(ActionEvent event) { navigate("login"); }

    private void navigate(String fxml) {
        try {
            PharmacyManagement.sceneChange(fxml);
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }
}
