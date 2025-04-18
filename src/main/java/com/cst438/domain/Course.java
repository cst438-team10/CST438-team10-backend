package com.cst438.domain;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Course {
    @Id
    @Column(name="course_id")
    private String courseId;
    private String title;
    private int credits;
    @OneToMany(mappedBy="course", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Section> section;

    public List<Section> getSection() {
        return section;
    }

    public void setSection(List<Section> section) {
        this.section = section;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public int getCredits() {
        return credits;
    }
    public void setCredits(int credits) {
        this.credits = credits;
    }
    public String getCourseId() {
        return courseId;
    }
    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

}
