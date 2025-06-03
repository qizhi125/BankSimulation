package com.banksimulation.dao;

import com.banksimulation.entity.Admin;
import com.banksimulation.entity.OperationLog;
import com.banksimulation.entity.TransactionRecord;
import com.banksimulation.entity.User;

import java.util.List;
import java.util.Optional;

/**
 * 数据访问对象接口
 * Defines the contract for data access operations for different entities.
 */
public interface DataAccessObject {

    // User operations
    void saveUser(User user);
    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByAccountNumber(String accountNumber);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(String userId);

    // Admin operations
    void saveAdmin(Admin admin);
    Optional<Admin> getAdminByUsername(String username);
    List<Admin> getAllAdmins();
    void updateAdmin(Admin admin); // For password changes etc.

    // TransactionRecord operations
    void saveTransaction(TransactionRecord transaction);
    List<TransactionRecord> getTransactionsByUserId(String userId);
    List<TransactionRecord> getTransactionsByAccountNumber(String accountNumber);
    List<TransactionRecord> getAllTransactions();

    // OperationLog operations
    void saveLog(OperationLog log);
    List<OperationLog> getAllLogs();
}
