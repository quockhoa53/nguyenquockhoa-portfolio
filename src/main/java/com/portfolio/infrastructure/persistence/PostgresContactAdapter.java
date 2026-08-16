package com.portfolio.infrastructure.persistence;

import com.portfolio.application.port.out.ContactMessagePort;
import com.portfolio.domain.model.ContactMessage;
import com.portfolio.infrastructure.persistence.entity.ContactMessageEntity;
import com.portfolio.infrastructure.persistence.repository.ContactMessageJpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public class PostgresContactAdapter implements ContactMessagePort {
    private final ContactMessageJpaRepository messages;

    public PostgresContactAdapter(ContactMessageJpaRepository messages) {
        this.messages = messages;
    }

    @Override
    public ContactMessage save(ContactMessage message) {
        var saved = messages.save(new ContactMessageEntity(
                message.name(), message.email(), message.subject(), message.message(), message.createdAt()));
        return new ContactMessage(
                saved.getId(),
                saved.getName(),
                saved.getEmail(),
                saved.getSubject(),
                saved.getMessage(),
                saved.getCreatedAt());
    }
}
