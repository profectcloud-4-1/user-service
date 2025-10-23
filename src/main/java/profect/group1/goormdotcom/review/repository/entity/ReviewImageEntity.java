package profect.group1.goormdotcom.review.repository.entity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "p_review_image")
@SQLDelete(sql = "UPDATE p_review_image SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class ReviewImageEntity {

    @Id
    @Column(name = "id", columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "review_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID reviewId;

    @Column(name = "image_object", nullable = false, length = 1024)
    private String imageObject;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // JPA 기본 생성자
    protected ReviewImageEntity() {}

    // 전체 생성자
    public ReviewImageEntity(UUID id, UUID reviewId, String imageObject,
                             LocalDateTime createdAt, LocalDateTime deletedAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.imageObject = imageObject;
        this.createdAt = createdAt;
        this.deletedAt = deletedAt;
    }

    // Getters
    public UUID getId() { return id; }
    public UUID getReviewId() { return reviewId; }
    public String getImageObject() { return imageObject; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
}
