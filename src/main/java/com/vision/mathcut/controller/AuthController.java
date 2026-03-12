package com.vision.mathcut.controller;

import com.vision.mathcut.dto.auth.LoginRequest;
import com.vision.mathcut.dto.auth.LoginResponse;
import com.vision.mathcut.dto.auth.RefreshRequest;
import com.vision.mathcut.dto.auth.RegisterRequest;
import com.vision.mathcut.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "가입되었습니다."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String bearerToken,
                                       @AuthenticationPrincipal Long userId) {
        if (userId == null) {
            return ResponseEntity.noContent().build();
        }
        String token = StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")
                ? bearerToken.substring(7) : null;
        if (token != null) {
            authService.logout(token, userId);
        }
        return ResponseEntity.noContent().build();
    }
}
