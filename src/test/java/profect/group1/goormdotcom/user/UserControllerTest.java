package profect.group1.goormdotcom.user.controller.external.v1;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.apiPayload.code.status.ErrorStatus;
import profect.group1.goormdotcom.user.controller.auth.LoginUser;
import profect.group1.goormdotcom.user.controller.external.v1.dto.request.EditRequestDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.request.LoginRequestDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.request.RegisterRequestDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.request.UserAddressRequestDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.response.LoginResponseDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.response.MeResponseDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.response.RegisterResponseDto;
import profect.group1.goormdotcom.user.controller.external.v1.dto.response.UserAddressListResponseDto;
import profect.group1.goormdotcom.user.domain.User;
import profect.group1.goormdotcom.user.domain.UserAddress;
import profect.group1.goormdotcom.user.service.UserAddressService;
import profect.group1.goormdotcom.user.service.UserService;
import profect.group1.goormdotcom.user.service.dto.CreateUserDto;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @InjectMocks
    private UserController userController;

    @Mock
    private UserService userService;

    @Mock
    private UserAddressService userAddressService;

    private RegisterRequestDto registerRequestDto;
    private LoginRequestDto loginRequestDto;
    private EditRequestDto editRequestDto;
    private UserAddressRequestDto userAddressRequestDto;

    private User user;
    private UserAddress userAddress;
    private UUID userId;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        registerRequestDto = RegisterRequestDto.builder()
                .name("Test User")
                .email("test@example.com")
                .password("Password123!")
                .build();

        loginRequestDto = LoginRequestDto.builder()
                .email("test@example.com")
                .password("Password123!")
                .build();

        editRequestDto = EditRequestDto.builder()
                .name("Edited User")
                .email("edited@example.com")
                .build();

        userAddressRequestDto = UserAddressRequestDto.builder()
                .address("Address 1")
                .addressDetail("Detail 1")
                .zipcode("12345")
                .phone("010-1111-2222")
                .name("Receiver")
                .deliveryMemo("Memo")
                .build();

        user = User.builder()
                .id(userId)
                .name("Test User")
                .email("test@example.com")
                .build();

        userAddress = UserAddress.builder()
                .id(addressId)
                .userId(userId)
                .address("Address 1")
                .addressDetail("Detail 1")
                .zipcode("12345")
                .phone("010-1111-2222")
                .name("Receiver")
                .deliveryMemo("Memo")
                .build();
    }

    @Test
    @DisplayName("회원가입 성공 시 RegisterResponseDto의 ID가 생성된 유저 ID와 같다")
    void register_success() {
        // given
        when(userService.register(any(CreateUserDto.class))).thenReturn(user);

        // when
        ApiResponse<RegisterResponseDto> response = userController.register(registerRequestDto);

        // then
        assertThat(response.getResult().getId())
                .isEqualTo(user.getId().toString());

        verify(userService, times(1)).register(any(CreateUserDto.class));
    }

    @Test
    @DisplayName("회원가입 - 이메일 중복 시 AUTH_EMAIL_ALREADY_EXISTS 코드 반환")
    void register_fail_emailAlreadyExists() {
        // given
        when(userService.register(any(CreateUserDto.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        // when
        ApiResponse<RegisterResponseDto> response = userController.register(registerRequestDto);

        // then
        assertThat(response.getCode())
                .isEqualTo(ErrorStatus.AUTH_EMAIL_ALREADY_EXISTS.getCode());

        verify(userService, times(1)).register(any(CreateUserDto.class));
    }

    @Test
    @DisplayName("로그인 성공 시 반환된 액세스 토큰이 서비스에서 받은 토큰과 같다")
    void login_success() {
        // given
        String token = "jwt-token";
        when(userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword()))
                .thenReturn(token);

        // when
        ApiResponse<LoginResponseDto> response = userController.login(loginRequestDto);

        // then
        assertThat(response.getResult().getAccessToken())
                .isEqualTo(token);

        verify(userService, times(1))
                .login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    @Test
    @DisplayName("로그인 - 잘못된 자격 증명 시 AUTH_NOT_EXISTS 코드 반환")
    void login_fail_invalidCredentials() {
        // given
        when(userService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword()))
                .thenThrow(new RuntimeException("Invalid credentials"));

        // when
        ApiResponse<LoginResponseDto> response = userController.login(loginRequestDto);

        // then
        assertThat(response.getCode())
                .isEqualTo(ErrorStatus.AUTH_NOT_EXISTS.getCode());

        verify(userService, times(1))
                .login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
    }

    @Test
    @DisplayName("내 정보 조회(me) 시 반환된 DTO의 유저 ID가 로그인 유저와 같다")
    void me_success() {
        // given
        when(userService.findById(userId)).thenReturn(user);

        // when
        ApiResponse<MeResponseDto> response = userController.me(userId);

        // then
        assertThat(response.getResult().getMe().getId())
                .isEqualTo(userId);

        verify(userService, times(1)).findById(userId);
    }

    @Test
    @DisplayName("주소 생성 - 성공 시 반환된 UUID가 생성된 주소 ID와 같다")
    void createAddress_success() {
        // given
        when(userAddressService.createUserAddress(userId, userAddressRequestDto))
                .thenReturn(userAddress);

        // when
        ApiResponse<UUID> response =
                userController.createAddress(userId, userAddressRequestDto);

        // then
        assertThat(response.getResult())
                .isEqualTo(addressId);

        verify(userAddressService, times(1))
                .createUserAddress(userId, userAddressRequestDto);
    }

    @Test
    @DisplayName("주소 수정 - 성공 시 반환된 UUID가 요청한 주소 ID와 같다")
    void updateAddress_success() {
        // given
        when(userAddressService.updateUserAddress(addressId, userAddressRequestDto))
                .thenReturn(userAddress);

        // when
        ApiResponse<UUID> response =
                userController.updateAddress(userId, addressId, userAddressRequestDto);

        // then
        assertThat(response.getResult())
                .isEqualTo(addressId);

        verify(userAddressService, times(1))
                .updateUserAddress(addressId, userAddressRequestDto);
    }

    @Test
    @DisplayName("주소 수정 - 주소가 없으면 NOT_FOUND 코드 반환")
    void updateAddress_notFound() {
        // given
        when(userAddressService.updateUserAddress(addressId, userAddressRequestDto))
                .thenThrow(new IllegalArgumentException("UserAddress not found"));

        // when
        ApiResponse<UUID> response =
                userController.updateAddress(userId, addressId, userAddressRequestDto);

        // then
        assertThat(response.getCode())
                .isEqualTo(ErrorStatus._NOT_FOUND.getCode());

        verify(userAddressService, times(1))
                .updateUserAddress(addressId, userAddressRequestDto);
    }

    @Test
    @DisplayName("주소 전체 조회 - 리스트 사이즈가 서비스에서 반환한 값과 같다")
    void getAddresses_success() {
        // given
        List<UserAddress> list = Collections.singletonList(userAddress);
        when(userAddressService.getAllByUserId(userId)).thenReturn(list);

        // when
        ApiResponse<UserAddressListResponseDto> response =
                userController.getAddresses(userId);

        // then
        assertThat(response.getResult().getList().size())
                .isEqualTo(1);

        verify(userAddressService, times(1)).getAllByUserId(userId);
    }

    @Test
    @DisplayName("단일 주소 조회 - 반환된 주소의 ID가 요청한 주소 ID와 같다")
    void getAddress_success() {
        // given
        when(userAddressService.getById(addressId)).thenReturn(userAddress);

        // when
        ApiResponse<UserAddress> response =
                userController.getAddress(userId, addressId);

        // then
        assertThat(response.getResult().getId())
                .isEqualTo(addressId);

        verify(userAddressService, times(1)).getById(addressId);
    }
}