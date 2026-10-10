package com.scholarship.backend;

import jakarta.persistence.*;

@Entity
@Table(name = "student_profiles")
public class StudentProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false) private String fullName;
    @Column(nullable = false) private String email;
    private String collegeName;
    private String state;
    private String category;
    private Double percentage;
    private Double annualFamilyIncome;
    private String course;
    private Integer studyYear;

    public StudentProfile() {}
    public Long getId(){ return id; } public void setId(Long id){this.id=id;}
    public String getFullName(){return fullName;} public void setFullName(String v){fullName=v;}
    public String getEmail(){return email;} public void setEmail(String v){email=v;}
    public String getCollegeName(){return collegeName;} public void setCollegeName(String v){collegeName=v;}
    public String getState(){return state;} public void setState(String v){state=v;}
    public String getCategory(){return category;} public void setCategory(String v){category=v;}
    public Double getPercentage(){return percentage;} public void setPercentage(Double v){percentage=v;}
    public Double getAnnualFamilyIncome(){return annualFamilyIncome;} public void setAnnualFamilyIncome(Double v){annualFamilyIncome=v;}
    public String getCourse(){return course;} public void setCourse(String v){course=v;}
    public Integer getStudyYear(){return studyYear;} public void setStudyYear(Integer v){studyYear=v;}
}
