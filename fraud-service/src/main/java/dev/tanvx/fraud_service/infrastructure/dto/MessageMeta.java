package dev.tanvx.fraud_service.infrastructure.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import dev.tanvx.fraud_service.kafka.events.EventType;
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
