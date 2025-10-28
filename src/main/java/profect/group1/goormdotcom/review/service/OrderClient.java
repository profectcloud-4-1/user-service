package profect.group1.goormdotcom.review.service;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "order-service", url = "${order.service.url}")
public interface OrderClient {
    /**
     * userId + productId로 orderId 조회
     */
    @GetMapping("/api/v1/orders/search")
    UUID getOrderIdByUserAndProduct(@RequestParam("userId") UUID userId,
                                    @RequestParam("productId") UUID productId);

}
