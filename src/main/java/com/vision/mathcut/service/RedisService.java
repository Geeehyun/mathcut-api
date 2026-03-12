package com.vision.mathcut.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;

    // ── Refresh Token ──────────────────────────────────────

    public void saveRefreshToken(Long userId, String refreshToken, long ttlMs) {
        stringRedisTemplate.opsForValue()
                .set("RT:" + userId, refreshToken, ttlMs, TimeUnit.MILLISECONDS);
    }

    public String getRefreshToken(Long userId) {
        return stringRedisTemplate.opsForValue().get("RT:" + userId);
    }

    public void deleteRefreshToken(Long userId) {
        stringRedisTemplate.delete("RT:" + userId);
    }

    // ── Access Token 블랙리스트 ────────────────────────────

    public void addToBlacklist(String accessToken, long remainingMs) {
        if (remainingMs > 0) {
            stringRedisTemplate.opsForValue()
                    .set("BL:" + accessToken, "logout", remainingMs, TimeUnit.MILLISECONDS);
        }
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey("BL:" + accessToken));
    }
}
