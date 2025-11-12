package profect.group1.goormdotcom.user.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import java.util.UUID;
// import profect.group1.goormdotcom.common.config.FeignAuthConfig;
// import profect.group1.goormdotcom.common.config.FeignRetryConfig;
import org.springframework.http.HttpHeaders;
import profect.group1.goormdotcom.apiPayload.ApiResponse;

@FeignClient(
		name = "order",
        url = "${service.order.url}",
		fallback = CartClientFallBack.class
)
public interface CartClient {

	@PostMapping("/internal/v1/carts")
	ApiResponse<UUID> create(@RequestHeader UUID userId);
}
