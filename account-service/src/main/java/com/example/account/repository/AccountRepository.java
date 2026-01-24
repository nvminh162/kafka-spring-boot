package com.example.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.model.Account;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {}
