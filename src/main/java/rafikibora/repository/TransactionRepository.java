package rafikibora.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rafikibora.model.transactions.Transaction;

import java.util.Optional;

//extends crud methods
public interface TransactionRepository extends JpaRepository<Transaction,Integer> {
    Optional<Transaction> findById(Integer id);
}
