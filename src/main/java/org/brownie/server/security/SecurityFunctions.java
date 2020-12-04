package org.brownie.server.security;

import javax.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public class SecurityFunctions {

    public enum ALGORITHM {
        SHA256 {
            @Override
            public String toString() { return "SHA-256"; }
        },
        SHA512 {
            @Override
            public String toString() {
                return "SHA-512";
            }
        };

        public abstract String toString();
    }

    public static String getSHA(@NotNull String value,
                                @NotNull ALGORITHM algorithm) {

        byte[] data = value.getBytes(StandardCharsets.UTF_8);

        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algorithm.toString());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (messageDigest == null) return null;

        return new String(messageDigest.digest(data));
    }

    public static String getRandomUUIDString() {
        return UUID.randomUUID().toString();
    }

    public static String getClearRandomUUIDString() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getSaltedPasswordHash(String password, String salt) {
        String beginning = salt.substring(0, salt.length() / 2);
        String ending = salt.substring(salt.length() / 2);

        return SecurityFunctions.getSHA(beginning + password + ending, ALGORITHM.SHA512);
    }
}