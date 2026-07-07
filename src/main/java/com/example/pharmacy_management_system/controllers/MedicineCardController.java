package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.models.Medicine;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Controller for a single medicine card.
 * Follows Single Responsibility Principle: only handles card UI and add events.
 */
public class MedicineCardController {

    @FXML private ImageView imageView;
    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label priceLabel;
    @FXML private Label stockLabel;
    @FXML private Spinner<Integer> spinner;
    @FXML private javafx.scene.control.Button addButton;

    private Medicine medicine;
    private MedicineCardListener listener;

    public interface MedicineCardListener {
        void onAddToCart(Medicine medicine, int quantity);
        void onViewDetails(Medicine medicine);
    }

    public void setData(Medicine medicine, MedicineCardListener listener) {
        this.medicine = medicine;
        this.listener = listener;

        idLabel.setText("ID: " + medicine.getMedicineId());
        nameLabel.setText(medicine.getName());
        priceLabel.setText(String.format("%.2f", medicine.getSellingPrice()));
        stockLabel.setText("Stock: " + medicine.getQuantity());

        if (medicine.getQuantity() < 20) {
            stockLabel.setText("Low: " + medicine.getQuantity());
            stockLabel.setStyle("-fx-text-fill: #ff9800;");
        }

        String path = medicine.getImagePath();
        String defaultPath = System.getProperty("user.dir") + "/src/main/resources/images/default_medicine.png";
        if (path == null || path.trim().isEmpty()) {
            path = defaultPath;
        } else if (!path.startsWith("file:") && !path.startsWith("/") && !(path.length() > 1 && path.charAt(1) == ':')) {
            path = System.getProperty("user.dir") + "/src/main/resources/" + path;
        }
        try {
            imageView.setImage(new Image("file:" + path));
        } catch (Exception e) {
            imageView.setImage(new Image("file:" + defaultPath));
        }

        int maxQuantity = Math.max(1, medicine.getQuantity());
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, maxQuantity, 1));
    }

    @FXML
    private void addToCart() {
        if (listener != null && medicine != null) {
            listener.onAddToCart(medicine, spinner.getValue());
        }
    }

    @FXML
    private void viewDetails() {
        if (listener != null && medicine != null) {
            listener.onViewDetails(medicine);
        }
    }
}
