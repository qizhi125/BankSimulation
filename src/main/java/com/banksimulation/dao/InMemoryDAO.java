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
    private final Map<String, User> usersByAccountNumber = new ConcurrentHashMap<>(); // Key: accountNumber
    private final Map<String, Admin> admins = new ConcurrentHashMap<>(); // Key: adminId
    private final List<TransactionRecord> transactions = new ArrayList<>(); // 简单列表
    private final List<OperationLog> logs = new ArrayList<>(); // 简单列表

    // 为了方便测试，可以预设一些数据
    public InMemoryDAO() {
        // 预设一个顶级管理员账户
        String adminPasswordPlain = "admin"; // 默认管理员明文密码
        String adminPasswordHashed = PasswordHasher.hashPassword(adminPasswordPlain);
        Admin defaultAdmin = new Admin("admin", adminPasswordHashed, true); // 设为顶级管理员
        admins.put(defaultAdmin.getAdminId(), defaultAdmin);
        System.out.println("InMemoryDAO: Default top-level admin created: " + defaultAdmin.getUsername() + " with password: " + adminPasswordPlain);

        // 预设一个普通用户账户
        String userPasswordPlain = "userpass"; // 默认用户明文密码
        String userPasswordHashed = PasswordHasher.hashPassword(userPasswordPlain);
        User defaultUser = new User("user1", userPasswordHashed, "John", "Doe", "100001");
        users.put(defaultUser.getUserId(), defaultUser);
        usersByAccountNumber.put(defaultUser.getAccountNumber(), defaultUser);
        System.out.println("InMemoryDAO: Default user created: " + defaultUser.getUsername() + " with password: " + userPasswordPlain);

        // 预设一个非顶级管理员账户 (用于测试权限限制)
        String subAdminPasswordPlain = "subadmin";
        String subAdminPasswordHashed = PasswordHasher.hashPassword(subAdminPasswordPlain);
        Admin subAdmin = new Admin("subadmin", subAdminPasswordHashed, false); // 非顶级管理员
        admins.put(subAdmin.getAdminId(), subAdmin);
        System.out.println("InMemoryDAO: Sub-admin created: " + subAdmin.getUsername() + " with password: " + subAdminPasswordPlain);

        // 预设另一个普通用户账户用于转账测试
        String user2PasswordPlain = "user2pass";
        String user2PasswordHashed = PasswordHasher.hashPassword(user2PasswordPlain);
        User user2 = new User("user2", user2PasswordHashed, "Jane", "Smith", "100002");
        user2.setBalance(500.0); // 给user2一些初始余额
        users.put(user2.getUserId(), user2);
        usersByAccountNumber.put(user2.getAccountNumber(), user2);
        System.out.println("InMemoryDAO: Default user2 created: " + user2.getUsername() + " with password: " + user2PasswordPlain + ", balance: " + user2.getBalance());
    }

    // --- User operations ---
    @Override
    public void saveUser(User user) {
        users.put(user.getUserId(), user);
        usersByAccountNumber.put(user.getAccountNumber(), user); // 维护按账号查找的Map
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
        return Optional.ofNullable(usersByAccountNumber.get(accountNumber));
    }

    @Override
    public Optional<User> getUserByUserId(String userId) { // 实现新增的方法
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
        usersByAccountNumber.put(user.getAccountNumber(), user); // 更新按账号查找的Map
        System.out.println("User updated: " + user.getUsername());
    }

    @Override
    public void deleteUser(String userId) {
        // 获取被删除用户的用户名和账号，以便在日志中正确记录并从usersByAccountNumber中移除
        Optional<User> userToDeleteOptional = users.values().stream()
                .filter(user -> user.getUserId().equals(userId))
                .findFirst();
        if (userToDeleteOptional.isPresent()) {
            User userToDelete = userToDeleteOptional.get();
            String username = userToDelete.getUsername();
            String accountNumber = userToDelete.getAccountNumber();

            users.remove(userId);
            usersByAccountNumber.remove(accountNumber); // 从按账号查找的Map中移除

            // 同时删除相关交易记录和日志
            transactions.removeIf(t -> t.getUserId().equals(userId));
            logs.removeIf(l -> l.getActorUsername().equals(username) && l.getActorType() == com.banksimulation.entity.ActorType.USER);
            System.out.println("User deleted: " + username + " (ID: " + userId + ")");
        } else {
            System.out.println("Attempted to delete non-existent user with ID: " + userId);
        }
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
