package com.kmovie.repository;

import com.kmovie.entity.EmailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmailMessageRepository extends JpaRepository<EmailMessage, UUID> {
}
