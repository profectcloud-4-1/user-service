package profect.group1.goormdotcom.review.repository.mapper;

import profect.group1.goormdotcom.review.domain.Review;
import profect.group1.goormdotcom.review.repository.entity.ReviewEntity;
import profect.group1.goormdotcom.review.repository.entity.ReviewImageEntity;

import java.util.UUID;

public class ReviewMapper {
    /**
     * Domain → Entity
     */
    public static ReviewEntity toEntity(Review review) {
        return new ReviewEntity(
                review.getId(),
                review.getUserId(),
                review.getProductId(),
                review.getDeliveryId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt(),
                review.getUpdatedAt(),
                null  // deletedAt은 null (신규 생성)
        );

        /**
         * .orderProductId(domain.getOrderProductId())
         *  .storeId(domain.getBrandId())
         *  .updatedAt(domain.getUpdatedAt())
         */
    }

    /**
     * Entity → Domain
     * (imageUrl은 별도 조회 후 합성)
     */
    public static Review toDomain(ReviewEntity entity, String imageUrl) {
        return Review.of(
                entity.getId(),
                entity.getUserId(),
                entity.getProductId(),
                entity.getDeliveryId(),
                entity.getRating(),
                entity.getContent(),
                imageUrl,  // 별도 조회한 이미지 URL
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    /**
     * Domain의 imageUrl → ReviewImageEntity
     */
    public static ReviewImageEntity toImageEntity(UUID reviewId, String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return null;  // 이미지 없으면 null 반환
        }

        return new ReviewImageEntity(
                UUID.randomUUID(),
                reviewId,
                imageUrl,
                java.time.LocalDateTime.now(),
                null  // deletedAt은 null
        );
    }
}
