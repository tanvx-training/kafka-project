package dev.tanvx.wallet_service.domain.transaction.service.impl;

import dev.tanvx.wallet_service.domain.transaction.dto.request.TransactionCreateRequest;
import dev.tanvx.wallet_service.domain.transaction.dto.response.TransactionCreateResponse;
import dev.tanvx.wallet_service.domain.transaction.repository.TransactionRepository;
import dev.tanvx.wallet_service.domain.transaction.service.TransactionService;
import dev.tanvx.wallet_service.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

  private final TransactionRepository transactionRepository;

  private final UserRepository userRepository;

  @Override
  public TransactionCreateResponse processTransaction(TransactionCreateRequest request) {
    return null;
  }
}
