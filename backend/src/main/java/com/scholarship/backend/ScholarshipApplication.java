package com.scholarship.backend;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scholarship_applications")
public class ScholarshipApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long studentId;
    @Column(nullable=false) private String studentName;
    @Column(nullable=false) private String email;
    private Long scholarshipId;
    @Column(nullable=false) private String scholarshipName;
    @Column(nullable=false) private String status = "Submitted";
    private LocalDateTime submittedAt;

    @PrePersist public void onCreate(){ if(submittedAt==null) submittedAt=LocalDateTime.now(); if(status==null||status.isBlank())status="Submitted"; }
    public ScholarshipApplication(){}
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getStudentId(){return studentId;} public void setStudentId(Long v){studentId=v;}
    public String getStudentName(){return studentName;} public void setStudentName(String v){studentName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public Long getScholarshipId(){return scholarshipId;} public void setScholarshipId(Long v){scholarshipId=v;}
    public String getScholarshipName(){return scholarshipName;} public void setScholarshipName(String v){scholarshipName=v;}
    public String getStatus(){return status;} public void setStatus(String v){status=v;}
    public LocalDateTime getSubmittedAt(){return submittedAt;} public void setSubmittedAt(LocalDateTime v){submittedAt=v;}
}
