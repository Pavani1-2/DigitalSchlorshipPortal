
package com.scholarship.backend;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/catalog")
@CrossOrigin(origins = "http://localhost:5173")
public class ScholarshipCatalogController {

    private final ScholarshipCatalogRepository repository;

    public ScholarshipCatalogController(
            ScholarshipCatalogRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/scholarships")
    public List<ScholarshipCatalog> getAllScholarships() {
        return repository.findAll();
    }
}

