package com.library.loanservice.repository;

import com.library.loanservice.entity.LoanEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanEventRepository extends JpaRepository<LoanEvent, Long> {
    List<LoanEvent> findByLoanIdOrderByTimestampAsc(Long loanId);
}