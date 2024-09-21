package dev.tanvx.fraud_service.domain.fraud.repository;

import dev.tanvx.fraud_service.domain.fraud.entity.Fraud;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FraudRepository extends JpaRepository<Fraud, UUID> {

}
