package IptGroup.ParadoxIncTask.controller;

import IptGroup.ParadoxIncTask.dto.AuthRequest;
import IptGroup.ParadoxIncTask.dto.AuthResponse;
import IptGroup.ParadoxIncTask.entity.User;
import IptGroup.ParadoxIncTask.provider.JwtTokenProvider;
import IptGroup.ParadoxIncTask.repository.UserRepository;
import IptGroup.ParadoxIncTask.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);

        return ResponseEntity.ok("User registered successfully");
    }

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String accessToken = jwtTokenProvider.generateToken(user.getUsername(), accessTokenExpirationMs);
        String refreshToken = jwtTokenProvider.generateToken(user.getUsername(), refreshTokenExpirationMs);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");

        return refreshTokenService.findByToken(refreshToken)
                .map(token -> {
                    if (refreshTokenService.isExpired(token)) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired");
                    }
                    String newAccessToken = jwtTokenProvider.generateToken(
                            token.getUser().getUsername(),
                            accessTokenExpirationMs
                    );
                    return ResponseEntity.ok(Map.of(
                            "accessToken",  newAccessToken,
                            "refreshToken", refreshToken
                    ));
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token"));
    }
}

