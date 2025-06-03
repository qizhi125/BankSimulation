package com.banksimulation.dao;

import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.User;
import com.banksimulation.util.PasswordHasher;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 内存数据访问对象实现
 * In-memory implementation of DataAccessObject, suitable for prototyping.
 * Data is lost when the application closes.
 */
public class InMemoryDAO implements DataAccessObject {

    // 使用 ConcurrentHashMap 保证多线程安全，尽管在这个单机模拟中可能不是严格必需，但良好的实践。
    private final Map<String, User> users = new ConcurrentHashMap<>(); // Key: userId
    private final Map<String, Admin> admins = new ConcurrentHashMap<>(); // Key: adminId
    private final Map<String, TransactionRecord> transactions = new ConcurrentHashMap<>(); // Key: transactionId
    private final Map<String, OperationLog> logs = new ConcurrentHashMap<>(); // Key: logId

    // 为了方便测试，可以预设一些数据
    public InMemoryDAO() {
        // 预设一个管理员账户
        String adminPasswordPlain = "admin123"; // 默认管理员明文密码
        String adminPasswordHashed = PasswordHasher.hashPassword(adminPasswordPlain);
        Admin defaultAdmin = new Admin("admin", adminPasswordHashed);
        admins.put(defaultAdmin.getAdminId(), defaultAdmin);
        System.out.println("InMemoryDAO: Default admin created: " + defaultAdmin.getUsername() + " with password: " + adminPasswordPlain);

        // 预设一个普通用户账户
        String userPasswordPlain = "password123"; // 默认用户明文密码
        String userPasswordHashed = PasswordHasher.hashPassword(userPasswordPlain);
        User defaultUser = new User("user1", userPasswordHashed, "John", "Doe", "1000010001");
        users.put(defaultUser.getUserId(), defaultUser);
        System.out.println("InMemoryDAO: Default user created: " + defaultUser.getUsername() + " with password: " + userPasswordPlain);
    }

    // --- User operations ---
    @Override
    public void saveUser(User user) {
        users.put(user.getUserId(), user);
        System.out.println("User saved: " + user.getUsername());
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return users.values().stream()
                .filter(user -> user.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<User> getUserByAccountNumber(String accountNumber) {
        return users.values().stream()
                .filter(user -> user.getAccountNumber().equals(accountNumber))
                .findFirst();
    }

    @Override
    public Optional<User> getUserByUserId(String userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values()); // 返回副本以防止外部修改
    }

    @Override
    public void updateUser(User user) {
        // 假设用户已存在，直接覆盖
        users.put(user.getUserId(), user);
        System.out.println("User updated: " + user.getUsername());
    }

    @Override
    public void deleteUser(String userId) {
        users.remove(userId);
        // 同时删除相关交易记录和日志（根据业务需求，这里简单处理）
        transactions.values().removeIf(t -> t.getUserId().equals(userId));
        logs.values().removeIf(l -> {
            // 尝试获取被删除用户的用户名，如果用户已不存在，则安全处理
            Optional<User> deletedUserOptional = users.values().stream()
                    .filter(u -> u.getUserId().equals(userId))
                    .findFirst();
            return deletedUserOptional.isPresent() && l.getActorUsername().equals(deletedUserOptional.get().getUsername());
        });
        System.out.println("User deleted: " + userId);
    }

    // --- Admin operations ---
    @Override
    public void saveAdmin(Admin admin) {
        admins.put(admin.getAdminId(), admin);
        System.out.println("Admin saved: " + admin.getUsername());
    }

    @Override
    public Optional<Admin> getAdminByUsername(String username) {
        return admins.values().stream()
                .filter(admin -> admin.getUsername().equals(username))
                .findFirst();
    }

    @Override
    public Optional<Admin> getAdminByAdminId(String adminId) {
        return Optional.ofNullable(admins.get(adminId));
    }

    @Override
    public List<Admin> getAllAdmins() {
        return new ArrayList<>(admins.values());
    }

    @Override
    public void updateAdmin(Admin admin) {
        admins.put(admin.getAdminId(), admin);
        System.out.println("Admin updated: " + admin.getUsername());
    }

    // --- TransactionRecord operations ---
    @Override
    public void saveTransaction(TransactionRecord transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
        System.out.println("Transaction saved: " + transaction.getTransactionId());
    }

    @Override
    public List<TransactionRecord> getTransactionsByUserId(String userId) {
        return transactions.values().stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionRecord> getTransactionsByAccountNumber(String accountNumber) {
        return transactions.values().stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionRecord> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    // --- OperationLog operations ---
    @Override
    public void saveLog(OperationLog log) {
        logs.put(log.getLogId(), log);
        System.out.println("Log saved: " + log.getAction());
    }

    @Override
    public List<OperationLog> getAllLogs() {
        return new ArrayList<>(logs.values());
    }
}
