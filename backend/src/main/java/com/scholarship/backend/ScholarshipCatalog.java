

package com.scholarship.backend;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "scholarships")
public class ScholarshipCatalog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 150)
    private String provider;

    @Column(length = 500)
    private String eligibility;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    private LocalDate deadline;

    @Column(columnDefinition = "TEXT")
    private String description;

    public ScholarshipCatalog() {}

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getProvider() {
        return provider;
    }

    public String getEligibility() {
        return eligibility;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDate getDeadline() {
        return deadline;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setEligibility(String eligibility) {
        this.eligibility = eligibility;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setDeadline(LocalDate deadline) {
        this.deadline = deadline;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
