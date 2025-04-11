package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentStudentDTO;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class StudentController {
    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    GradeRepository gradeRepository;

    /**
     students lists there assignments given year and semester value
     returns list of assignments may be empty
     logged in user must be the student (assignment 7)
     */
    @GetMapping("/assignments")
    public List<AssignmentStudentDTO> getStudentAssignments(
            @RequestParam("studentId") int studentId,
            @RequestParam("year") int year,
            @RequestParam("semester") String semester) {

        // return a list of assignments and (if they exist) the assignment grade
        //  for all sections that the student is enrolled for the given year and semester
        //  hint: use the assignment repository method findByStudentIdAndYearAndSemesterOrderByDueDate
        List<Assignment> assignments = assignmentRepository.findByStudentIdAndYearAndSemesterOrderByDueDate(studentId, year, semester);
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, studentId);

        List<AssignmentStudentDTO> assignmentDTOs = new ArrayList<>();
        for (Assignment assignment : assignments) {
                for (Enrollment enrollment : enrollments) {
                    //List<Grade> grades = (List<Grade>) gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignment.getAssignmentId());
                    //for (Grade grade : grades) {
                    Grade grade = gradeRepository.findByEnrollmentIdAndAssignmentId(enrollment.getEnrollmentId(), assignment.getAssignmentId());
                    Integer score = (grade != null) ? grade.getScore() : null;
                    AssignmentStudentDTO dto = new AssignmentStudentDTO(
                            assignment.getAssignmentId(),
                            assignment.getTitle(),
                            assignment.getDueDate(),
                            enrollment.getSection().getCourse().getCourseId(),
                            assignment.getSection().getSecId(),
                            score
                    );
                    assignmentDTOs.add(dto);
                //}

                }

        }
        return assignmentDTOs;
    }
}