package com.data.backup;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;

public class EncryptionUtil {
	
//	public static String generateSecretKey() {
//        byte[] key = new byte[16]; // 16 bytes = 128 bits
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(key);
//        return Base64.getEncoder().encodeToString(key);
//    }
	
    private static String SECRET_KEY = "abcdefjhijklmnop"; // Replace with your own secret key
    private static final String ALGORITHM = "AES";

    public static String encryptPassword(String password) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encryptedBytes = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt password.", e);
        }
    }

    public static String decryptPassword(String encryptedPassword) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedPassword));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt password.", e);
        }
    }
}

