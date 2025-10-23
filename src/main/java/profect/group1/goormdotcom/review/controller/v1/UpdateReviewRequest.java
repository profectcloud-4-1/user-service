package profect.group1.goormdotcom.review.controller.v1;

import jakarta.validation.constraints.*;

class UpdateReviewRequest {
    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotBlank
    @Size(min = 10, max = 500)
    private String content;

    private String imageUrl;

    // Getters
}
