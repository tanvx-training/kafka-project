package dev.tanvx.wallet_service.domain.transaction.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TransactionCreateRequest {

  private final String senderId;

  private final String receiverId;

  private final BigDecimal amount;

  private final String transactionType;
}
