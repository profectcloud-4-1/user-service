package profect.group1.goormdotcom.payment.controller.v1;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import profect.group1.goormdotcom.apiPayload.ApiResponse;
import profect.group1.goormdotcom.payment.controller.dto.PaymentResponseDto;
import profect.group1.goormdotcom.payment.controller.dto.request.PaymentCreateRequestDto;
import profect.group1.goormdotcom.payment.controller.dto.request.PaymentFailRequestDto;
import profect.group1.goormdotcom.payment.controller.dto.request.PaymentSuccessRequestDto;
import profect.group1.goormdotcom.payment.controller.mapper.PaymentDtoMapper;
import profect.group1.goormdotcom.payment.domain.Payment;
import profect.group1.goormdotcom.payment.service.PaymentService;
import profect.group1.goormdotcom.user.domain.User;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController implements PaymentApiDocs {
    private final PaymentService paymentService;

    @Override
    @PostMapping
    public ApiResponse<PaymentResponseDto> requestPayment(@AuthenticationPrincipal User user, @RequestBody @Valid PaymentCreateRequestDto paymentRequestDto) {

        Payment payment = paymentService.requestPayment(paymentRequestDto, user);
        return ApiResponse.onSuccess(PaymentDtoMapper.toPaymentDto(payment));
    }

    @Override
    @GetMapping("/toss/success")
    public ApiResponse<PaymentResponseDto> tossPaymentSuccess(@ModelAttribute @Valid PaymentSuccessRequestDto paymentSuccessRequestDto) {
        Payment payment = paymentService.tossPaymentSuccess(paymentSuccessRequestDto);
        return ApiResponse.onSuccess(PaymentDtoMapper.toPaymentDto(payment));
    }

    @Override
    @GetMapping("/toss/fail")
    public ApiResponse<Void> tossPaymentFail(@ModelAttribute @Valid PaymentFailRequestDto paymentFailRequestDto) {
        paymentService.tossPaymentFail(paymentFailRequestDto);
        return ApiResponse.onSuccess(null);
    }
}
