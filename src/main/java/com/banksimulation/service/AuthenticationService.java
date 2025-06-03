package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.Admin;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.User;
import com.banksimulation.util.PasswordHasher;

import java.util.Optional;

/**
 * 认证服务
 * Handles user and admin registration and login authentication.
 */
public class AuthenticationService {

    private final DataAccessObject dao;
    private final LoggingService loggingService; // 依赖日志服务

    public AuthenticationService(DataAccessObject dao, LoggingService loggingService) {
        this.dao = dao;
        this.loggingService = loggingService;
    }

    /**
     * 注册新用户
     * Registers a new user.
     * @param user The user object to register.
     * @return true if registration is successful, false otherwise (e.g., username already exists).
     */
    public boolean registerUser(User user) {
        // 检查用户名是否已存在
        if (dao.getUserByUsername(user.getUsername()).isPresent()) {
            System.out.println("Registration failed: Username '" + user.getUsername() + "' already exists.");
            loggingService.logSystemAction("User registration failed: Username '" + user.getUsername() + "' already exists.");
            return false;
        }
        // 检查账号是否已存在
        if (dao.getUserByAccountNumber(user.getAccountNumber()).isPresent()) {
            System.out.println("Registration failed: Account number '" + user.getAccountNumber() + "' already exists.");
            loggingService.logSystemAction("User registration failed: Account number '" + user.getAccountNumber() + "' already exists.");
            return false;
        }

        dao.saveUser(user);
        System.out.println("User registered: " + user.getUsername());
        loggingService.logSystemAction("New user registered: " + user.getUsername());
        return true;
    }

    /**
     * 用户登录验证
     * Authenticates a user login.
     * @param username The username.
     * @param password The plain text password.
     * @return An Optional containing the User object if login is successful, empty otherwise.
     */
    public Optional<User> loginUser(String username, String password) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 验证密码
            if (PasswordHasher.verifyPassword(password, user.getPasswordHash())) {
                if (user.isActive()) {
                    System.out.println("User '" + username + "' logged in successfully.");
                    loggingService.logUserAction(username, "Login successful", "User '" + username + "' logged in.");
                    return Optional.of(user);
                } else {
                    System.out.println("Login failed for '" + username + "': Account is inactive.");
                    loggingService.logUserAction(username, "Login failed: Inactive account", "User '" + username + "' account is inactive.");
                    return Optional.empty();
                }
            }
        }
        System.out.println("Login failed for '" + username + "': Invalid username or password.");
        loggingService.logUserAction(username, "Login failed: Invalid credentials", "Attempted login with username '" + username + "'.");
        return Optional.empty();
    }

    /**
     * 管理员登录验证
     * Authenticates an admin login.
     * @param username The admin username.
     * @param password The plain text password.
     * @return An Optional containing the Admin object if login is successful, empty otherwise.
     */
    public Optional<Admin> loginAdmin(String username, String password) {
        Optional<Admin> adminOptional = dao.getAdminByUsername(username);
        if (adminOptional.isPresent()) {
            Admin admin = adminOptional.get();
            // 验证密码
            if (PasswordHasher.verifyPassword(password, admin.getPasswordHash())) {
                System.out.println("Admin '" + username + "' logged in successfully.");
                loggingService.logAdminAction(username, "Login successful", "Admin '" + username + "' logged in.");
                return Optional.of(admin);
            }
        }
        System.out.println("Login failed for '" + username + "': Invalid admin username or password.");
        loggingService.logAdminAction(username, "Login failed: Invalid credentials", "Attempted admin login with username '" + username + "'.");
        return Optional.empty();
    }
}
