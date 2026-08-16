package com.portfolio.application.port.out;

import com.portfolio.domain.model.ContactMessage;

public interface ContactMessagePort {
    ContactMessage save(ContactMessage message);
}
