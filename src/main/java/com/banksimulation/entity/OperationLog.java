package com.banksimulation.entity;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 操作日志实体类
 * Represents an operation log entry in the bank simulation system.
 */
public class OperationLog {
    private String logId;           // 日志ID (UUID)
    private LocalDateTime timestamp;    // 时间戳
    private String actorUsername;   // 操作者用户名
    private ActorType actorType;    // 操作者类型
    private String action;          // 操作描述
    private String details;         // 详细信息

    // 构造函数
    public OperationLog(String actorUsername, ActorType actorType, String action, String details) {
        this.logId = UUID.randomUUID().toString(); // 自动生成 UUID
        this.timestamp = LocalDateTime.now();
        this.actorUsername = actorUsername;
        this.actorType = actorType;
        this.action = action;
        this.details = details;
    }

    // 用于从数据存储加载的构造函数
    public OperationLog(String logId, LocalDateTime timestamp, String actorUsername, ActorType actorType, String action, String details) {
        this.logId = logId;
        this.timestamp = timestamp;
        this.actorUsername = actorUsername;
        this.actorType = actorType;
        this.action = action;
        this.details = details;
    }

    // Getters
    public String getLogId() {
        return logId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public ActorType getActorType() {
        return actorType;
    }

    public String getAction() {
        return action;
    }

    public String getDetails() {
        return details;
    }

    // Setters (通常日志记录创建后不修改)
    // For simplicity, no setters are provided as log entries are immutable once recorded.
}
