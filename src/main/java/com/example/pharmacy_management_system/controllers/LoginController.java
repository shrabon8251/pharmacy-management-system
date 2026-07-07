package com.example.pharmacy_management_system.controllers;

import com.example.pharmacy_management_system.PharmacyManagement;
import com.example.pharmacy_management_system.models.User;
import com.example.pharmacy_management_system.services.UserService;
import com.example.pharmacy_management_system.utility.AlertUtil;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Login controller following SRP: handles login, registration, and password reset forms.
 */
public class LoginController implements Initializable {

    private static final String[] SECURITY_QUESTIONS = {
            "What is your mother's middle name?",
            "What was the name of your first pet?",
            "What is your favorite colour?",
            "What was the name of your first school?",
            "What city were you born in?",
            "What is your favorite food?"
    };

    @FXML private VBox loginPane;
    @FXML private VBox createAccountPane;
    @FXML private VBox forgotPasswordPane;

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField passwordTextField;
    @FXML private CheckBox showPasswordCheckBox;

    @FXML private TextField caFullNameField;
    @FXML private TextField caUsernameField;
    @FXML private TextField caEmailField;
    @FXML private TextField caPhoneField;
    @FXML private ComboBox<String> caQuestionBox;
    @FXML private TextField caAnswerField;
    @FXML private PasswordField caPasswordField;
    @FXML private TextField caPasswordTextField;
    @FXML private CheckBox showCreatePasswordCheckBox;
    @FXML private PasswordField caConfirmPasswordField;
    @FXML private TextField caConfirmPasswordTextField;
    @FXML private CheckBox showCreateConfirmPasswordCheckBox;

    @FXML private TextField fpUsernameField;
    @FXML private ComboBox<String> fpQuestionBox;
    @FXML private TextField fpAnswerField;
    @FXML private PasswordField fpNewPasswordField;
    @FXML private TextField fpNewPasswordTextField;
    @FXML private CheckBox showForgotPasswordCheckBox;
    @FXML private PasswordField fpConfirmPasswordField;
    @FXML private TextField fpConfirmPasswordTextField;
    @FXML private CheckBox showForgotConfirmPasswordCheckBox;

    private final UserService userService = new UserService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        caQuestionBox.setItems(FXCollections.observableArrayList(SECURITY_QUESTIONS));
        fpQuestionBox.setItems(FXCollections.observableArrayList(SECURITY_QUESTIONS));

        bindPasswordFields(passwordField, passwordTextField);
        bindPasswordFields(caPasswordField, caPasswordTextField);
        bindPasswordFields(caConfirmPasswordField, caConfirmPasswordTextField);
        bindPasswordFields(fpNewPasswordField, fpNewPasswordTextField);
        bindPasswordFields(fpConfirmPasswordField, fpConfirmPasswordTextField);

