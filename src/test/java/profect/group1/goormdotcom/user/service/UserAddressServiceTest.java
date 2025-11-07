package profect.group1.goormdotcom.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import profect.group1.goormdotcom.user.controller.external.v1.dto.request.UserAddressRequestDto;
import profect.group1.goormdotcom.user.domain.UserAddress;
import profect.group1.goormdotcom.user.domain.mapper.UserAddressMapper;
import profect.group1.goormdotcom.user.repository.UserAddressRepository;
import profect.group1.goormdotcom.user.repository.entity.UserAddressEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @InjectMocks
    private UserAddressService userAddressService;

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserAddressMapper userAddressMapper;

    private UserAddressRequestDto userAddressRequestDto;
    private UserAddressRequestDto userAddressUpdateRequestDto;
    private UserAddressEntity userAddressEntity;
    private UserAddressEntity userAddressUpdatedEntity;
    private UserAddress userAddress;
    private UserAddress userAddressUpdated;
    private UUID userId;
    private UUID addressId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        addressId = UUID.randomUUID();

        userAddressRequestDto = UserAddressRequestDto.builder()
                .address("Test Address")
                .addressDetail("Test Detail")
                .zipcode("12345")
                .phone("010-1234-5678")
                .name("Test User")
                .deliveryMemo("Test Memo")
                .build();

        userAddressUpdateRequestDto = UserAddressRequestDto.builder()
                .address("New Address")
                .addressDetail("New Detail")
                .zipcode("23456")
                .phone("010-8765-4321")
                .name("New User")
                .deliveryMemo("New Memo")
                .build();

        userAddressEntity = UserAddressEntity.builder()
                .id(addressId)
                .userId(userId)
                .address("Test Address")
                .addressDetail("Test Detail")
                .zipcode("12345")
                .phone("010-1234-5678")
                .name("Test User")
                .deliveryMemo("Test Memo")
                .build();

        userAddress = UserAddress.builder()
                .id(addressId)
                .userId(userId)
                .address("Test Address")
                .addressDetail("Test Detail")
                .zipcode("12345")
                .phone("010-1234-5678")
                .name("Test User")
                .deliveryMemo("Test Memo")
                .build();

        userAddressUpdated = UserAddress.builder()
                .id(addressId)
                .userId(userId)
                .address(userAddressUpdateRequestDto.getAddress())
                .addressDetail(userAddressUpdateRequestDto.getAddressDetail())
                .zipcode(userAddressUpdateRequestDto.getZipcode())
                .phone(userAddressUpdateRequestDto.getPhone())
                .name(userAddressUpdateRequestDto.getName())
                .deliveryMemo(userAddressUpdateRequestDto.getDeliveryMemo())
                .build();
    }

    @Test
    @DisplayName("주소 생성 - 성공")
    void testCreateUserAddress_success() {
        // given
        when(userAddressRepository.save(any(UserAddressEntity.class)))
                .thenReturn(userAddressEntity);
        when(userAddressMapper.toDomain(userAddressEntity))
                .thenReturn(userAddress);

        // when
        UserAddress createdAddress = userAddressService.createUserAddress(userId, userAddressRequestDto);

        // then
        assertThat(createdAddress)
                .isNotNull()
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("address", userAddressRequestDto.getAddress())
                .hasFieldOrPropertyWithValue("name", userAddressRequestDto.getName());

        // save 검증
        verify(userAddressRepository, times(1)).save(any(UserAddressEntity.class));
    }

    @Test
    @DisplayName("주소 수정 - 성공")
    void testUpdateUserAddress_success() {
        // given
        when(userAddressRepository.findById(addressId))
                .thenReturn(Optional.of(userAddressEntity));
        when(userAddressRepository.save(any(UserAddressEntity.class)))
                .thenReturn(userAddressUpdatedEntity);
        when(userAddressMapper.toDomain(userAddressUpdatedEntity))
                .thenReturn(userAddressUpdated);

        // when
        UserAddress updatedAddress =
                userAddressService.updateUserAddress(addressId, userAddressUpdateRequestDto);

        // then
        assertThat(updatedAddress.getAddress())
                .isEqualTo(userAddressUpdateRequestDto.getAddress());

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressRepository, times(1)).save(any(UserAddressEntity.class));
    }

    @Test
    @DisplayName("주소 수정 - 주소 없음")
    void testUpdateUserAddress_notFound() {
        // given
        when(userAddressRepository.findById(addressId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userAddressService.updateUserAddress(addressId, userAddressRequestDto));

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressRepository, never()).save(any());
    }

    @Test
    @DisplayName("주소 삭제 - 성공")
    void testDeleteUserAddress_success() {
        // given
        when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(userAddressEntity));

        // when
        boolean result = userAddressService.deleteUserAddress(addressId);

        // then
        assertThat(result).isTrue();

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressRepository, times(1)).delete(userAddressEntity);
    }

    @Test
    @DisplayName("주소 삭제 - 주소 없음")
    void testDeleteUserAddress_notFound() {
        // given
        when(userAddressRepository.findById(addressId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userAddressService.deleteUserAddress(addressId));

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressRepository, never()).delete(any());
    }

    @Test
    @DisplayName("ID로 주소 조회 - 성공")
    void testGetById_success() {
        // given
        when(userAddressRepository.findById(addressId)).thenReturn(Optional.of(userAddressEntity));
        when(userAddressMapper.toDomain(userAddressEntity)).thenReturn(userAddress);

        // when
        UserAddress foundAddress = userAddressService.getById(addressId);

        // then
        assertThat(foundAddress)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", addressId)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("address", "Test Address")
                .hasFieldOrPropertyWithValue("addressDetail", "Test Detail")
                .hasFieldOrPropertyWithValue("zipcode", "12345")
                .hasFieldOrPropertyWithValue("phone", "010-1234-5678")
                .hasFieldOrPropertyWithValue("name", "Test User")
                .hasFieldOrPropertyWithValue("deliveryMemo", "Test Memo");

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressMapper, times(1)).toDomain(userAddressEntity);
    }

    @Test
    @DisplayName("ID로 주소 조회 - 주소 없음")
    void testGetById_notFound() {
        // given
        when(userAddressRepository.findById(addressId)).thenReturn(Optional.empty());

        // when & then
        assertThrows(IllegalArgumentException.class,
                () -> userAddressService.getById(addressId));

        verify(userAddressRepository, times(1)).findById(addressId);
        verify(userAddressMapper, never()).toDomain(any());
    }

    @Test
    @DisplayName("사용자 ID로 모든 주소 조회 - 성공")
    void testGetAllByUserId_success() {
        // given
        when(userAddressRepository.findAllByUserId(userId))
                .thenReturn(Collections.singletonList(userAddressEntity));
        when(userAddressMapper.toDomain(userAddressEntity))
                .thenReturn(userAddress);

        // when
        List<UserAddress> addresses = userAddressService.getAllByUserId(userId);

        // then
        assertThat(addresses)
                .isNotNull()
                .hasSize(1)
                .first()
                .hasFieldOrPropertyWithValue("id", addressId)
                .hasFieldOrPropertyWithValue("userId", userId)
                .hasFieldOrPropertyWithValue("address", "Test Address")
                .hasFieldOrPropertyWithValue("addressDetail", "Test Detail")
                .hasFieldOrPropertyWithValue("zipcode", "12345")
                .hasFieldOrPropertyWithValue("phone", "010-1234-5678")
                .hasFieldOrPropertyWithValue("name", "Test User")
                .hasFieldOrPropertyWithValue("deliveryMemo", "Test Memo");

        verify(userAddressRepository, times(1)).findAllByUserId(userId);
        verify(userAddressMapper, times(1)).toDomain(userAddressEntity);
    }
}