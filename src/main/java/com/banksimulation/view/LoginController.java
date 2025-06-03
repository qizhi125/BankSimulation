package com.banksimulation.view;

import com.banksimulation.App;
import com.banksimulation.entity.Admin; // Import Admin class
import com.banksimulation.entity.User;
import com.banksimulation.service.AdminService;
import com.banksimulation.service.AuthenticationService;
import com.banksimulation.service.LoggingService;
import com.banksimulation.service.UserService;
import com.banksimulation.util.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * Login screen controller
 * Controller for the login view.
 */
public class LoginController {

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label messageLabel;

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final AdminService adminService;
    private final LoggingService loggingService;
    private final Stage primaryStage;

    // Constructor, dependency injection via App class
    public LoginController(AuthenticationService authenticationService, UserService userService,
                           AdminService adminService, LoggingService loggingService, Stage primaryStage) {
        this.authenticationService = authenticationService;
        this.userService = userService;
        this.adminService = adminService;
        this.loggingService = loggingService;
        this.primaryStage = primaryStage;
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        // Attempt user login
        Optional<User> userOptional = authenticationService.loginUser(username, password);
        if (userOptional.isPresent()) {
            messageLabel.setText("User login successful!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            navigateToUserDashboard(userOptional.get());
            return;
        }

        // If user login fails, attempt admin login
        Optional<Admin> adminOptional = authenticationService.loginAdmin(username, password); // Receive Admin object
        if (adminOptional.isPresent()) {
            messageLabel.setText("Admin login successful!");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            navigateToAdminDashboard(adminOptional.get()); // Pass Admin object
            return;
        }

        // Login failed
        messageLabel.setText("Login failed: Invalid username or password.");
        messageLabel.setTextFill(javafx.scene.paint.Color.RED);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Registration failed: Username and password cannot be empty.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        String accountNumber = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        User newUser = new User(username, hashedPassword, "", "", accountNumber);

        if (authenticationService.registerUser(newUser)) {
            messageLabel.setText("Registration successful! Please log in.");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setText("Registration failed: Username already exists or other error occurred.");
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * Navigate to user dashboard
     * Navigates to the user dashboard.
     * @param user The logged-in user.
     */
    private void navigateToUserDashboard(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/UserDashboardView.fxml"));
            App app = (App) primaryStage.getUserData();

            fxmlLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == UserDashboardController.class) {
                    return new UserDashboardController(
                            app.getUserService(),
                            app.getLoggingService(),
                            primaryStage,
                            user
                    );
                }
                try {
                    return controllerClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Bank Simulation System - User Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading user dashboard view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Navigate to admin dashboard
     * Navigates to the admin dashboard.
     * @param admin The logged-in admin (as an Admin object).
     */
    private void navigateToAdminDashboard(Admin admin) { // Receive Admin object
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/AdminDashboardView.fxml"));
            App app = (App) primaryStage.getUserData();

            fxmlLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == com.banksimulation.view.AdminDashboardController.class) {
                    com.banksimulation.view.AdminDashboardController adminController = new com.banksimulation.view.AdminDashboardController(
                            app.getAdminService(),
                            app.getUserService(), // AdminDashboard might need UserService to get user details
                            app.getLoggingService(),
                            primaryStage,
                            admin // Pass Admin object
                    );
                    return adminController;
                }
                try {
                    return controllerClass.newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root);
            primaryStage.setTitle("Bank Simulation System - Admin Dashboard");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading admin dashboard view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
