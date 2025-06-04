package com.banksimulation.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 交易记录实体类
 * Represents a transaction record in the bank simulation system.
 */
public class TransactionRecord {
    private String transactionId;       // 交易ID (UUID)
    private String userId;              // 用户ID (外键)
    private String accountNumber;       // 账号
    private TransactionType type;       // 交易类型 (DEPOSIT, WITHDRAWAL, TRANSFER_IN, TRANSFER_OUT)
    private double amount;              // 金额
    private double balanceAfterTransaction; // 交易后余额
    private LocalDateTime timestamp;    // 时间戳
    private String description;         // 描述
    private String relatedAccountNumber; // 关联账户号码，用于转账交易

    // 构造函数 - 用于存款和取款
    public TransactionRecord(String userId, String accountNumber, TransactionType type,
                             double amount, double balanceAfterTransaction, String description) {
        this(userId, accountNumber, type, amount, balanceAfterTransaction, description, null); // 默认 relatedAccountNumber 为 null
    }

    // 构造函数 - 用于转账 (包含 relatedAccountNumber)
    public TransactionRecord(String userId, String accountNumber, TransactionType type,
                             double amount, double balanceAfterTransaction, String description, String relatedAccountNumber) {
        this.transactionId = UUID.randomUUID().toString(); // 自动生成 UUID
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.timestamp = LocalDateTime.now();
        this.description = description;
        this.relatedAccountNumber = relatedAccountNumber;
    }

    // 用于从数据存储加载的构造函数 (包含所有字段)
    public TransactionRecord(String transactionId, String userId, String accountNumber, TransactionType type,
                             double amount, double balanceAfterTransaction, LocalDateTime timestamp, String description, String relatedAccountNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.accountNumber = accountNumber;
        this.type = type;
        this.amount = amount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.timestamp = timestamp;
        this.description = description;
        this.relatedAccountNumber = relatedAccountNumber;
    }

    // Getters
    public String getTransactionId() {
        return transactionId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public TransactionType getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedAccountNumber() {
        return relatedAccountNumber;
    }

    // Setters (通常交易记录创建后不修改，但如果需要，可以添加)
    // For simplicity, no setters are provided as transactions are immutable once recorded.
}
