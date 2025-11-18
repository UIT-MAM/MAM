import com.se114p12.backend.entities.authentication.User2FABackupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface User2FABackupCodeRepository
    extends JpaRepository<User2FABackupCode, Long>, JpaSpecificationExecutor<User2FABackupCode> {}
