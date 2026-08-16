package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "skills")
public class SkillEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private int proficiency;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    protected SkillEntity() {}

    public SkillEntity(String name, String category, int proficiency, int displayOrder) {
        update(name, category, proficiency, displayOrder);
    }

    public void update(String name, String category, int proficiency, int displayOrder) {
        this.name = name;
        this.category = category;
        this.proficiency = proficiency;
        this.displayOrder = displayOrder;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public int getProficiency() {
        return proficiency;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }
}
