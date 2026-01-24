package com.example.account.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.model.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {
    List<Message> findByStatus(boolean status);
}
