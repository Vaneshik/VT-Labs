package util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {
    public static String getHash(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(password.getBytes());
        byte[] hashedPassword = md.digest();
        return bytesToHex(hashedPassword);
    }

    private static String bytesToHex(byte[] hashedPassword) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : hashedPassword) {
            stringBuilder.append(String.format("%02x", b));
        }
        return stringBuilder.toString();
    }
}
