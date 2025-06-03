package com.banksimulation.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 用户实体类
 * Represents a regular user in the bank simulation system.
 */
public class User {
    private String userId;        // 用户ID (UUID)
    private String username;      // 用户名
    private String passwordHash;  // 加密后的密码
    private String firstName;     // 名
    private String lastName;      // 姓
    private String accountNumber; // 银行账号
    private double balance;       // 账户余额
    private boolean isActive;     // 账户是否激活/允许登录
    private boolean canDeposit;   // 是否允许存款权限
    private boolean canWithdraw;  // 是否允许取款权限
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 最后更新时间

    // 构造函数
    public User(String username, String passwordHash, String firstName, String lastName, String accountNumber) {
        this.userId = UUID.randomUUID().toString(); // 自动生成 UUID
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountNumber = accountNumber;
        this.balance = 0.00; // 初始余额为0
        this.isActive = true; // 默认激活
        this.canDeposit = true; // 默认允许存款
        this.canWithdraw = true; // 默认允许取款
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 用于从数据存储加载的构造函数（包含所有字段）
    public User(String userId, String username, String passwordHash, String firstName, String lastName,
                String accountNumber, double balance, boolean isActive, boolean canDeposit,
                boolean canWithdraw, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.accountNumber = accountNumber;
        this.balance = balance;
        this.isActive = isActive;
        this.canDeposit = canDeposit;
        this.canWithdraw = canWithdraw;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isActive() {
        return isActive;
    }

    public boolean canDeposit() {
        return canDeposit;
    }

    public boolean canWithdraw() {
        return canWithdraw;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters (根据需要提供，通常只提供可修改的属性的setter)
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setBalance(double balance) {
        this.balance = balance;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setActive(boolean active) {
        isActive = active;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setCanDeposit(boolean canDeposit) {
        this.canDeposit = canDeposit;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setCanWithdraw(boolean canWithdraw) {
        this.canWithdraw = canWithdraw;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }

    // 注意：userId, username, accountNumber, createdAt 通常不提供setter，因为它们是唯一标识或创建时生成
    // 但是，如果需要从数据库加载时设置，可以在第二个构造函数中包含。
    // For simplicity, we'll keep it immutable after creation for these fields.
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
