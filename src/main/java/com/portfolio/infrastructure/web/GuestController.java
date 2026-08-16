package com.portfolio.infrastructure.web;

import com.portfolio.application.service.GuestIdentityService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guests")
public class GuestController {
    private final GuestIdentityService identities;

    public GuestController(GuestIdentityService identities) {
        this.identities = identities;
    }

    @PostMapping
    public GuestResponse register(@Valid @RequestBody GuestRequest body, HttpServletResponse response) {
        var guest = identities.register(body.displayName(), body.email(), response);
        return new GuestResponse(guest.getId().toString(), guest.getDisplayName(), guest.isEmailVerified());
    }

    public record GuestRequest(
            @NotBlank @Size(max = 150) String displayName, @NotBlank @Email @Size(max = 255) String email) {}

    public record GuestResponse(String id, String displayName, boolean emailVerified) {}
}
