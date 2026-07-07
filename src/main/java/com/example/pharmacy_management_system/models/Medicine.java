package com.example.pharmacy_management_system.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Medicine {

    private int medicineId;
    private String name;
    private String category;
    private String manufacturer;
    private String description;
    private BigDecimal buyingPrice;
    private BigDecimal sellingPrice;
    private int quantity;
    private LocalDate expiryDate;
    private String imagePath;

    public Medicine() {
    }

    public Medicine(int medicineId, String name, String category, String manufacturer,
                      BigDecimal buyingPrice, BigDecimal sellingPrice, int quantity, LocalDate expiryDate) {
        this(medicineId, name, category, manufacturer, buyingPrice, sellingPrice, quantity, expiryDate, "images/default_medicine.png");
    }

    public Medicine(int medicineId, String name, String category, String manufacturer,
                      BigDecimal buyingPrice, BigDecimal sellingPrice, int quantity, LocalDate expiryDate, String imagePath) {
        this(medicineId, name, category, manufacturer, null, buyingPrice, sellingPrice, quantity, expiryDate, imagePath);
    }

    public Medicine(int medicineId, String name, String category, String manufacturer, String description,
                      BigDecimal buyingPrice, BigDecimal sellingPrice, int quantity, LocalDate expiryDate, String imagePath) {
        this.medicineId = medicineId;
        this.name = name;
        this.category = category;
        this.manufacturer = manufacturer;
        this.description = description;
        this.buyingPrice = buyingPrice;
        this.sellingPrice = sellingPrice;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.imagePath = imagePath;
    }

    public int getMedicineId() {
        return medicineId;
    }

    public void setMedicineId(int medicineId) {
        this.medicineId = medicineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getBuyingPrice() {
        return buyingPrice;
    }

    public void setBuyingPrice(BigDecimal buyingPrice) {
        this.buyingPrice = buyingPrice;
    }

    public BigDecimal getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(BigDecimal sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isLowStock() {
        return quantity < 20;
    }
}
