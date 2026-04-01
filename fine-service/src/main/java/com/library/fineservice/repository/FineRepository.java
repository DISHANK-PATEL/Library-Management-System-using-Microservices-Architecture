package com.library.fineservice.repository;

import com.library.fineservice.entity.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {
    Optional<Fine> findByLoanId(Long loanId);
}