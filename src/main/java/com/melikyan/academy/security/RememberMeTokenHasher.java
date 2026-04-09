package com.melikyan.academy.security;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Component
public class RememberMeTokenHasher {
    private final byte[] secretBytes;

    public RememberMeTokenHasher(@Value("${security.remember-me.hmac-secret}") String secret) {
        this.secretBytes = secret.getBytes(StandardCharsets.UTF_8);
    }

    public String hash(String token) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] result = mac.doFinal(token.getBytes(StandardCharsets.UTF_8));
            return toHex(result);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to hash remember-me token", exception);
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }
}
