package dev.tanvx.fraud_service.domain.fraud.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
@Entity
@Table(name = "_fraud")
public class Fraud {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "transaction_id", nullable = false)
  private UUID transactionId;

  @Column(name = "is_fraud", nullable = false)
  private boolean isFraud;

  @Column(name = "fraud_reason", nullable = false)
  private String fraudReason;

  @Column(name = "detected_at", nullable = false)
  private LocalDateTime detectedAt;
}
