package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.Admin; // 导入 Admin 类
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord; // 导入 TransactionRecord
import com.banksimulation.entity.User;
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
    private final LoggingService loggingService; // 依赖日志服务

    public AdminService(DataAccessObject dao, LoggingService loggingService) {
        this.dao = dao;
        this.loggingService = loggingService;
    }

    /**
     * 检查指定用户名是否为顶级管理员。
     * Checks if the given username belongs to a top-level administrator.
     * @param adminUsername The username to check.
     * @return true if the user is a top-level admin, false otherwise.
     */
    public boolean isTopLevelAdmin(String adminUsername) {
        Optional<Admin> adminOptional = dao.getAdminByUsername(adminUsername);
        return adminOptional.map(Admin::isTopLevelAdmin).orElse(false);
    }

    /**
     * 根据用户名获取管理员对象。
     * Retrieves an Admin object by its username.
     * @param username The username of the admin to retrieve.
     * @return An Optional containing the Admin object if found, empty otherwise.
     */
    public Optional<Admin> getAdminByUsername(String username) {
        return dao.getAdminByUsername(username);
    }

    /**
     * 管理员创建新用户。
     * Admin creates a new user.
     * @param actingAdminUsername The username of the admin performing the action.
     * @param newUser The user object to create.
     * @return true if user creation is successful, false otherwise (e.g., username already exists, or permission denied).
     */
    public boolean createUser(String actingAdminUsername, User newUser) {
        if (dao.getUserByUsername(newUser.getUsername()).isPresent()) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to create user: Username '" + newUser.getUsername() + "' already exists.");
            loggingService.logAdminAction(actingAdminUsername, "Create user failed", "Username '" + newUser.getUsername() + "' already exists.");
            return false;
        }

        // 只有顶级管理员可以创建新管理员 (如果未来有创建管理员的功能)
        // 目前只创建普通用户，所以所有管理员都可以创建普通用户
        dao.saveUser(newUser);
        System.out.println("Admin '" + actingAdminUsername + "' created new user: " + newUser.getUsername());
        loggingService.logAdminAction(actingAdminUsername, "User created", "Created user: " + newUser.getUsername());
        return true;
    }

    /**
     * 修改指定用户信息。
     * Modifies information for a specified user.
     * @param actingAdminUsername The username of the admin performing the action.
     * @param targetUsername The username of the user to modify.
     * @param updatedInfo A User object containing the updated information.
     * @return true if modification is successful, false otherwise.
     */
    public boolean modifyUserInfo(String actingAdminUsername, String targetUsername, User updatedInfo) {
        Optional<User> userOptional = dao.getUserByUsername(targetUsername);
        if (userOptional.isEmpty()) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to modify user info: User '" + targetUsername + "' not found.");
            loggingService.logAdminAction(actingAdminUsername, "Modify user info failed", "User '" + targetUsername + "' not found.");
            return false;
        }

        User userToModify = userOptional.get();

        // 非顶级管理员不能修改顶级管理员的信息（这里假设没有管理员修改管理员信息的界面）
        // 如果 targetUsername 是另一个管理员，且 actingAdminUsername 不是顶级管理员，则禁止
        // 这里只处理普通用户，所以只需检查 actingAdminUsername 是否有权限修改普通用户
        // 鉴于目前所有管理员都可以修改普通用户信息，此处无需额外权限检查

        userToModify.setFirstName(updatedInfo.getFirstName());
        userToModify.setLastName(updatedInfo.getLastName());
        userToModify.setActive(updatedInfo.isActive());
        userToModify.setCanDeposit(updatedInfo.canDeposit());
        userToModify.setCanWithdraw(updatedInfo.canWithdraw());

        dao.updateUser(userToModify);
        System.out.println("Admin '" + actingAdminUsername + "' modified info for user: " + targetUsername);
        loggingService.logAdminAction(actingAdminUsername, "Modify user info", "Modified info for user: " + targetUsername);
        return true;
    }

    /**
     * 启用/禁止用户登录。
     * Toggles a user's login status (active/inactive).
     * @param actingAdminUsername The username of the admin performing the action.
     * @param targetUsername The username of the user to toggle.
     * @param isActive The new active status.
     * @return true if status is updated, false otherwise.
     */
    public boolean toggleUserLoginStatus(String actingAdminUsername, String targetUsername, boolean isActive) {
        Optional<User> userOptional = dao.getUserByUsername(targetUsername);
        if (userOptional.isEmpty()) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to toggle login status: User '" + targetUsername + "' not found.");
            loggingService.logAdminAction(actingAdminUsername, "Toggle user login status failed", "User '" + targetUsername + "' not found.");
            return false;
        }

        // 顶级管理员可以修改所有用户状态
        // 非顶级管理员不能修改顶级管理员的状态 (如果 targetUsername 是管理员)
        // 这里只处理普通用户，所以只需检查 actingAdminUsername 是否有权限修改普通用户
        // 鉴于目前所有管理员都可以修改普通用户状态，此处无需额外权限检查

        User userToToggle = userOptional.get();
        userToToggle.setActive(isActive);
        dao.updateUser(userToToggle);
        System.out.println("Admin '" + actingAdminUsername + "' toggled login status for user '" + targetUsername + "' to " + (isActive ? "active" : "inactive") + ".");
        loggingService.logAdminAction(actingAdminUsername, "Toggle user login status", "Set '" + targetUsername + "' to " + (isActive ? "active" : "inactive") + ".");
        return true;
    }

    /**
     * 修改用户操作权限。
     * Modifies a user's operation permissions.
     * @param actingAdminUsername The username of the admin performing the action.
     * @param targetUsername The username of the user to modify.
     * @param newPermissions A map containing new permission settings (e.g., "canDeposit": true).
     * @return true if permissions are updated, false otherwise.
     */
    public boolean modifyUserPermissions(String actingAdminUsername, String targetUsername, Map<String, Boolean> newPermissions) {
        // 只有顶级管理员有授权能力
        if (!isTopLevelAdmin(actingAdminUsername)) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to modify user permissions: Only top-level admins can grant permissions.");
            loggingService.logAdminAction(actingAdminUsername, "Modify user permissions failed", "Attempted to grant permissions without top-level admin rights.");
            return false;
        }

        Optional<User> userOptional = dao.getUserByUsername(targetUsername);
        if (userOptional.isEmpty()) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to modify user permissions: User '" + targetUsername + "' not found.");
            loggingService.logAdminAction(actingAdminUsername, "Modify user permissions failed", "User '" + targetUsername + "' not found.");
            return false;
        }

        User userToModify = userOptional.get();
        if (newPermissions.containsKey("canDeposit")) {
            userToModify.setCanDeposit(newPermissions.get("canDeposit"));
        }
        if (newPermissions.containsKey("canWithdraw")) {
            userToModify.setCanWithdraw(newPermissions.get("canWithdraw"));
        }
        dao.updateUser(userToModify);
        System.out.println("Admin '" + actingAdminUsername + "' modified permissions for user: " + targetUsername);
        loggingService.logAdminAction(actingAdminUsername, "Modify user permissions", "Modified permissions for user: " + targetUsername + " -> " + newPermissions);
        return true;
    }

    /**
     * 删除用户。
     * Deletes a user.
     * @param actingAdminUsername The username of the admin performing the action.
     * @param targetUsername The username of the user to delete.
     * @return true if user is deleted, false otherwise.
     */
    public boolean deleteUser(String actingAdminUsername, String targetUsername) {
        Optional<User> userOptional = dao.getUserByUsername(targetUsername);
        if (userOptional.isEmpty()) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to delete user: User '" + targetUsername + "' not found.");
            loggingService.logAdminAction(actingAdminUsername, "Delete user failed", "User '" + targetUsername + "' not found.");
            return false;
        }

        User userToDelete = userOptional.get();

        // 非顶级管理员不能删除顶级管理员
        Optional<Admin> targetAdminOptional = dao.getAdminByUsername(targetUsername);
        if (targetAdminOptional.isPresent() && targetAdminOptional.get().isTopLevelAdmin() && !isTopLevelAdmin(actingAdminUsername)) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to delete user '" + targetUsername + "': Cannot delete a top-level admin.");
            loggingService.logAdminAction(actingAdminUsername, "Delete user failed", "Attempted to delete top-level admin '" + targetUsername + "'.");
            return false;
        }
        // 不能删除自己（防止误操作，或者如果UI允许删除自己，则需要特殊处理）
        if (actingAdminUsername.equals(targetUsername)) {
            System.out.println("Admin '" + actingAdminUsername + "' failed to delete user: Cannot delete self.");
            loggingService.logAdminAction(actingAdminUsername, "Delete user failed", "Attempted to delete self.");
            return false;
        }

        dao.deleteUser(userToDelete.getUserId()); // DAO层根据userId删除
        System.out.println("Admin '" + actingAdminUsername + "' deleted user: " + targetUsername);
        loggingService.logAdminAction(actingAdminUsername, "User deleted", "Deleted user: " + targetUsername);
        return true;
    }

    /**
     * 获取所有用户信息列表。
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
     * 获取所有交易记录。
     * Retrieves all transaction records in the system.
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
