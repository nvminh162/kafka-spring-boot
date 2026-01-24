package com.example.account.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.model.Statistic;

@Repository
public interface StatisticRepository extends JpaRepository<Statistic, Integer> {
    List<Statistic> findByStatus(boolean status);
}
