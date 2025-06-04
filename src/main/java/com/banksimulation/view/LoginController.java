package com.banksimulation.view;

import com.banksimulation.App; // 引入App类
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
 * 登录界面控制器
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

    // 构造函数，通过App类进行依赖注入
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
        String username = usernameField.getText().trim(); // 添加 trim()
        String password = passwordField.getText().trim(); // 添加 trim()

        // 尝试用户登录
        Optional<User> userOptional = authenticationService.loginUser(username, password);
        if (userOptional.isPresent()) {
            messageLabel.setText("用户登录成功！");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            navigateToUserDashboard(userOptional.get());
            return;
        }

        // 如果用户登录失败，尝试管理员登录
        Optional<com.banksimulation.entity.Admin> adminOptional = authenticationService.loginAdmin(username, password);
        if (adminOptional.isPresent()) {
            messageLabel.setText("管理员登录成功！");
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            navigateToAdminDashboard(adminOptional.get());
            return;
        }

        // 登录失败
        messageLabel.setText("登录失败：用户名或密码错误。");
        messageLabel.setTextFill(javafx.scene.paint.Color.RED);
    }

    @FXML
    private void handleRegister(ActionEvent event) {
        String username = usernameField.getText().trim(); // 添加 trim()
        String password = passwordField.getText().trim(); // 添加 trim()

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("注册失败：用户名和密码不能为空。"); // Registration failed: Username and password cannot be empty.
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
            return;
        }

        String hashedPassword = PasswordHasher.hashPassword(password);
        // 自动生成一个简单的账号，实际应用中需要更严谨的账号生成逻辑，例如确保唯一性
        // 这里只是一个示例，实际生产环境需要更健壮的账号生成和校验机制
        String accountNumber = String.valueOf(System.currentTimeMillis()).substring(0, 10);
        User newUser = new User(username, hashedPassword, "", "", accountNumber); // 暂时留空first/last name

        if (authenticationService.registerUser(newUser)) {
            messageLabel.setText("注册成功！请登录。"); // Registration successful! Please log in.
            messageLabel.setTextFill(javafx.scene.paint.Color.GREEN);
            usernameField.clear();
            passwordField.clear();
        } else {
            messageLabel.setText("注册失败：用户名已存在或发生其他错误。"); // Registration failed: Username already exists or other error occurred.
            messageLabel.setTextFill(javafx.scene.paint.Color.RED);
        }
    }

    /**
     * 导航到用户仪表板
     * Navigates to the user dashboard.
     * @param user The logged-in user.
     */
    private void navigateToUserDashboard(User user) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/UserDashboardView.fxml"));
            App app = (App) primaryStage.getUserData(); // 获取App实例

            fxmlLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == UserDashboardController.class) {
                    return new UserDashboardController(
                            app.getUserService(),
                            app.getLoggingService(),
                            primaryStage,
                            user // 传递当前登录的用户对象
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
            primaryStage.setTitle("银行模拟系统 - 用户仪表板");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading user dashboard view: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 导航到管理员仪表板
     * Navigates to the admin dashboard.
     * @param admin The logged-in admin.
     */
    private void navigateToAdminDashboard(com.banksimulation.entity.Admin admin) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/AdminDashboardView.fxml"));
            App app = (App) primaryStage.getUserData(); // 获取App实例

            fxmlLoader.setControllerFactory(controllerClass -> {
                if (controllerClass == AdminDashboardController.class) {
                    return new AdminDashboardController(
                            app.getAdminService(),
                            app.getUserService(), // AdminDashboard可能需要UserService来获取用户详情
                            app.getLoggingService(),
                            primaryStage,
                            admin // 传递当前登录的管理员对象
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
            primaryStage.setTitle("银行模拟系统 - 管理员仪表板");
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            System.err.println("Error loading admin dashboard view: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
