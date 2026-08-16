package com.portfolio.application.service;

import com.portfolio.application.port.in.SendContactMessageUseCase;
import com.portfolio.application.port.out.ContactMessagePort;
import com.portfolio.domain.model.ContactMessage;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService implements SendContactMessageUseCase {

    private final ContactMessagePort port;

    public ContactService(ContactMessagePort port) {
        this.port = port;
    }

    @Override
    @Transactional
    public ContactMessage send(Command command) {
        var message = new ContactMessage(
                null, command.name(), command.email(), command.subject(), command.message(), OffsetDateTime.now());
        return port.save(message);
    }
}
