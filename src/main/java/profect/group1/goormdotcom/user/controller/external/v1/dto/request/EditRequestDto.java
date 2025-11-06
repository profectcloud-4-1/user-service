package profect.group1.goormdotcom.user.controller.external.v1.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Getter
@Builder
public class EditRequestDto {
    @Schema(description = "이름")
    @NotBlank
    private String name;
    @Schema(description = "이메일")
    @NotBlank
    @Email
    private String email;
}