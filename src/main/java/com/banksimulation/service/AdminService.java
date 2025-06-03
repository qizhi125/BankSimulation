package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.User;
import com.banksimulation.entity.Admin; // 导入 Admin 类
import com.banksimulation.entity.TransactionRecord; // 导入 TransactionRecord 类
import com.banksimulation.util.PasswordHasher;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 管理员服务
 * Handles administrator-specific business logic like user management.
 */
public class AdminService {

    private final DataAccessObject dao;
    private final LoggingService loggingService;

    public AdminService(DataAccessObject dao, LoggingService loggingService) {
        this.dao = dao;
        this.loggingService = loggingService;
    }

    /**
     * 管理员创建新用户
     * Admin creates a new user.
     * @param adminUsername The username of the admin performing the action.
     * @param user The user object to create.
     * @return true if user creation is successful, false otherwise (e.g., username already exists).
     */
    public boolean createUser(String adminUsername, User user) {
        if (dao.getUserByUsername(user.getUsername()).isPresent()) {
            System.out.println("Admin '" + adminUsername + "' failed to create user: Username '" + user.getUsername() + "' already exists.");
            loggingService.logAdminAction(adminUsername, "Create user failed", "Username '" + user.getUsername() + "' already exists.");
            return false;
        }
        if (dao.getUserByAccountNumber(user.getAccountNumber()).isPresent()) {
            System.out.println("Admin '" + adminUsername + "' failed to create user: Account number '" + user.getAccountNumber() + "' already exists.");
            loggingService.logAdminAction(adminUsername, "Create user failed", "Account number '" + user.getAccountNumber() + "' already exists.");
            return false;
        }

        dao.saveUser(user);
        System.out.println("Admin '" + adminUsername + "' created new user: " + user.getUsername());
        loggingService.logAdminAction(adminUsername, "User created", "Created user: " + user.getUsername());
        return true;
    }

    /**
     * 修改指定用户信息
     * Modifies information for a specified user.
     * @param adminUsername The username of the admin performing the action.
     * @param targetUsername The username of the user to modify.
     * @param updatedInfo A User object containing the updated information.
     * @return true if modification is successful, false otherwise.
     */
    public boolean modifyUserInfo(String adminUsername, String targetUsername, User updatedInfo) {
        Optional<User> userOptional = dao.getUserByUsername(targetUsername);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // 更新可修改的字段
            user.setFirstName(updatedInfo.getFirstName());
            user.setLastName(updatedInfo.getLastName());
            user.setActive(updatedInfo.isActive());
            user.setCanDeposit(updatedInfo.canDeposit());
            user.setCanWithdraw(updatedInfo.canWithdraw());
            // 注意：密码修改应通过专门的updatePassword方法或重置功能
            // user.setPasswordHash(updatedInfo.getPasswordHash()); // 不直接在这里修改密码哈希

            dao.updateUser(user);
            System.out.println("Admin '" + adminUsername + "' modified info for user: " + targetUsername);
            loggingService.logAdminAction(adminUsername, "Modify user info", "Modified info for user: " + targetUsername);
            return true;
        }
        System.out.println("Admin '" + adminUsername + "' failed to modify user info: User '" + targetUsername + "' not found.");
        loggingService.logAdminAction(adminUsername, "Modify user info failed", "User '" + targetUsername + "' not found.");
        return false;
    }

    /**
     * 启用/禁止用户登录
     * Toggles a user's login status (active/inactive).
     * @param adminUsername The username of the admin performing the action.
     * @param username The username of the user to toggle.
     * @param isActive The new active status.
     * @return true if status is updated, false otherwise.
     */
    public boolean toggleUserLoginStatus(String adminUsername, String username, boolean isActive) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setActive(isActive);
            dao.updateUser(user);
            System.out.println("Admin '" + adminUsername + "' toggled login status for user '" + username + "' to " + (isActive ? "active" : "inactive") + ".");
            loggingService.logAdminAction(adminUsername, "Toggle user login status", "Set '" + username + "' to " + (isActive ? "active" : "inactive") + ".");
            return true;
        }
        System.out.println("Admin '" + adminUsername + "' failed to toggle login status: User '" + username + "' not found.");
        loggingService.logAdminAction(adminUsername, "Toggle user login status failed", "User '" + username + "' not found.");
        return false;
    }

    /**
     * 修改用户操作权限
     * Modifies a user's operation permissions.
     * @param adminUsername The username of the admin performing the action.
     * @param username The username of the user to modify.
     * @param newPermissions A map containing new permission settings (e.g., "canDeposit": true).
     * @return true if permissions are updated, false otherwise.
     */
    public boolean modifyUserPermissions(String adminUsername, String username, Map<String, Boolean> newPermissions) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (newPermissions.containsKey("canDeposit")) {
                user.setCanDeposit(newPermissions.get("canDeposit"));
            }
            if (newPermissions.containsKey("canWithdraw")) {
                user.setCanWithdraw(newPermissions.get("canWithdraw"));
            }
            dao.updateUser(user);
            System.out.println("Admin '" + adminUsername + "' modified permissions for user: " + username);
            loggingService.logAdminAction(adminUsername, "Modify user permissions", "Modified permissions for user: " + username + " -> " + newPermissions);
            return true;
        }
        System.out.println("Admin '" + adminUsername + "' failed to modify user permissions: User '" + username + "' not found.");
        loggingService.logAdminAction(adminUsername, "Modify user permissions failed", "User '" + username + "' not found.");
        return false;
    }

    /**
     * 获取所有用户信息列表
     * Retrieves a list of all users.
     * @param adminUsername The username of the admin performing the action.
     * @return A list of all User objects.
     */
    public List<User> getAllUsers(String adminUsername) {
        List<User> users = dao.getAllUsers();
        System.out.println("Admin '" + adminUsername + "' retrieved all users list.");
        loggingService.logAdminAction(adminUsername, "Get all users", "Retrieved " + users.size() + " users.");
        return users;
    }

    /**
     * 获取所有交易记录
     * Retrieves all transaction records.
     * @param adminUsername The username of the admin performing the action.
     * @return A list of all TransactionRecord objects.
     */
    public List<TransactionRecord> getAllTransactions(String adminUsername) {
        List<TransactionRecord> transactions = dao.getAllTransactions();
        System.out.println("Admin '" + adminUsername + "' retrieved all transaction records.");
        loggingService.logAdminAction(adminUsername, "Get all transactions", "Retrieved " + transactions.size() + " transactions.");
        return transactions;
    }
}
