package profect.group1.goormdotcom.review.controller.v1;

import profect.group1.goormdotcom.review.controller.dto.ReviewResponseDto;

import java.util.List;
import java.util.UUID;

public class ProductReviewListResponse {
    private UUID productId;
    private String productName;
    private Double averageRating;
    private Integer totalReviews;
    private List<ReviewResponseDto> reviews;

    // Getters & Constructor
}
