package com.banksimulation;

import com.banksimulation.dao.InMemoryDAO;
import com.banksimulation.entity.Admin; // 导入 Admin 类
import com.banksimulation.entity.User; // 导入 User 类
import com.banksimulation.service.AdminService;
import com.banksimulation.service.AuthenticationService;
import com.banksimulation.service.LoggingService;
import com.banksimulation.service.UserService;
import com.banksimulation.util.PasswordHasher; // 导入 PasswordHasher
import com.banksimulation.view.AdminDashboardController;
import com.banksimulation.view.LoginController;
import com.banksimulation.view.UserDashboardController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * JavaFX 银行模拟系统主应用程序类
 * Main JavaFX application class for the Bank Simulation System.
 * This class initializes services and displays the login view.
 */
public class App extends Application {

    // 声明服务实例
    private InMemoryDAO dao;
    private LoggingService loggingService;
    private AuthenticationService authenticationService;
    private UserService userService;
    private AdminService adminService;

    // Getters for services (用于控制器工厂注入)
    public AuthenticationService getAuthenticationService() {
        return authenticationService;
    }

    public UserService getUserService() {
        return userService;
    }

    public AdminService getAdminService() {
        return adminService;
    }

    public LoggingService getLoggingService() {
        return loggingService;
    }

    @Override
    public void init() throws Exception {
        super.init();
        // 初始化DAO和所有服务
        dao = new InMemoryDAO(); // 使用内存DAO，其构造函数已预设数据
        loggingService = new LoggingService(dao);
        authenticationService = new AuthenticationService(dao, loggingService);
        userService = new UserService(dao, loggingService);
        adminService = new AdminService(dao, loggingService);

        System.out.println("Services initialized successfully.");
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        primaryStage.setUserData(this); // 将App实例存储到Stage中，方便控制器获取服务

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("view/LoginView.fxml"));
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == LoginController.class) {
                return new LoginController(authenticationService, userService, adminService, loggingService, primaryStage);
            } else if (controllerClass == UserDashboardController.class) {
                throw new UnsupportedOperationException("UserDashboardController should be instantiated with a User object via LoginController.");
            } else if (controllerClass == AdminDashboardController.class) {
                throw new UnsupportedOperationException("AdminDashboardController should be instantiated with an Admin object via LoginController.");
            }
            try {
                return controllerClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(fxmlLoader.load(), 800, 600);
        primaryStage.setTitle("银行模拟系统 - 登录");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen(); // 窗口居中显示
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
