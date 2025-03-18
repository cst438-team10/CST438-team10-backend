package com.cst438.controller;

import com.cst438.domain.*;
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
public class StudentScheduleController {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     students lists their transcript containing all enrollments
     returns list of enrollments in chronological order
     logged in user must be the student (assignment 7)
     example URL  /transcript?studentId=19803
     */
    @GetMapping("/transcripts")
    public List<EnrollmentDTO> getTranscript(@RequestParam("studentId") int studentId) {

        // TODOdone


        // list course_id, sec_id, title, credit, grade
        // hint: use enrollment repository method findEnrollmentByStudentIdOrderByTermId
        // remove the following line when done

        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(studentId);
        List<EnrollmentDTO> dtos = new ArrayList<>();
        for (Enrollment e : enrollments) {
            Section s = e.getSection();
            Term term = s.getTerm();
            Course course = s.getCourse();
            User student = e.getUser();
            EnrollmentDTO dto = new EnrollmentDTO(e.getEnrollmentId(), e.getGrade(), student.getId(), student.getName(),
                    student.getEmail(), course.getCourseId(), course.getTitle(), s.getSecId(),
                    s.getSectionNo(), s.getBuilding(), s.getRoom(), s.getTimes(), course.getCredits(), term.getYear(), term.getSemester());
            dtos.add(dto);
        }

        return dtos;
    }


    /**
     students enrolls into a section of a course
     returns the enrollment data including primary key
     logged in user must be the student (assignment 7)
     */
    @PostMapping("/enrollments/sections/{sectionNo}")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            @RequestParam("studentId") int studentId ) {

        // TODOdone

        // check that the Section entity with primary key sectionNo exists
        Optional<Section> sectionOpt = sectionRepository.findById(sectionNo);
        if (sectionOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "section not found");
        }
        Section section = sectionOpt.get();
        Term term = section.getTerm();

        // check that today is between addDate and addDeadline for the section
        Date currentDate = new Date(System.currentTimeMillis());
        if (currentDate.before(term.getAddDate()) || currentDate.after(term.getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enrollment closed");
        }

        // check that student is not already enrolled into this section
        Enrollment existing = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, studentId);
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "student already enrolled in this section");
        }

        // create a new enrollment entity and save.  The enrollment grade will
        // be NULL until instructor enters final grades for the course.

        Optional<User> userOpt = userRepository.findById(studentId);
        if (userOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "student not found");
        }
        User student = userOpt.get();

        Enrollment enrollment = new Enrollment();
        enrollment.setSection(section);
        enrollment.setUser(student);
        enrollment.setGrade(null);
        enrollment = enrollmentRepository.save(enrollment);

        Course course = section.getCourse();
        EnrollmentDTO dto = new EnrollmentDTO(
                enrollment.getEnrollmentId(), enrollment.getGrade(), student.getId(), student.getName(), student.getEmail(),
                course.getCourseId(), course.getTitle(), section.getSecId(), section.getSectionNo(), section.getBuilding(),
                section.getRoom(), section.getTimes(), course.getCredits(), term.getYear(), term.getSemester()
        );

        return dto;

    }


    /**
     students drops an enrollment for a section
     logged in user must be the student (assignment 7)
     */
    @DeleteMapping("/enrollments/{enrollmentId}")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId) {

        // TODOdone
        // check that today is not after the dropDeadline for section
        Optional<Enrollment> enrollmentOpt = enrollmentRepository.findById(enrollmentId);
        if (enrollmentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "enrollment not found");
        }
        Enrollment enrollment = enrollmentOpt.get();
        Section section = enrollment.getSection();
        Term term = section.getTerm();

        Date currentDate = new Date(System.currentTimeMillis());
        if (currentDate.after(term.getDropDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "drop deadline has passed");
        }

        // delete the enrollment entity
        enrollmentRepository.delete(enrollment);
    }


}
