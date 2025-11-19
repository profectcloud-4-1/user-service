package profect.group1.goormdotcom.user.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginTokensDto {
    private final String accessToken;
    private final String refreshToken;
}