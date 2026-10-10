package com.scholarship.backend;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
@CrossOrigin(origins = "http://localhost:5173")
public class ScholarshipApplicationController {
    private final ScholarshipApplicationRepository repository;
    public ScholarshipApplicationController(ScholarshipApplicationRepository repository){this.repository=repository;}

    @GetMapping public List<ScholarshipApplication> all(){return repository.findAll();}
    @PostMapping public ScholarshipApplication create(@RequestBody ScholarshipApplication a){
        a.setId(null); a.setStatus("Submitted"); a.setSubmittedAt(null); return repository.save(a);
    }
    @PutMapping("/{id}/status") public ScholarshipApplication status(@PathVariable Long id,@RequestBody StatusUpdate update){
        ScholarshipApplication a=repository.findById(id).orElseThrow(() -> new RuntimeException("Application not found: "+id));
        a.setStatus(update.getStatus()); return repository.save(a);
    }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id){repository.deleteById(id);}
    public static class StatusUpdate {
        private String status;
        public String getStatus(){return status;} public void setStatus(String status){this.status=status;}
    }
}
