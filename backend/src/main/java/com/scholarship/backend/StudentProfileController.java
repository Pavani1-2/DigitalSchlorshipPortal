package com.scholarship.backend;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/students")
@CrossOrigin(origins = "http://localhost:5173")
public class StudentProfileController {
    private final StudentProfileRepository repository;
    public StudentProfileController(StudentProfileRepository repository){this.repository=repository;}

    @GetMapping public List<StudentProfile> all(){return repository.findAll();}
    @GetMapping("/{id}") public StudentProfile one(@PathVariable Long id){
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Student not found: "+id));
    }
    @PostMapping public StudentProfile create(@RequestBody StudentProfile student){
        student.setId(null); return repository.save(student);
    }
    @PutMapping("/{id}") public StudentProfile update(@PathVariable Long id,@RequestBody StudentProfile input){
        StudentProfile s=repository.findById(id).orElseThrow(() -> new RuntimeException("Student not found: "+id));
        s.setFullName(input.getFullName()); s.setEmail(input.getEmail()); s.setCollegeName(input.getCollegeName());
        s.setState(input.getState()); s.setCategory(input.getCategory()); s.setPercentage(input.getPercentage());
        s.setAnnualFamilyIncome(input.getAnnualFamilyIncome()); s.setCourse(input.getCourse()); s.setStudyYear(input.getStudyYear());
        return repository.save(s);
    }
    @DeleteMapping("/{id}") public void delete(@PathVariable Long id){repository.deleteById(id);}
}
