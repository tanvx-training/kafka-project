package dev.tanvx.wallet_service.kafka.producer;

import dev.tanvx.wallet_service.kafka.events.EventType;
import dev.tanvx.wallet_service.kafka.events.TransactionEvent;
import dev.tanvx.wallet_service.kafka.utils.MessageBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProducerImpl implements TransactionProducer {

  // <tenant_id>-<service_owner>-<topic_name>-<environment>
  private static final String TRANSACTION_TOPIC = "tanvx-wallet-transactions-dev";

  private static final String SERVICE = "tanvx-wallet";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Override
  public void send(TransactionEvent transactionEvent) {

    try {
      var message = MessageBuilder.build(
          SERVICE,
          EventType.EVENT,
          transactionEvent.getTransactionType().name(),
          transactionEvent
      );

      kafkaTemplate.send(TRANSACTION_TOPIC, message);
      log.info("Produced a message to topic: {}, value: {}", TRANSACTION_TOPIC, transactionEvent);
    } catch (Exception e) {
      log.error("Failed to produce the message to topic: " + TRANSACTION_TOPIC);
      e.printStackTrace();
    }
  }
}
