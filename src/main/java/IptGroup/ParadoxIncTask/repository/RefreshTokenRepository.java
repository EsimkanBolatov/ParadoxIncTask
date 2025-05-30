package IptGroup.ParadoxIncTask.repository;

import IptGroup.ParadoxIncTask.entity.RefreshToken;
import IptGroup.ParadoxIncTask.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUser(User user);
}
