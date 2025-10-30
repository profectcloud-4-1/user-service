package profect.group1.goormdotcom.review.domain;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
public class ReviewImage {
    private UUID id;
    private UUID reviewId;
    private UUID fileId;
    private String imageUrl;
    private LocalDateTime deletedAt;

    //imageUrl 없이 생성하는 생성자
    public ReviewImage(UUID id, UUID reviewId, UUID fileId) {
        this.id = id;
        this.reviewId = reviewId;
        this.fileId = fileId;
        this.imageUrl = null;
        this.deletedAt = null;
    }

    /*private UUID id;
    private UUID reviewId;
    private String imageUrl;
    private LocalDateTime deletedAt;

    public void updateReviewId(UUID productId) {
        this.reviewId = reviewId;
    }

    public ReviewImage(
            UUID id,
            UUID reviewId
    ) {
        this.id = id;
        this.reviewId = reviewId;
    }

    public ReviewImage(
            UUID id,
            UUID reviewId,
            String imageUrl
    ) {
        this.id = id;
        this.reviewId = reviewId;
        this.imageUrl = imageUrl;
    }*/
}