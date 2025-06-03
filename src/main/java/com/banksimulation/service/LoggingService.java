package com.banksimulation.service;

import com.banksimulation.dao.DataAccessObject;
import com.banksimulation.entity.ActorType;
import com.banksimulation.entity.OperationLog;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 日志服务
 * Responsible for recording and retrieving operation logs.
 */
public class LoggingService {

    private final DataAccessObject dao;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoggingService(DataAccessObject dao) {
        this.dao = dao;
    }

    /**
     * 获取底层的DataAccessObject实例。
     * 警告：直接访问DAO可能会打破服务层的封装，请谨慎使用。
     * Retrieves the underlying DataAccessObject instance.
     * WARNING: Direct access to DAO might break service layer encapsulation, use with caution.
     * @return The DataAccessObject instance.
     */
    public DataAccessObject getDao() {
        return dao;
    }

    /**
     * 记录普通用户操作日志
     * Logs an action performed by a regular user.
     * @param username The username of the user.
     * @param action A brief description of the action.
     * @param details Detailed information about the action.
     */
    public void logUserAction(String username, String action, String details) {
        OperationLog log = new OperationLog(username, ActorType.USER, action, details);
        dao.saveLog(log);
    }

    /**
     * 记录管理员操作日志
     * Logs an action performed by an administrator.
     * @param adminUsername The username of the admin.
     * @param action A brief description of the action.
     * @param details Detailed information about the action.
     */
    public void logAdminAction(String adminUsername, String action, String details) {
        OperationLog log = new OperationLog(adminUsername, ActorType.ADMIN, action, details);
        dao.saveLog(log);
    }

    /**
     * 记录系统操作日志 (例如注册失败，系统初始化等)
     * Logs an action performed by the system.
     * @param action A brief description of the action.
     * @param details Detailed information about the action.
     */
    public void logSystemAction(String action, String details) {
        OperationLog log = new OperationLog("SYSTEM", ActorType.SYSTEM, action, details);
        dao.saveLog(log);
    }

    /**
     * 记录系统操作日志 (简化版，无详细信息)
     * Logs an action performed by the system (simplified version).
     * @param action A brief description of the action.
     */
    public void logSystemAction(String action) {
        logSystemAction(action, "");
    }


    /**
     * 获取所有日志
     * Retrieves all operation logs.
     * @return A list of all OperationLog objects.
     */
    public List<OperationLog> getLogs() {
        return dao.getAllLogs();
    }

    /**
     * 将日志导出到文件
     * Exports logs to a file in a specified format.
     * @param filePath The path to the output file.
     * @param format The format of the export (e.g., "CSV", "TXT").
     * @return true if export is successful, false otherwise.
     */
    public boolean exportLogs(String filePath, String format) {
        List<OperationLog> logs = dao.getAllLogs();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            if ("CSV".equalsIgnoreCase(format)) {
                writer.println("Log ID,Timestamp,Actor Username,Actor Type,Action,Details"); // CSV header
                for (OperationLog log : logs) {
                    writer.printf("%s,%s,%s,%s,\"%s\",\"%s\"%n",
                            log.getLogId(),
                            log.getTimestamp().format(FORMATTER),
                            log.getActorUsername(),
                            log.getActorType(),
                            log.getAction().replace("\"", "\"\""), // Escape quotes for CSV
                            log.getDetails().replace("\"", "\"\"")
                    );
                }
            } else if ("TXT".equalsIgnoreCase(format)) {
                for (OperationLog log : logs) {
                    writer.printf("[%s] [%s] [%s] %s: %s%n",
                            log.getTimestamp().format(FORMATTER),
                            log.getActorType(),
                            log.getActorUsername(),
                            log.getAction(),
                            log.getDetails()
                    );
                }
            } else {
                System.out.println("Unsupported log export format: " + format);
                return false;
            }
            System.out.println("Logs exported successfully to: " + filePath);
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting logs to file: " + e.getMessage());
            return false;
        }
    }
}
