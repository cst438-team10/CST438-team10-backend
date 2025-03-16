package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.GradeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class GradeController {
    @Autowired
    GradeRepository gradeRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    AssignmentRepository assignmentRepository;
    // instructor gets grades for assignment ordered by student name
    // user must be instructor for the section
    /**
     instructor lists the grades for an assignment for all enrolled students
     returns the list of grades (ordered by student name) for the assignment
     if there is no grade entity for an enrolled student, a grade entity with null grade is created
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/assignments/{assignmentId}/grades")
    public List<GradeDTO> getAssignmentGrades(@PathVariable("assignmentId") int assignmentId) {
        // get the list of enrollments for the section related to this assignment.
        // hint: use te enrollment repository method findEnrollmentsBySectionOrderByStudentName.
        // for each enrollment, get the grade related to the assignment and enrollment
        // hint: use the gradeRepository findByEnrollmentIdAndAssignmentId method.
        Assignment assignment = assignmentRepository.findById(assignmentId).orElse(null);
        if (assignment != null) {
            List<GradeDTO> grades = new ArrayList<>();
            Section section = assignment.getSection();
            List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(section.getSectionNo());
            for (Enrollment e : enrollments) {
                Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(e.getEnrollmentId(), assignmentId);
                grades.add(new GradeDTO(grade.getGradeId(), grade.getEnrollment().getUser().getName(),
                        grade.getEnrollment().getUser().getEmail(), grade.getAssignment().getTitle(), grade.getAssignment().getSection().getCourse().getCourseId(),
                        grade.getAssignment().getSection().getSecId(), grade.getScore()));
            }
            return grades;
        }
        return null;
    }

    // instructor uploads grades for assignment
    // user must be instructor for the section
    /**
     instructor updates one or more assignment grades
     only the score attribute of grade entity can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/grades")
    public void updateGrades(@RequestBody List<GradeDTO> dlist) {

        // for each grade in the GradeDTO list, retrieve the grade entity
        // update the score and save the entity
        for (GradeDTO g : dlist) {
            Grade grade = gradeRepository.findById(g.gradeId()).orElse(null);
            if (grade != null) {
                grade.setScore(g.score());
                gradeRepository.save(grade);
            }
        }
    }

}
