package com.banksimulation.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * 密码哈希工具类
 * Utility class for hashing and verifying passwords.
 * NOTE: For production, consider using stronger hashing libraries like BCrypt or Argon2.
 */
public class PasswordHasher {

    /**
     * 对密码进行哈希处理
     * Hashes a plain text password using SHA-256.
     * @param password The plain text password.
     * @return The Base64 encoded hash of the password.
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            // This should not happen with SHA-256
            throw new RuntimeException("SHA-256 algorithm not found.", e);
        }
    }

    /**
     * 验证密码
     * Verifies a plain text password against a hashed password.
     * @param plainPassword The plain text password to verify.
     * @param hashedPassword The stored hashed password.
     * @return true if the plain password matches the hashed password, false otherwise.
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        String newHash = hashPassword(plainPassword);
        return newHash.equals(hashedPassword);
    }
}
