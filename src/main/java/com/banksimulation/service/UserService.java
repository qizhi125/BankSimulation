package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.TransactionType;
import com.banksimulation.entity.User;
import com.banksimulation.util.PasswordHasher;

import java.util.List;
import java.util.Optional;

/**
 * 用户服务
 * Handles user-specific business logic like deposit, withdrawal, balance check, and transfer.
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
            User user = userOptional.get(); // 获取User对象
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
            User user = userOptional.get(); // 获取User对象
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
     * 处理用户转账
     * Processes a transfer from one user's account to another.
     * This method attempts to simulate an atomic transaction.
     * @param senderUsername The username of the sender.
     * @param receiverAccountNumber The account number of the receiver.
     * @param amount The amount to transfer.
     * @return true if transfer is successful, false otherwise.
     */
    public boolean transfer(String senderUsername, String receiverAccountNumber, double amount) {
        if (amount <= 0) {
            System.out.println("Transfer failed for '" + senderUsername + "': Amount must be positive.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Attempted transfer of " + amount + " (invalid amount).");
            return false;
        }

        // 获取发送方和接收方用户对象
        Optional<User> senderOptional = dao.getUserByUsername(senderUsername);
        Optional<User> receiverOptional = dao.getUserByAccountNumber(receiverAccountNumber);

        if (senderOptional.isEmpty()) {
            System.out.println("Transfer failed: Sender '" + senderUsername + "' not found.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Sender not found.");
            return false;
        }
        if (receiverOptional.isEmpty()) {
            System.out.println("Transfer failed for '" + senderUsername + "': Receiver account '" + receiverAccountNumber + "' not found.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Receiver account '" + receiverAccountNumber + "' not found.");
            return false;
        }

        User sender = senderOptional.get();
        User receiver = receiverOptional.get();

        // 不允许自己转账给自己
        if (sender.getAccountNumber().equals(receiver.getAccountNumber())) {
            System.out.println("Transfer failed for '" + senderUsername + "': Cannot transfer to own account.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Attempted to transfer to self.");
            return false;
        }

        // 检查发送方和接收方账户状态及权限
        if (!sender.isActive()) {
            System.out.println("Transfer failed for '" + senderUsername + "': Sender account is inactive.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Sender account inactive.");
            return false;
        }
        if (!sender.canWithdraw()) {
            System.out.println("Transfer failed for '" + senderUsername + "': Sender does not have withdrawal permission.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Sender no withdrawal permission.");
            return false;
        }
        if (sender.getBalance() < amount) {
            System.out.println("Transfer failed for '" + senderUsername + "': Insufficient balance. Current: " + sender.getBalance() + ", Requested: " + amount);
            loggingService.logUserAction(senderUsername, "Transfer failed", "Insufficient balance. Current: " + sender.getBalance() + ", Requested: " + amount);
            return false;
        }

        if (!receiver.isActive()) {
            System.out.println("Transfer failed for '" + senderUsername + "': Receiver account '" + receiverAccountNumber + "' is inactive.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Receiver account inactive.");
            return false;
        }
        if (!receiver.canDeposit()) {
            System.out.println("Transfer failed for '" + senderUsername + "': Receiver does not have deposit permission.");
            loggingService.logUserAction(senderUsername, "Transfer failed", "Receiver no deposit permission.");
            return false;
        }

        // 模拟事务：在内存DAO中，通过同步块确保操作的原子性
        // 在实际数据库中，会使用数据库事务
        synchronized (this) { // 简单地同步整个服务实例，确保并发安全
            // 重新获取最新余额，防止在检查后到更新前有其他操作影响余额
            Optional<User> currentSenderOptional = dao.getUserByUsername(senderUsername);
            Optional<User> currentReceiverOptional = dao.getUserByAccountNumber(receiverAccountNumber);

            if (currentSenderOptional.isEmpty() || currentReceiverOptional.isEmpty()) {
                System.out.println("Transfer failed: Sender or receiver disappeared during transaction.");
                loggingService.logUserAction(senderUsername, "Transfer failed", "Sender or receiver disappeared during transaction.");
                return false;
            }

            User currentSender = currentSenderOptional.get();
            User currentReceiver = currentReceiverOptional.get();

            if (currentSender.getBalance() < amount) { // 再次检查余额
                System.out.println("Transfer failed for '" + senderUsername + "': Insufficient balance after re-check.");
                loggingService.logUserAction(senderUsername, "Transfer failed", "Insufficient balance after re-check.");
                return false;
            }

            // 执行扣款
            double senderNewBalance = currentSender.getBalance() - amount;
            currentSender.setBalance(senderNewBalance);
            dao.updateUser(currentSender);

            // 执行收款
            double receiverNewBalance = currentReceiver.getBalance() + amount;
            currentReceiver.setBalance(receiverNewBalance);
            dao.updateUser(currentReceiver);

            // 记录发送方交易
            TransactionRecord senderTransaction = new TransactionRecord(
                    sender.getUserId(), sender.getAccountNumber(), TransactionType.TRANSFER_OUT,
                    amount, senderNewBalance, "Transfer to " + receiver.getAccountNumber(), receiver.getAccountNumber()
            );
            dao.saveTransaction(senderTransaction);

            // 记录接收方交易
            TransactionRecord receiverTransaction = new TransactionRecord(
                    receiver.getUserId(), receiver.getAccountNumber(), TransactionType.TRANSFER_IN,
                    amount, receiverNewBalance, "Transfer from " + sender.getAccountNumber(), sender.getAccountNumber()
            );
            dao.saveTransaction(receiverTransaction);

            System.out.println("Transfer successful from '" + senderUsername + "' to '" + receiverAccountNumber + "' of " + amount + ".");
            loggingService.logUserAction(senderUsername, "Transfer successful", "Transferred " + amount + " to " + receiver.getAccountNumber());
            loggingService.logUserAction(receiver.getUsername(), "Received transfer", "Received " + amount + " from " + sender.getAccountNumber());
            return true;
        }
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
            User user = userOptional.get(); // 获取User对象
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
            User user = userOptional.get(); // 获取User对象
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
            User user = userOptional.get(); // 获取User对象
            loggingService.logUserAction(username, "View personal info", "User '" + username + "' viewed their details.");
            return Optional.of(user); // 返回User对象而不是Optional<User>的Optional
        }
        System.out.println("Get user details failed: User '" + username + "' not found.");
        loggingService.logUserAction(username, "View personal info failed", "User '" + username + "' not found.");
        return Optional.empty();
    }
}