        showLogin();
    }

    private void bindPasswordFields(PasswordField passwordField, TextField textField) {
        passwordField.textProperty().bindBidirectional(textField.textProperty());
    }

    @FXML
    private void toggleLoginPasswordVisibility() {
        togglePasswordVisibility(passwordField, passwordTextField, showPasswordCheckBox);
    }

    @FXML
    private void toggleCreatePasswordVisibility() {
        togglePasswordVisibility(caPasswordField, caPasswordTextField, showCreatePasswordCheckBox);
    }

    @FXML
    private void toggleCreateConfirmPasswordVisibility() {
        togglePasswordVisibility(caConfirmPasswordField, caConfirmPasswordTextField, showCreateConfirmPasswordCheckBox);
    }

    @FXML
    private void toggleForgotPasswordVisibility() {
        togglePasswordVisibility(fpNewPasswordField, fpNewPasswordTextField, showForgotPasswordCheckBox);
    }

    @FXML
    private void toggleForgotConfirmPasswordVisibility() {
        togglePasswordVisibility(fpConfirmPasswordField, fpConfirmPasswordTextField, showForgotConfirmPasswordCheckBox);
    }

    private void togglePasswordVisibility(PasswordField passwordField, TextField textField, CheckBox checkBox) {
        boolean show = checkBox.isSelected();
        passwordField.setVisible(!show);
        passwordField.setManaged(!show);
        textField.setVisible(show);
        textField.setManaged(show);
    }

    @FXML
    private void Login() {
        try {
            String username = usernameField.getText().trim();
            String password = passwordField.getText();

            User user = userService.authenticate(username, password);
            PharmacyManagement.currentUser = user.getUsername();
            AlertUtil.showInfo("Login Successful", "Welcome, " + user.getUsername());
            PharmacyManagement.sceneChange("dashboard");
        } catch (RuntimeException e) {
            AlertUtil.showError("Login Failed", e.getMessage());
        } catch (IOException e) {
            AlertUtil.showError("Navigation Error", e.getMessage());
        }
    }

    @FXML
    private void showLogin() {
        switchPane(loginPane);
    }

    @FXML
    private void showCreateAccount() {
        switchPane(createAccountPane);
    }

    @FXML
    private void showForgotPassword() {
        switchPane(forgotPasswordPane);
    }

    private void switchPane(VBox targetPane) {
        final VBox[] currentPane = new VBox[1];
        if (loginPane.isVisible()) currentPane[0] = loginPane;
        else if (createAccountPane.isVisible()) currentPane[0] = createAccountPane;
        else if (forgotPasswordPane.isVisible()) currentPane[0] = forgotPasswordPane;

        if (currentPane[0] == targetPane) return;

        if (currentPane[0] != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentPane[0]);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                currentPane[0].setVisible(false);
                targetPane.setVisible(true);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), targetPane);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            });
            fadeOut.play();
        } else {
            targetPane.setVisible(true);
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), targetPane);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        }
    }

    @FXML
    private void createAccount() {
        String fullName = caFullNameField.getText().trim();
        String username = caUsernameField.getText().trim();
        String email = caEmailField.getText().trim();
        String phone = caPhoneField.getText().trim();
        String question = caQuestionBox.getValue();
        String answer = caAnswerField.getText().trim();
        String password = caPasswordField.getText();
        String confirmPassword = caConfirmPasswordField.getText();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || phone.isEmpty()
                || question == null || answer.isEmpty() || password.isEmpty()) {
            AlertUtil.showError("Error", "All fields are required.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            AlertUtil.showError("Error", "Passwords do not match.");
            return;
        }
        if (password.length() < 4) {
            AlertUtil.showError("Error", "Password must be at least 4 characters.");
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setPhone(phone);
        user.setSecurityQuestion(question);
        user.setSecurityAnswer(answer);

        try {
            userService.register(user);
            AlertUtil.showInfo("Success", "Account created successfully. Please log in.");
            clearCreateAccountFields();
            showLogin();
        } catch (RuntimeException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    @FXML
    private void forgotPassword() {
        String username = fpUsernameField.getText().trim();
        String question = fpQuestionBox.getValue();
        String answer = fpAnswerField.getText().trim();
        String newPassword = fpNewPasswordField.getText();
        String confirmPassword = fpConfirmPasswordField.getText();

        if (username.isEmpty() || question == null || answer.isEmpty() || newPassword.isEmpty()) {
            AlertUtil.showError("Error", "All fields are required.");
            return;
        }
        if (!newPassword.equals(confirmPassword)) {
            AlertUtil.showError("Error", "Passwords do not match.");
            return;
        }
        if (newPassword.length() < 4) {
            AlertUtil.showError("Error", "Password must be at least 4 characters.");
            return;
        }

        try {
            userService.resetPassword(username, question, answer, newPassword);
            AlertUtil.showInfo("Success", "Password reset successfully. Please log in.");
            clearForgotPasswordFields();
            showLogin();
        } catch (RuntimeException e) {
            AlertUtil.showError("Error", e.getMessage());
        }
    }

    private void clearCreateAccountFields() {
        caFullNameField.clear();
        caUsernameField.clear();
        caEmailField.clear();
        caPhoneField.clear();
        caQuestionBox.getSelectionModel().clearSelection();
        caAnswerField.clear();
        caPasswordField.clear();
        caConfirmPasswordField.clear();
    }

    private void clearForgotPasswordFields() {
        fpUsernameField.clear();
        fpQuestionBox.getSelectionModel().clearSelection();
        fpAnswerField.clear();
        fpNewPasswordField.clear();
        fpConfirmPasswordField.clear();
    }
}
