package com.mmva.newsapp.infrastructure.security.encryption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Encryption Service for GDPR compliance.
 * Provides AES-256-GCM encryption for sensitive data fields.
 */
@Service
@ConditionalOnProperty(name = "app.encryption.enabled", havingValue = "true", matchIfMissing = false)
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    private final SecretKey secretKey;

    public EncryptionService(@Value("${app.encryption.secret:}") String encryptionKey) {
        if (encryptionKey == null || encryptionKey.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Encryption secret not configured. Set app.encryption.secret property.");
        }

        // Convert hex string to byte array for key
        byte[] keyBytes = hexStringToByteArray(encryptionKey);
        if (keyBytes.length != KEY_SIZE / 8) {
            throw new IllegalStateException(
                    "Encryption key must be 256 bits (64 hex characters)");
        }

        this.secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Encrypts sensitive data using AES-256-GCM.
     *
     * @param data The plain text data to encrypt
     * @return Base64 encoded encrypted data with IV
     */
    public String encrypt(String data) {
        if (data == null || data.trim().isEmpty()) {
            return data; // Don't encrypt empty/null data
        }

        try {
            // Generate random IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom secureRandom = new SecureRandom();
            secureRandom.nextBytes(iv);

            // Initialize cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            // Encrypt data
            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            // Combine IV + encrypted data
            byte[] combined = new byte[GCM_IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, combined, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, 0, combined, GCM_IV_LENGTH, encryptedData.length);

            // Return as Base64
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Decrypts data encrypted with the encrypt() method.
     *
     * @param encryptedData Base64 encoded encrypted data with IV
     * @return The decrypted plain text
     */
    public String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.trim().isEmpty()) {
            return encryptedData; // Don't decrypt empty/null data
        }

        try {
            // Decode from Base64
            byte[] combined = Base64.getDecoder().decode(encryptedData);

            // Extract IV and encrypted data
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] encrypted = new byte[combined.length - GCM_IV_LENGTH];

            System.arraycopy(combined, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(combined, GCM_IV_LENGTH, encrypted, 0, encrypted.length);

            // Initialize cipher for decryption
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encrypted);

            return new String(decryptedData, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Generates a new random encryption key for development/testing.
     * WARNING: Never use this in production!
     *
     * @return Hex string representation of the key
     */
    public static String generateKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(KEY_SIZE);
            SecretKey key = keyGenerator.generateKey();
            return byteArrayToHexString(key.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    private static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    private static String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}