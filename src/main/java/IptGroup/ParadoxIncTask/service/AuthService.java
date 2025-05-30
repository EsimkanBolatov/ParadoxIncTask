package IptGroup.ParadoxIncTask.service;

import IptGroup.ParadoxIncTask.dto.AuthRequest;
import IptGroup.ParadoxIncTask.entity.RefreshToken;
import IptGroup.ParadoxIncTask.provider.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationConfiguration authConfig;
    private final JwtTokenProvider           jwtTokenProvider;
    private final RefreshTokenService        refreshTokenService;

    @Value("${jwt.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;

    public Map<String, String> login(AuthRequest authRequest) throws Exception {
        // Получаем AuthenticationManager «на лету»
        AuthenticationManager authManager = authConfig.getAuthenticationManager();
        authManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        String accessToken = jwtTokenProvider.generateToken(
                authRequest.getUsername(),
                accessTokenExpirationMs
        );
        RefreshToken refreshToken = refreshTokenService
                .createRefreshToken(authRequest.getUsername());

        return Map.of(
                "accessToken",  accessToken,
                "refreshToken", refreshToken.getToken()
        );
    }
}



