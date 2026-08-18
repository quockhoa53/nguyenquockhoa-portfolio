package com.portfolio.infrastructure.security;

import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.stereotype.Service;

/**
 * Standard RFC 6238 TOTP (Time-Based One-Time Password) Service.
 * Generates Base32 secrets and validates 6-digit authentication codes
 * compatible with Google Authenticator, Microsoft Authenticator, and Authy.
 */
@Service
public class TotpService {
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private static final int TIME_STEP_SECONDS = 30;
    private static final int CODE_DIGITS = 6;
    private static final int SECRET_BYTE_LENGTH = 20; // 160 bits (32 Base32 chars)
    private static final String BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Generates a new random Base32 secret key.
     */
    public String generateSecret() {
        byte[] buffer = new byte[SECRET_BYTE_LENGTH];
        secureRandom.nextBytes(buffer);
        return encodeBase32(buffer);
    }

    /**
     * Builds standard otpauth:// URI for scanning into Authenticator apps.
     */
    public String buildOtpAuthUri(String username, String secret) {
        String issuer = "NQK Portfolio";
        String label = issuer + ":" + username;
        return String.format(
                "otpauth://totp/%s?secret=%s&issuer=%s&algorithm=SHA1&digits=6&period=30",
                URLEncoder.encode(label, StandardCharsets.UTF_8).replace("+", "%20"),
                secret,
                URLEncoder.encode(issuer, StandardCharsets.UTF_8).replace("+", "%20")
        );
    }

    /**
     * Validates a 6-digit TOTP code against the secret key.
     * Allows a drift window of ±1 step (±30 seconds) to handle device clock offset smoothly.
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.trim().length() != CODE_DIGITS) {
            return false;
        }

        try {
            int targetCode = Integer.parseInt(code.trim());
            byte[] key = decodeBase32(secret);
            long currentStep = Instant.now().getEpochSecond() / TIME_STEP_SECONDS;

            // Check previous step (-30s), current step (0s), and next step (+30s)
            for (int i = -1; i <= 1; i++) {
                long step = currentStep + i;
                int calculatedCode = generateCodeForStep(key, step);
                if (calculatedCode == targetCode) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private int generateCodeForStep(byte[] key, long step) throws Exception {
        byte[] data = ByteBuffer.allocate(8).putLong(step).array();
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        mac.init(new SecretKeySpec(key, HMAC_ALGORITHM));
        byte[] hash = mac.doFinal(data);

        // Dynamic truncation (RFC 4226)
        int offset = hash[hash.length - 1] & 0x0F;
        int binary = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);

        return binary % (int) Math.pow(10, CODE_DIGITS);
    }

    private String encodeBase32(byte[] data) {
        StringBuilder result = new StringBuilder((data.length * 8 + 4) / 5);
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;

        while (next < data.length) {
            buffer = (buffer << 8) | (data[next++] & 0xFF);
            bitsLeft += 8;
            while (bitsLeft >= 5) {
                bitsLeft -= 5;
                result.append(BASE32_CHARS.charAt((buffer >> bitsLeft) & 0x1F));
            }
        }

        if (bitsLeft > 0) {
            result.append(BASE32_CHARS.charAt((buffer << (5 - bitsLeft)) & 0x1F));
        }

        return result.toString();
    }

    private byte[] decodeBase32(String base32) {
        String clean = base32.toUpperCase().replaceAll("[^A-Z2-7]", "");
        byte[] out = new byte[(clean.length() * 5) / 8];
        int buffer = 0;
        int bitsLeft = 0;
        int outIndex = 0;

        for (int i = 0; i < clean.length(); i++) {
            char c = clean.charAt(i);
            int val = BASE32_CHARS.indexOf(c);
            if (val < 0) continue;

            buffer = (buffer << 5) | val;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                bitsLeft -= 8;
                if (outIndex < out.length) {
                    out[outIndex++] = (byte) ((buffer >> bitsLeft) & 0xFF);
                }
            }
        }

        return Arrays.copyOf(out, outIndex);
    }
}
