package profect.group1.goormdotcom.delivery.repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import profect.group1.goormdotcom.delivery.repository.entity.BrandAddressEntity;

public interface BrandAddressRepository extends JpaRepository<BrandAddressEntity, UUID> {
	Optional<BrandAddressEntity> findByBrandId(UUID brandId);
    List<BrandAddressEntity> findAllByBrandId(UUID brandId);
}