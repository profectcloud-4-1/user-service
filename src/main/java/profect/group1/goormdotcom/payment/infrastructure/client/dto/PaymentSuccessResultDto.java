package profect.group1.goormdotcom.payment.infrastructure.client.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentSuccessResultDto(
        UUID userID,
        String status,
        Long amount,
        OffsetDateTime approvedAt
) {};