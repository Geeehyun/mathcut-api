package com.vision.mathcut.service;

import com.vision.mathcut.config.JwtTokenProvider;
import com.vision.mathcut.domain.User;
import com.vision.mathcut.dto.auth.LoginRequest;
import com.vision.mathcut.dto.auth.LoginResponse;
import com.vision.mathcut.dto.auth.RefreshRequest;
import com.vision.mathcut.dto.auth.RegisterRequest;
import com.vision.mathcut.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        String accessToken = jwtTokenProvider.generateToken(user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        redisService.saveRefreshToken(user.getId(), refreshToken, jwtTokenProvider.getRefreshExpirationMs());

        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname());
    }

    public LoginResponse refresh(RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 리프레시 토큰입니다.");
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);
        String stored = redisService.getRefreshToken(userId);

        if (stored == null || !stored.equals(refreshToken)) {
            throw new IllegalArgumentException("리프레시 토큰이 만료되었거나 로그아웃된 계정입니다.");
        }

        // Refresh Token Rotation
        String newAccessToken = jwtTokenProvider.generateToken(userId);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userId);
        redisService.deleteRefreshToken(userId);
        redisService.saveRefreshToken(userId, newRefreshToken, jwtTokenProvider.getRefreshExpirationMs());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new LoginResponse(newAccessToken, newRefreshToken, user.getId(), user.getNickname());
    }

    public void logout(String accessToken, Long userId) {
        long remaining = jwtTokenProvider.getRemainingMs(accessToken);
        redisService.addToBlacklist(accessToken, remaining);
        redisService.deleteRefreshToken(userId);
    }
}
