package com.noteverse.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Helpers for password-reset tokens.
 * The raw token is only ever sent to the user's email; the database
 * stores just its SHA-256 hash, so a leaked database can't be used
 * to reset anyone's password.
 */
public class TokenUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    // 32 random bytes -> 64 hex characters
    public static String generateToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return toHex(bytes);
    }

    public static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return toHex(md.digest(input.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 is not available on this JVM.", e);
        }
    }

    // Quick shape check so obviously bogus tokens never hit the database
    public static boolean looksLikeToken(String value) {
        return value != null && value.matches("[0-9a-f]{64}");
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
