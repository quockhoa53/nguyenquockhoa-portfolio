package com.portfolio.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "admin_allowed_ips")
public class AdminAllowedIpEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "admin_id", nullable = false)
    private AdminUserEntity admin;

    @Column(name = "ip_address", nullable = false, length = 64)
    private String ipAddress;

    @Column(length = 255)
    private String description;

    protected AdminAllowedIpEntity() {}

    public AdminAllowedIpEntity(AdminUserEntity admin, String ipAddress, String description) {
        this.admin = admin;
        this.ipAddress = ipAddress;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public AdminUserEntity getAdmin() {
        return admin;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getDescription() {
        return description;
    }
}
