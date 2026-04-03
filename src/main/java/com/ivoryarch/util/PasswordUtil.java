package com.ivoryarch.util;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    public static String hashPassword(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(plainText.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) { throw new RuntimeException("SHA-256 not available", e); }
    }
    public static boolean verify(String plain, String hashed) { return hashPassword(plain).equals(hashed); }
}
