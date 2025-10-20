package profect.group1.goormdotcom.delivery.service;

import java.util.UUID;

public interface DeliveryService {
	/**
	 * 반송 가능 여부 확인
	 * @return -1: 존재하지 않는 order id, 0: 반송불가(현재 배송중), 1: 즉시취소 가능(배송 시작 전이기 때문), 2: 반송 프로세스를 거쳐 반송 가능(배송 완료됨)
	 */
	public Integer canReturn(UUID orderId);
}
