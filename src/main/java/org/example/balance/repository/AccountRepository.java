package org.example.balance.repository;

import jakarta.persistence.LockModeType;
import org.example.balance.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)

    Optional<Account> findById(UUID id);
}
