package profect.group1.goormdotcom.user.controller.external.v1.dto.request;
import lombok.ToString;
import lombok.Getter;
import lombok.AllArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


@ToString
@Getter
@AllArgsConstructor
public class RegisterRequestDto {
    @Schema(description = "이름")
    @NotBlank
    private String name;
    @Schema(description = "이메일")
    @NotBlank
    @Email
    private String email;
    @Schema(description = "비밀번호")
    @NotBlank
    private String password;
}