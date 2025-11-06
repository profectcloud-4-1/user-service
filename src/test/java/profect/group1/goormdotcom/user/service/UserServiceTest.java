package profect.group1.goormdotcom.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.common.security.JwtTokenProvider;
import profect.group1.goormdotcom.user.domain.User;
import profect.group1.goormdotcom.user.domain.enums.UserRole;
import profect.group1.goormdotcom.user.domain.mapper.UserMapper;
import profect.group1.goormdotcom.user.infrastructure.client.CartClient;
import profect.group1.goormdotcom.user.repository.UserRepository;
import profect.group1.goormdotcom.user.repository.entity.UserEntity;
import profect.group1.goormdotcom.user.service.dto.CreateUserDto;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordService passwordService;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private CartClient cartClient;

    @Mock
    private UserMapper userMapper;

    private CreateUserDto createUserDto;
    private UserEntity userEntity;
    private User user;

    @BeforeEach
    void setUp() {
        createUserDto = new CreateUserDto("testuser", "test@example.com", "Password123!");

        userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .password("encryptedPassword")
                .name("testuser")
                .build();

        user = User.builder()
                .id(userEntity.getId())
                .email(userEntity.getEmail())
                .name(userEntity.getName())
                .build();
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void testRegister_success() {
        // given
        when(passwordService.validate("Password123!")).thenReturn(true);
        when(passwordService.encrypt("Password123!")).thenReturn("encryptedPassword");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(UserEntity.class))).thenReturn(userEntity);
        when(jwtTokenProvider.generateAccessToken(any(UUID.class), any(String.class))).thenReturn("accessToken");
        when(cartClient.create(any(String.class))).thenReturn(ApiResponse.onSuccess(UUID.randomUUID()));

        // when
        User registeredUser = userService.register(createUserDto);

        // then
        assertThat(registeredUser).isNotNull();
    }

    @Test
    @DisplayName("비밀번호 규칙 검증 미통과")
    void testPasswordValidation_fail() {
        // given
        String invalidPassword = "password123"; // 대문자/특수문자 없음

        // when
        boolean result = passwordService.validate(invalidPassword);

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복으로 인한 실패")
    void testRegister_emailAlreadyExists() {
        // given
        when(passwordService.validate("Password123!")).thenReturn(true);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(createUserDto);
        });
    }

    @Test
    @DisplayName("로그인 - 성공")
    void testLogin_success() {
        // given
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(userEntity));

        when(passwordService.isMatch("Password123!", "encryptedPassword"))
                .thenReturn(true);

        when(jwtTokenProvider.generateAccessToken(any(UUID.class), isNull()))
                .thenReturn("accessToken");

        // when
        String token = userService.login("test@example.com", "Password123!");

        // then
        assertThat(token).isEqualTo("accessToken");
    }

    @Test
    @DisplayName("로그인 - 잘못된 비밀번호로 인한 실패")
    void testLogin_invalidPassword() {
        // given
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(userEntity));
        when(passwordService.isMatch("wrongpassword", "encryptedPassword")).thenReturn(false);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> {
            userService.login("test@example.com", "wrongpassword");
        });
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 이메일")
    void login_invalidEmail() {
        // given
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.login("test@example.com", "Password123!"));
    }

    @Test
    @DisplayName("edit - 이름/이메일 수정 성공")
    void edit_success() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());

        // when
        userService.edit(user.getId(), "newName", "new@example.com");

        // then
        verify(userRepository).findById(user.getId());
        verify(userRepository).findByEmail("new@example.com");
        verify(userRepository).save(any(UserEntity.class));
        assertThat(userEntity.getName()).isEqualTo("newName");
        assertThat(userEntity.getEmail()).isEqualTo("new@example.com");
    }

    @Test
    @DisplayName("edit - 이메일 중복 시 예외")
    void edit_emailAlreadyExists() {
        // given
        UserEntity other = UserEntity.builder()
                .id(UUID.randomUUID())
                .email("new@example.com")
                .password("xxx")
                .name("other")
                .role(UserRole.CUSTOMER.getCode())
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(userEntity));
        when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.of(other));

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.edit(user.getId(), "newName", "new@example.com"));
    }

    @Test
    @DisplayName("deleteById - 성공")
    void deleteById_success() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(userEntity));

        // when
        userService.deleteById(user.getId());

        // then
        verify(userRepository).findById(user.getId());
        verify(userRepository).delete(userEntity);
    }

    @Test
    @DisplayName("deleteById - 사용자 없음")
    void deleteById_notFound() {
        // given
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userService.deleteById(user.getId()));
    }
}