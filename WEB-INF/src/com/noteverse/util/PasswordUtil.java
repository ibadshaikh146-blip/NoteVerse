package com.noteverse.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Basic password hashing using SHA-256.
 *
 * NOTE: For a production system you'd use BCrypt (via the jBCrypt library)
 * instead — it's slower by design and salts automatically, which makes it
 * far more resistant to brute-force attacks than a plain SHA-256 hash.
 * SHA-256 is used here to avoid adding another external jar dependency
 * for a college capstone project, but this distinction is worth mentioning
 * if asked about security in a viva/presentation.
 */
public class PasswordUtil {

    public static String hash(String plainPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainPassword.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public static boolean verify(String plainPassword, String storedHash) {
        return hash(plainPassword).equals(storedHash);
    }
}
