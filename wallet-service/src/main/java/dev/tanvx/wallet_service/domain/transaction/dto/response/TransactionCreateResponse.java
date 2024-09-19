package dev.tanvx.wallet_service.domain.transaction.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.tanvx.wallet_service.infrastructure.enums.TransactionStatus;
import dev.tanvx.wallet_service.infrastructure.enums.TransactionType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionCreateResponse {

  private final UUID transactionId;

  private final UUID senderId;

  private final UUID receiverId;

  private final BigDecimal amount;

  private final TransactionType transactionType;

  private final TransactionStatus transactionStatus;

  private final LocalDateTime timestamp;
}
