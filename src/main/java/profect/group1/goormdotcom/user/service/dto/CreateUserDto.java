package profect.group1.goormdotcom.user.service.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CreateUserDto {
    private String name;
    private String email;
    private String password;
}