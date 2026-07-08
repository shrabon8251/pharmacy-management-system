# Pharmacy Management System


sql

CREATE DATABASE IF NOT EXISTS pharmacy_management_system;

USE pharmacy_management_system;

DROP TABLE IF EXISTS sales;
DROP TABLE IF EXISTS medicines;
DROP TABLE IF EXISTS suppliers;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    security_question VARCHAR(255) NOT NULL,
    security_answer VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE suppliers (
    supplier_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE medicines (
    medicine_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50) NOT NULL,
    manufacturer VARCHAR(100) NOT NULL,
    description TEXT,
    buying_price DECIMAL(10, 2) NOT NULL,
    selling_price DECIMAL(10, 2) NOT NULL,
    quantity INT NOT NULL,
    expiry_date DATE NOT NULL,
    image_path VARCHAR(255) DEFAULT 'images/default_medicine.png',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE sales (
    sale_id INT AUTO_INCREMENT PRIMARY KEY,
    medicine_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    total_price DECIMAL(10, 2) NOT NULL,
    sold_by VARCHAR(50) NOT NULL,
    sale_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (medicine_id) REFERENCES medicines(medicine_id)
);

INSERT INTO users (full_name, username, password, email, phone, security_question, security_answer)
VALUES ('System Admin', 'admin', 'admin123', 'admin@medicare.com', '01700000000', 'What is your favorite color?', 'blue');

INSERT INTO suppliers (name, phone, email, address) VALUES
    ('ABC Pharma Ltd.', '01711234567', 'abc@pharma.com', 'Dhaka, Bangladesh'),
    ('MediCare Supplies', '01876543210', 'info@medicare.com', 'Chittagong, Bangladesh');

INSERT INTO medicines (name, category, manufacturer, buying_price, selling_price, quantity, expiry_date) VALUES
    ('Paracetamol 500mg', 'Tablet', 'Square Pharma', 3.50, 5.00, 150, '2026-12-31'),
    ('Napa 200mg', 'Tablet', 'Beximco Pharma', 2.00, 3.50, 45, '2026-10-15'),
    ('Amoxicillin 250mg', 'Antibiotic', 'Incepta Pharma', 25.00, 35.00, 200, '2027-05-20'),
    ('Cough Syrup', 'Syrup', 'ACI Pharma', 45.00, 60.00, 12, '2026-03-30');

INSERT INTO sales (medicine_id, quantity, unit_price, total_price, sold_by, sale_date) VALUES
    (1, 10, 5.00, 50.00, 'admin', NOW()),
    (3, 5, 35.00, 175.00, 'admin', NOW()),
    (2, 2, 3.50, 7.00, 'admin', DATE_SUB(NOW(), INTERVAL 1 DAY));



