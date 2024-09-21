package dev.tanvx.wallet_service.kafka.message;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.tanvx.wallet_service.kafka.events.EventType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class MessageMeta {

  private String messageId;

  private String originalMessageId;

  private EventType type;

  private String serviceId;

  private long timestamp;

  private boolean autoRetry;
}
