package dev.tanvx.wallet_service.kafka.producer;

import dev.tanvx.wallet_service.kafka.events.TransactionEvent;

public interface TransactionProducer {

  void send(TransactionEvent transactionEvent);
}
