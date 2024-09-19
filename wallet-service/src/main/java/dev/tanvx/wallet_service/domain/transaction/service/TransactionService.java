package dev.tanvx.wallet_service.domain.transaction.service;

import dev.tanvx.wallet_service.domain.transaction.dto.request.TransactionCreateRequest;
import dev.tanvx.wallet_service.domain.transaction.dto.response.TransactionCreateResponse;

public interface TransactionService {

  TransactionCreateResponse processTransaction(TransactionCreateRequest request);
}
