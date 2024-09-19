package dev.tanvx.wallet_service.api;

import dev.tanvx.wallet_service.domain.transaction.dto.request.TransactionCreateRequest;
import dev.tanvx.wallet_service.domain.transaction.dto.response.TransactionCreateResponse;
import dev.tanvx.wallet_service.domain.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/transactions")
public class TransactionController {

  private final TransactionService transactionService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TransactionCreateResponse createTransaction(
      @RequestBody TransactionCreateRequest request) {

    return transactionService.processTransaction(request);
  }
}
