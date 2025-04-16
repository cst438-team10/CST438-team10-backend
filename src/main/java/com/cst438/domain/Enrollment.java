package com.cst438.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Enrollment {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="enrollment_id")
    int enrollmentId;
//    @OneToMany(mappedBy = "enrollment", cascade = CascadeType.REMOVE)

//    List<Grade> grades;
	// TODO complete this class
    // add additional attribute for grade
    String grade;
    // create relationship between enrollment and user entities
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User student;
    // create relationship between enrollment and section entities
    @ManyToOne
    @JoinColumn(name = "section_no")
    private Section section;

    // add getter/setter methods
    public int getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(int enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public User getUser() {
        return student;
    }

    public void setUser(User user) {
        this.student = user;
    }

    public Section getSection() {
        return section;
    }

    public void setSection(Section section) {
        this.section = section;
    }
}
