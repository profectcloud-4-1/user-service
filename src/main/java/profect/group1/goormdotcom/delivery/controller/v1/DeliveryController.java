package profect.group1.goormdotcom.delivery.controller.v1;

import jakarta.validation.Valid;
import java.util.UUID;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestParam;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.delivery.service.DeliveryService;
import profect.group1.goormdotcom.delivery.domain.Delivery;
import profect.group1.goormdotcom.delivery.domain.DeliveryReturn;
import profect.group1.goormdotcom.delivery.domain.DeliveryAddress;
import profect.group1.goormdotcom.delivery.controller.dto.response.CustomerAddressListResponseDto;
import profect.group1.goormdotcom.delivery.controller.dto.request.CreateAddressRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;

@RestController
@RequestMapping("/delivery")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class DeliveryController implements DeliveryApiDocs {

	private final DeliveryService service;

	@GetMapping("/address/mine")
	public ApiResponse<CustomerAddressListResponseDto> getMyAddresses(
		HttpServletRequest request
		) {
		Claims claims = (Claims) request.getAttribute("jwtClaims");
		UUID customerId = UUID.fromString(claims.getSubject());
		List<DeliveryAddress> addresses = this.service.getAddressesByCustomerId(customerId);
		return ApiResponse.onSuccess(CustomerAddressListResponseDto.of(addresses));
	}

	@PostMapping("/address/mine")
	public ApiResponse<DeliveryAddress> createMyAddress(
		@RequestBody @Valid CreateAddressRequestDto body,
		HttpServletRequest request
	) {
		Claims claims = (Claims) request.getAttribute("jwtClaims");
		UUID customerId = UUID.fromString(claims.getSubject());
		DeliveryAddress address = this.service.createCustomerAddress(customerId, body);
		return ApiResponse.onSuccess(address);
	}

    @PutMapping("/address/mine/{addressId}")
	public ApiResponse<DeliveryAddress> updateMyAddress(
		@RequestBody @Valid CreateAddressRequestDto body,
        HttpServletRequest request,
        @PathVariable UUID addressId
	) {
		Claims claims = (Claims) request.getAttribute("jwtClaims");
		UUID customerId = UUID.fromString(claims.getSubject());
        DeliveryAddress address = this.service.updateCustomerAddress(customerId, addressId, body);
		return ApiResponse.onSuccess(address);
	}

    @DeleteMapping("/address/mine/{addressId}")
	public ApiResponse<Boolean> deleteMyAddress(
        HttpServletRequest request,
        @PathVariable UUID addressId
	) {
		Claims claims = (Claims) request.getAttribute("jwtClaims");
		UUID customerId = UUID.fromString(claims.getSubject());
        boolean ok = this.service.deleteCustomerAddress(customerId, addressId);
		return ApiResponse.onSuccess(ok);
	}

	// ===== Brand Address (MASTER 전용) =====
    @GetMapping("/address/brand")
	@PreAuthorize("hasRole('MASTER')")
    public ApiResponse<List<DeliveryAddress>> getBrandAddresses(@RequestParam UUID brandId) {
        return ApiResponse.onSuccess(this.service.getBrandAddressesByBrandId(brandId));
	}

	@PostMapping("/address/brand")
	@PreAuthorize("hasRole('MASTER')")
    public ApiResponse<DeliveryAddress> createBrandAddress(
        @RequestBody @Valid CreateAddressRequestDto body
    ) {
        DeliveryAddress address = this.service.createBrandAddress(body);
		return ApiResponse.onSuccess(address);
	}

	@PutMapping("/address/brand/{addressId}")
	@PreAuthorize("hasRole('MASTER')")
	public ApiResponse<DeliveryAddress> updateBrandAddress(
		@PathVariable UUID addressId,
		@RequestBody @Valid CreateAddressRequestDto body
	) {
		DeliveryAddress address = this.service.updateBrandAddress(addressId, body);
		return ApiResponse.onSuccess(address);
	}

	@DeleteMapping("/address/brand/{addressId}")
	@PreAuthorize("hasRole('MASTER')")
	public ApiResponse<Boolean> deleteBrandAddress(@PathVariable UUID addressId) {
		boolean ok = this.service.deleteBrandAddress(addressId);
		return ApiResponse.onSuccess(ok);
	}
}
