package dev.tanvx.wallet_service.domain.transaction.producer;

import dev.tanvx.wallet_service.domain.transaction.dto.response.TransactionCreateResponse;

public interface TransactionProducer {

  void send(TransactionCreateResponse transactionCreateResponse);
}
