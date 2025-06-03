package com.banksimulation.dao;

import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.User;
import com.banksimulation.util.PasswordHasher; // 引入密码哈希工具类

import java.util.ArrayList;
import java.util.HashMap;
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
    private final List<TransactionRecord> transactions = new ArrayList<>(); // 简单列表
    private final List<OperationLog> logs = new ArrayList<>(); // 简单列表

    // 为了方便测试，可以预设一些数据
    public InMemoryDAO() {
        // 预设一个管理员账户
        // 将明文密码 "admin" 进行哈希，然后存储哈希值
        String adminPasswordPlain = "admin"; // 默认管理员明文密码
        String adminPasswordHashed = PasswordHasher.hashPassword(adminPasswordPlain);
        Admin defaultAdmin = new Admin("admin", adminPasswordHashed);
        admins.put(defaultAdmin.getAdminId(), defaultAdmin);
        System.out.println("InMemoryDAO: Default admin created: " + defaultAdmin.getUsername() + " with password: " + adminPasswordPlain);

        // 预设一个普通用户账户
        // 将明文密码 "userpass" 进行哈希，然后存储哈希值
        String userPasswordPlain = "userpass"; // 默认用户明文密码
        String userPasswordHashed = PasswordHasher.hashPassword(userPasswordPlain);
        User defaultUser = new User("user1", userPasswordHashed, "John", "Doe", "100001");
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
        transactions.removeIf(t -> t.getUserId().equals(userId));
        // 注意：这里假设日志的actorUsername与User的username相同，如果user被删除，其日志也删除
        // 更严谨的做法是先获取被删除用户的username，再移除日志
        // 或者在OperationLog中存储userId作为外键
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
        transactions.add(transaction);
        System.out.println("Transaction saved: " + transaction.getTransactionId());
    }

    @Override
    public List<TransactionRecord> getTransactionsByUserId(String userId) {
        return transactions.stream()
                .filter(t -> t.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionRecord> getTransactionsByAccountNumber(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionRecord> getAllTransactions() {
        return new ArrayList<>(transactions);
    }

    // --- OperationLog operations ---
    @Override
    public void saveLog(OperationLog log) {
        logs.add(log);
        System.out.println("Log saved: " + log.getAction());
    }

    @Override
    public List<OperationLog> getAllLogs() {
        return new ArrayList<>(logs);
    }
}
