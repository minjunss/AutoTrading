package com.example.UbitAutoTrading.redisTest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserRedisService {
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;

    public RefreshToken save(RefreshToken adminUserToken) {
        return refreshTokenRedisRepository.save(adminUserToken);
    }

    public RefreshToken findById(String userId) {
        return refreshTokenRedisRepository.findById(userId).get();
    }
}