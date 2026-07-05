package com.ragagentic.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

@Slf4j
@Service
public class CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // Dedicated constructor to ensure Java 8 time compliance on the mapper instance
    public CacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        // Clone and configure the object mapper to prevent mutating the global context bean
        this.objectMapper = objectMapper.copy().registerModule(new JavaTimeModule());
    }

    public void put(String keyPrefix, String rawQuestion, Object value) {
        try {
            String secureKey = generateSecureKey(keyPrefix, rawQuestion);
            String jsonValue = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(secureKey, jsonValue, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("Failed to serialize and cache RAG payload item", e);
        }
    }

    public <T> T get(String keyPrefix, String rawQuestion, Class<T> clazz) {
        try {
            String secureKey = generateSecureKey(keyPrefix, rawQuestion);
            String jsonValue = redisTemplate.opsForValue().get(secureKey);
            if (jsonValue == null) return null;
            return objectMapper.readValue(jsonValue, clazz);
        } catch (Exception e) {
            log.error("Failed to read and deserialize cached RAG payload item", e);
            return null;
        }
    }

    /**
     * Generates a collision-free SHA-256 Hex key string safely.
     */
    private String generateSecureKey(String prefix, String input) {
        if (input == null) return prefix + ":default";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return prefix + ":" + hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.warn("SHA-256 algorithm missing, falling back to clean fallback string cleaning", e);
            return prefix + ":" + input.trim().toLowerCase().replaceAll("[^a-zA-Z0-9]", "_");
        }
    }
}