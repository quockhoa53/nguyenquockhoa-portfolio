package com.portfolio.application.port.in;

import com.portfolio.domain.model.ContactMessage;

public interface SendContactMessageUseCase {
    ContactMessage send(Command command);

    record Command(String name, String email, String subject, String message) {}
}
