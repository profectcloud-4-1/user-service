package profect.group1.goormdotcom.delivery.repository.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import profect.group1.goormdotcom.common.domain.BaseEntity;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Comment;
import jakarta.persistence.Column;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor


@Entity
@Table(name = "p_delivery")
@EntityListeners(AuditingEntityListener.class)
@Filter(name = "deletedFilter", condition = "deleted_at IS NULL")
@SQLDelete(sql = "update p_delivery set deleted_at = NOW() where id = ?")
public class DeliveryEntity extends BaseEntity {

	@Id
	private UUID id;

	@Column(name = "deleted_at")
	private LocalDateTime deletedAt;
	
	@Column(name = "order_id", nullable = false)
	@Comment("주문 ID (p_order.id)")
	private UUID orderId;

	@Column(name = "status", nullable = false, length = 7)
	@Comment("배송 상태 (code_key: DELIVERY_STATUS)")
	private String status;

	@Column(name = "tracking_num")
	@Comment("송장 번호")
	private String trackingNumber;

	
}
