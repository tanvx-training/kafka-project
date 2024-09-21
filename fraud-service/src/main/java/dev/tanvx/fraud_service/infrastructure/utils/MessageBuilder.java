package dev.tanvx.fraud_service.infrastructure.utils;


import dev.tanvx.fraud_service.infrastructure.dto.KafkaMessage;
import dev.tanvx.fraud_service.infrastructure.dto.MessageMeta;
import dev.tanvx.fraud_service.kafka.events.EventType;
import java.util.UUID;

public class MessageBuilder {

  // Constructor
  public MessageBuilder() {
    // Empty constructor, as the builder methods are static
  }

  // Method to build KafkaMessage with metadata
  public static <T> KafkaMessage<T> build(String serviceId, EventType eventType, String messageCode,
      T payload) {
    // Create new KafkaMessage object
    KafkaMessage<T> message = new KafkaMessage<>();

    // Create MessageMeta using the builder pattern
    MessageMeta meta = MessageMeta.builder()
        .messageId(generateMessageId())
        .serviceId(serviceId)
        .type(eventType)
        .timestamp(System.currentTimeMillis())
        .build();

    // Set meta and payload for the Kafka message
    message.setMeta(meta);
    message.setMessageCode(messageCode);
    message.setPayload(payload);

    // Return the built KafkaMessage object
    return message;
  }

  // Method to generate a random message ID using UUID
  public static String generateMessageId() {
    return UUID.randomUUID().toString().replace("_", "");
  }
}

