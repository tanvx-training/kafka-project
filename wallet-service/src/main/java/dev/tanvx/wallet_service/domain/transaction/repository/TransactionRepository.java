package dev.tanvx.wallet_service.domain.transaction.repository;

import dev.tanvx.wallet_service.domain.transaction.entity.Transaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

}
