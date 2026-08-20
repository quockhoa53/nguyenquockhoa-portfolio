package com.portfolio.application.service;

import com.portfolio.application.port.in.SendContactMessageUseCase;
import com.portfolio.application.port.out.ContactMessagePort;
import com.portfolio.domain.model.ContactMessage;
import com.portfolio.infrastructure.mail.EmailNotificationService;
import java.time.OffsetDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactService implements SendContactMessageUseCase {

    private final ContactMessagePort port;
    private final EmailNotificationService emailService;

    public ContactService(ContactMessagePort port, EmailNotificationService emailService) {
        this.port = port;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public ContactMessage send(Command command) {
        var message = new ContactMessage(
                null, command.name(), command.email(), command.subject(), command.message(), OffsetDateTime.now());
        var savedMessage = port.save(message);
        emailService.sendContactNotification(savedMessage);
        return savedMessage;
    }
}
