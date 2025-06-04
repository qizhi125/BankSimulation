package com.banksimulation;

import com.banksimulation.dao.InMemoryDAO;
import com.banksimulation.entity.User; // 引入User实体
import com.banksimulation.service.AdminService;
import com.banksimulation.service.AuthenticationService;
import com.banksimulation.service.LoggingService;
import com.banksimulation.service.UserService;
import com.banksimulation.view.LoginController;
import com.banksimulation.view.UserDashboardController; // 引入UserDashboardController
import com.banksimulation.view.AdminDashboardController; // 引入AdminDashboardController
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

    @Override
    public void init() throws Exception {
        // 在这里初始化所有服务
        dao = new InMemoryDAO(); // 使用内存DAO
        loggingService = new LoggingService(dao);
        authenticationService = new AuthenticationService(dao, loggingService);
        userService = new UserService(dao, loggingService);
        adminService = new AdminService(dao, loggingService);

        System.out.println("Services initialized successfully.");
    }

    @Override
    public void start(Stage stage) throws IOException {
        // 将App实例保存到UserData，以便在控制器中访问其服务
        stage.setUserData(this);

        // 加载登录界面的 FXML 文件
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/banksimulation/view/LoginView.fxml"));
        // 设置控制器工厂，以便FXMLLoader能够注入服务
        fxmlLoader.setControllerFactory(controllerClass -> {
            if (controllerClass == LoginController.class) {
                return new LoginController(
                        authenticationService,
                        userService,
                        adminService,
                        loggingService,
                        stage // 传递主舞台
                );
            } else if (controllerClass == UserDashboardController.class) {
                // UserDashboardController的构造函数需要User对象，这里不会直接创建，而是从LoginController导航过来
                throw new UnsupportedOperationException("UserDashboardController should be instantiated with a User object from LoginController.");
            } else if (controllerClass == AdminDashboardController.class) {
                // AdminDashboardController的构造函数需要Admin对象，这里不会直接创建，而是从LoginController导航过来
                throw new UnsupportedOperationException("AdminDashboardController should be instantiated with an Admin object from LoginController.");
            }
            try {
                return controllerClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);

        stage.setTitle("银行模拟系统 - 登录"); // Bank Simulation System - Login
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

    // 提供getter方法以便其他控制器可以访问服务
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
}
