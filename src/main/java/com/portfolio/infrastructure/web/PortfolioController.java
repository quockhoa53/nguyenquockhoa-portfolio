package com.portfolio.infrastructure.web;

import com.portfolio.application.port.in.GetPortfolioUseCase;
import com.portfolio.application.port.in.SendContactMessageUseCase;
import com.portfolio.domain.model.ContactMessage;
import com.portfolio.domain.model.Portfolio;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class PortfolioController {

    private final GetPortfolioUseCase getPortfolio;
    private final SendContactMessageUseCase sendContact;

    public PortfolioController(GetPortfolioUseCase getPortfolio, SendContactMessageUseCase sendContact) {
        this.getPortfolio = getPortfolio;
        this.sendContact = sendContact;
    }

    @GetMapping("/portfolio")
    public Portfolio portfolio() {
        return getPortfolio.getPortfolio();
    }

    @GetMapping("/profile")
    public Portfolio.Profile profile() {
        return getPortfolio.getProfile();
    }

    @GetMapping("/skills")
    public List<Portfolio.Skill> skills() {
        return getPortfolio.getSkills();
    }

    @GetMapping("/experiences")
    public List<Portfolio.Experience> experiences() {
        return getPortfolio.getExperiences();
    }

    @GetMapping("/projects")
    public List<Portfolio.Project> projects() {
        return getPortfolio.getProjects();
    }

    @GetMapping("/projects/{id}")
    public Portfolio.Project project(@PathVariable long id) {
        return getPortfolio.getProject(id).orElseThrow(() -> new ProjectNotFoundException(id));
    }

    @PostMapping("/contact")
    @ResponseStatus(HttpStatus.CREATED)
    public ContactMessage contact(@Valid @RequestBody ContactRequest request) {
        var command = new SendContactMessageUseCase.Command(
                request.name(), request.email(), request.subject(), request.message());
        return sendContact.send(command);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static class ProjectNotFoundException extends RuntimeException {
        ProjectNotFoundException(long id) {
            super("Không tìm thấy dự án có id " + id);
        }
    }

    public record ContactRequest(
            @NotBlank @Size(max = 150) String name,
            @NotBlank @Email @Size(max = 255) String email,
            @NotBlank @Size(max = 255) String subject,
            @NotBlank @Size(max = 5000) String message) {}
}
