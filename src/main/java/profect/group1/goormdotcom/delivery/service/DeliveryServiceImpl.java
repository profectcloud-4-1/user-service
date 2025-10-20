package profect.group1.goormdotcom.delivery.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import profect.group1.goormdotcom.delivery.repository.DeliveryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnAddressRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryStepHistoryRepository;
import profect.group1.goormdotcom.delivery.repository.DeliveryReturnStepHistoryRepository;

import java.util.UUID;
import java.util.Optional;

import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.repository.entity.DeliveryEntity;
import profect.group1.goormdotcom.delivery.repository.mapper.DeliveryMapper;

@Service
@Transactional
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

	private final DeliveryRepository repo;
	private final DeliveryReturnRepository returnRepo;
	private final DeliveryAddressRepository addressRepo;
	private final DeliveryReturnAddressRepository returnAddressRepo;
	private final DeliveryStepHistoryRepository stepHistoryRepo;
	private final DeliveryReturnStepHistoryRepository returnStepHistoryRepo;

	public Integer canReturn(UUID orderId) {
		Optional<DeliveryEntity> found = this.repo.findByOrderId(orderId);
		if (found.isEmpty()) return -1;

		return DeliveryMapper.toDomain(found.get()).canReturn();
	}
}


