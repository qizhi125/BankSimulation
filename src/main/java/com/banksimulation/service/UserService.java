package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.TransactionType;
import com.banksimulation.entity.User;
import com.banksimulation.util.PasswordHasher; // 稍后创建这个工具类

import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 * Handles user-specific business logic like deposit, withdrawal, balance check.
 */
public class UserService {

    private final DataAccessObject dao;
    private final LoggingService loggingService; // 依赖日志服务

    public UserService(DataAccessObject dao, LoggingService loggingService) {
        this.dao = dao;
        this.loggingService = loggingService;
    }

    /**
     * 处理用户存款
     * Processes a deposit for a user.
     * @param username The username of the depositor.
     * @param amount The amount to deposit.
     * @return true if deposit is successful, false otherwise.
     */
    public boolean deposit(String username, double amount) {
        if (amount <= 0) {
            System.out.println("Deposit failed for '" + username + "': Amount must be positive.");
            loggingService.logUserAction(username, "Deposit failed", "Attempted deposit of " + amount + " (invalid amount).");
            return false;
        }

        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isActive()) {
                System.out.println("Deposit failed for '" + username + "': Account is inactive.");
                loggingService.logUserAction(username, "Deposit failed", "Account inactive.");
                return false;
            }
            if (!user.canDeposit()) {
                System.out.println("Deposit failed for '" + username + "': User does not have deposit permission.");
                loggingService.logUserAction(username, "Deposit failed", "No deposit permission.");
                return false;
            }

            double newBalance = user.getBalance() + amount;
            user.setBalance(newBalance);
            dao.updateUser(user);

            // 记录交易
            TransactionRecord transaction = new TransactionRecord(
                    user.getUserId(), user.getAccountNumber(), TransactionType.DEPOSIT,
                    amount, newBalance, "User deposit"
            );
            dao.saveTransaction(transaction);

            System.out.println("User '" + username + "' deposited " + amount + ". New balance: " + newBalance);
            loggingService.logUserAction(username, "Deposit successful", "Deposited " + amount + ". New balance: " + newBalance);
            return true;
        }
        System.out.println("Deposit failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "Deposit failed", "User not found.");
        return false;
    }

    /**
     * 处理用户取款
     * Processes a withdrawal for a user.
     * @param username The username of the withdrawer.
     * @param amount The amount to withdraw.
     * @return true if withdrawal is successful, false otherwise.
     */
    public boolean withdraw(String username, double amount) {
        if (amount <= 0) {
            System.out.println("Withdrawal failed for '" + username + "': Amount must be positive.");
            loggingService.logUserAction(username, "Withdrawal failed", "Attempted withdrawal of " + amount + " (invalid amount).");
            return false;
        }

        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (!user.isActive()) {
                System.out.println("Withdrawal failed for '" + username + "': Account is inactive.");
                loggingService.logUserAction(username, "Withdrawal failed", "Account inactive.");
                return false;
            }
            if (!user.canWithdraw()) {
                System.out.println("Withdrawal failed for '" + username + "': User does not have withdrawal permission.");
                loggingService.logUserAction(username, "Withdrawal failed", "No withdrawal permission.");
                return false;
            }
            if (user.getBalance() < amount) {
                System.out.println("Withdrawal failed for '" + username + "': Insufficient balance. Current: " + user.getBalance() + ", Requested: " + amount);
                loggingService.logUserAction(username, "Withdrawal failed", "Insufficient balance. Current: " + user.getBalance() + ", Requested: " + amount);
                return false;
            }

            double newBalance = user.getBalance() - amount;
            user.setBalance(newBalance);
            dao.updateUser(user);

            // 记录交易
            TransactionRecord transaction = new TransactionRecord(
                    user.getUserId(), user.getAccountNumber(), TransactionType.WITHDRAWAL,
                    amount, newBalance, "User withdrawal"
            );
            dao.saveTransaction(transaction);

            System.out.println("User '" + username + "' withdrew " + amount + ". New balance: " + newBalance);
            loggingService.logUserAction(username, "Withdrawal successful", "Withdrew " + amount + ". New balance: " + newBalance);
            return true;
        }
        System.out.println("Withdrawal failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "Withdrawal failed", "User not found.");
        return false;
    }

    /**
     * 查询用户余额
     * Checks the balance of a user's account.
     * @param username The username.
     * @return An Optional containing the balance if user is found, empty otherwise.
     */
    public Optional<Double> checkBalance(String username) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            System.out.println("User '" + username + "' balance: " + user.getBalance());
            loggingService.logUserAction(username, "Check balance", "Current balance: " + user.getBalance());
            return Optional.of(user.getBalance());
        }
        System.out.println("Check balance failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "Check balance failed", "User not found.");
        return Optional.empty();
    }

    /**
     * 修改用户密码
     * Updates a user's password.
     * @param username The username.
     * @param oldPassword The current plain text password.
     * @param newPassword The new plain text password.
     * @return true if password update is successful, false otherwise.
     */
    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (PasswordHasher.verifyPassword(oldPassword, user.getPasswordHash())) {
                user.setPasswordHash(PasswordHasher.hashPassword(newPassword)); // 哈希新密码
                dao.updateUser(user);
                System.out.println("User '" + username + "' password updated successfully.");
                loggingService.logUserAction(username, "Password updated", "User '" + username + "' changed password.");
                return true;
            } else {
                System.out.println("Password update failed for '" + username + "': Old password mismatch.");
                loggingService.logUserAction(username, "Password update failed", "Old password mismatch.");
                return false;
            }
        }
        System.out.println("Password update failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "Password update failed", "User not found.");
        return false;
    }

    /**
     * 获取用户详细信息
     * Retrieves a user's detailed information.
     * @param username The username.
     * @return An Optional containing the User object if found, empty otherwise.
     */
    public Optional<User> getUserDetails(String username) {
        Optional<User> userOptional = dao.getUserByUsername(username);
        if (userOptional.isPresent()) {
            loggingService.logUserAction(username, "View personal info", "User '" + username + "' viewed their details.");
            return userOptional;
        }
        System.out.println("Get user details failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "View personal info failed", "User '" + username + "' not found.");
        return Optional.empty();
    }
}
