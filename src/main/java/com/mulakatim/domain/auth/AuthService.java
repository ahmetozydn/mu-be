package com.mulakatim.domain.auth;

import com.mulakatim.config.JwtConfig;
import com.mulakatim.domain.auth.dto.*;
import com.mulakatim.domain.user.User;
import com.mulakatim.domain.user.UserRepository;
import com.mulakatim.domain.user.UserService;
import com.mulakatim.domain.user.dto.UserResponse;
import com.mulakatim.shared.enums.Role;
import com.mulakatim.shared.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtConfig jwtConfig;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw ApiException.conflict("EMAIL_ALREADY_EXISTS", "Bu e-posta ile zaten bir hesap mevcut.");
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> ApiException.unauthorized("INVALID_CREDENTIALS", "E-posta veya sifre hatali."));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw ApiException.unauthorized("INVALID_CREDENTIALS", "E-posta veya sifre hatali.");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse googleAuth(GoogleAuthRequest request) {
        GoogleOAuthService.GoogleUserInfo info = googleOAuthService.verify(request.idToken());

        boolean isNewUser = false;

        User user = userRepository.findByGoogleId(info.googleId())
                .orElseGet(() -> userRepository.findByEmail(info.email())
                        .map(existing -> {
                            existing.setGoogleId(info.googleId());
                            return userRepository.save(existing);
                        })
                        .orElse(null));

        if (user == null) {
            isNewUser = true;
            user = User.builder()
                    .name(info.name() != null ? info.name() : info.email())
                    .email(info.email())
                    .googleId(info.googleId())
                    .role(Role.USER)
                    .build();
            user = userRepository.save(user);
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        storeRefreshToken(user.getId(), refreshToken);

        return AuthResponse.ofGoogle(accessToken, refreshToken, UserResponse.from(user), isNewUser);
    }

    @Transactional
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {
        String token = request.refreshToken();

        if (!jwtService.isTokenValid(token) || !jwtService.isRefreshToken(token)) {
            throw ApiException.unauthorized("REFRESH_TOKEN_EXPIRED", "Refresh token gecersiz veya suresi dolmus.");
        }

        RefreshToken stored = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> ApiException.unauthorized("REFRESH_TOKEN_EXPIRED", "Refresh token gecersiz."));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(stored);
            throw ApiException.unauthorized("REFRESH_TOKEN_EXPIRED", "Refresh token suresi dolmus.");
        }

        UUID userId = jwtService.extractUserId(token);
        User user = userService.findById(userId);
        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        return new RefreshTokenResponse(newAccessToken);
    }

    @Transactional
    public void logout(UUID userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private AuthResponse buildAuthResponse(User user) {
        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());
        storeRefreshToken(user.getId(), refreshToken);
        return AuthResponse.of(accessToken, refreshToken, UserResponse.from(user));
    }

    private void storeRefreshToken(UUID userId, String refreshToken) {
        refreshTokenRepository.deleteByUserId(userId);
        RefreshToken entity = RefreshToken.builder()
                .userId(userId)
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtConfig.getRefreshTokenExpiry()))
                .build();
        refreshTokenRepository.save(entity);
    }
}
