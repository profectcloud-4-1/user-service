package profect.group1.goormdotcom.stock.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import profect.group1.goormdotcom.stock.repository.entity.StockEntity; 

public interface StockRepository extends JpaRepository<StockEntity, UUID>{   
}
