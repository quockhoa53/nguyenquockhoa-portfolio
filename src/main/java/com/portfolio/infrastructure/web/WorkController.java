package com.portfolio.infrastructure.web;

import com.portfolio.infrastructure.persistence.entity.WorkItemEntity;
import com.portfolio.infrastructure.persistence.repository.WorkItemJpaRepository;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/work-items")
public class WorkController {
    private final WorkItemJpaRepository workItems;

    public WorkController(WorkItemJpaRepository workItems) {
        this.workItems = workItems;
    }

    @GetMapping
    @Cacheable("work_items")
    public List<WorkResponse> list() {
        return workItems.findByPublishedTrueOrderByDisplayOrderAscIdAsc().stream()
                .map(this::response)
                .toList();
    }

    @GetMapping("/{slugOrId}")
    @Cacheable(value = "work_item_detail", key = "#slugOrId")
    public WorkResponse detail(@PathVariable String slugOrId) {
        return workItems
                .findBySlugAndPublishedTrue(slugOrId)
                .or(() -> {
                    try {
                        long id = Long.parseLong(slugOrId);
                        return workItems.findById(id).filter(WorkItemEntity::isPublished);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .map(this::response)
                .orElseThrow(WorkItemNotFoundException::new);
    }

    private WorkResponse response(WorkItemEntity item) {
        List<String> techList = List.of();
        if (item.getTechnologies() != null && !item.getTechnologies().isBlank()) {
            techList = Arrays.stream(item.getTechnologies().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .toList();
        }

        return new WorkResponse(
                item.getId(),
                item.getSlug(),
                item.getPeriod(),
                item.getRole(),
                item.getCompany(),
                item.getTitle(),
                item.getSummary(),
                item.getContent(),
                techList,
                item.getDisplayOrder(),
                item.isPublished());
    }

    public record WorkResponse(
            Long id,
            String slug,
            String period,
            String role,
            String company,
            String title,
            String summary,
            String content,
            List<String> technologies,
            int displayOrder,
            boolean published) {}

    @ResponseStatus(HttpStatus.NOT_FOUND)
    static class WorkItemNotFoundException extends RuntimeException {}
}
