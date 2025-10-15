package profect.group1.goormdotcom.user.infra;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

interface UserJpaRepository extends JpaRepository<UserJpaEntity, UUID> {}