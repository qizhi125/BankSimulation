package com.banksimulation.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 管理员实体类
 * Represents an administrator in the bank simulation system.
 */
public class Admin {
    private String adminId;       // 管理员ID (UUID)
    private String username;      // 管理员用户名
    private String passwordHash;  // 加密后的密码
    private boolean isTopLevelAdmin; // 是否为顶级管理员
    private LocalDateTime createdAt; // 创建时间
    private LocalDateTime updatedAt; // 最后更新时间

    // 构造函数
    public Admin(String username, String passwordHash, boolean isTopLevelAdmin) {
        this.adminId = UUID.randomUUID().toString(); // 自动生成 UUID
        this.username = username;
        this.passwordHash = passwordHash;
        this.isTopLevelAdmin = isTopLevelAdmin; // 设置是否为顶级管理员
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // 用于从数据存储加载的构造函数
    public Admin(String adminId, String username, String passwordHash, boolean isTopLevelAdmin, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.adminId = adminId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.isTopLevelAdmin = isTopLevelAdmin;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public String getAdminId() {
        return adminId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isTopLevelAdmin() { // 新增 getter
        return isTopLevelAdmin;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    // Setters
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        this.updatedAt = LocalDateTime.now(); // 更新时间戳
    }

    public void setTopLevelAdmin(boolean topLevelAdmin) { // 新增 setter
        isTopLevelAdmin = topLevelAdmin;
        this.updatedAt = LocalDateTime.now();
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
