package dev.tanvx.wallet_service.domain.transaction.entity;

import dev.tanvx.wallet_service.domain.user.entity.User;
import dev.tanvx.wallet_service.infrastructure.enums.TransactionStatus;
import dev.tanvx.wallet_service.infrastructure.enums.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "_transaction")
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "sender_user_id")
  private User sender;

  @ManyToOne
  @JoinColumn(name = "receiver_user_id")
  private User receiver;

  @Column(name = "amount", nullable = false)
  private BigDecimal amount;

  @Column(name = "timestamp", nullable = false)
  private LocalDateTime timestamp;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false)
  private TransactionType transactionType;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_status", nullable = false)
  private TransactionStatus transactionStatus;
}
