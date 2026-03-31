package com.library.loanservice.repository;

import com.library.loanservice.entity.Loan;
import com.library.loanservice.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findByStatus(LoanStatus status);
}