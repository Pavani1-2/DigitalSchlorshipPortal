package com.scholarship.backend;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/scholarships")
@CrossOrigin(origins = "*")
public class ScholarshipController {

    private final ScholarshipRepository repository;

    public ScholarshipController(ScholarshipRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Scholarship create(@RequestBody Scholarship scholarship) {
        scholarship.setId(null);
        return repository.save(scholarship);
    }

    @GetMapping
    public List<Scholarship> getAll() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Scholarship> getById(@PathVariable Long id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Scholarship> update(
            @PathVariable Long id,
            @RequestBody Scholarship updated) {

        return repository.findById(id).map(existing -> {
            existing.setStudentName(updated.getStudentName());
            existing.setEmail(updated.getEmail());
            existing.setScholarshipName(updated.getScholarshipName());
            existing.setCollegeName(updated.getCollegeName());
            existing.setAmount(updated.getAmount());
            existing.setStatus(updated.getStatus());
            existing.setDescription(updated.getDescription());
            existing.setEligibilityCriteria(updated.getEligibilityCriteria());

            return ResponseEntity.ok(repository.save(existing));
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}