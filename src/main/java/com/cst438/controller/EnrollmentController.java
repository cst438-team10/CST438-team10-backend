package com.cst438.controller;


import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    /**
     instructor gets list of enrollments for a section
     list of enrollments returned is in order by student name
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {

        // TODO
		//  hint: use enrollment repository findEnrollmentsBySectionNoOrderByStudentName method
        //  remove the following line when done
      List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsBySectionNoOrderByStudentName(sectionNo);
      List<EnrollmentDTO> enrollmentDTOS_list = new ArrayList<>();
      for (Enrollment e: enrollments) {
          enrollmentDTOS_list.add(new EnrollmentDTO(e.getEnrollmentId(), e.getGrade(), e.getUser().getId(),
                  e.getUser().getName(), e.getUser().getEmail(), e.getSection().getCourse().getCourseId(),
                  e.getSection().getCourse().getTitle(), e.getSection().getSecId(), e.getSection().getSectionNo(),
                  e.getSection().getBuilding(), e.getSection().getRoom(), e.getSection().getTimes(),
                  e.getSection().getCourse().getCredits(), e.getSection().getTerm().getYear(), e.getSection().getTerm().getSemester()));
      }
      return enrollmentDTOS_list;
    }

    // instructor uploads enrollments with the final grades for the section
    // user must be instructor for the section
    /**
     instructor updates enrollment grades
     only the grade attribute of enrollment can be changed
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/enrollments")
    public void updateEnrollmentGrade(@RequestBody List<EnrollmentDTO> dlist) {

        // TODO
        // For each EnrollmentDTO in the list
        // find the Enrollment entity using enrollmentId
        // update the grade and save back to database
        for (EnrollmentDTO e : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(e.enrollmentId()).orElse(null);
            if (enrollment != null) {
                enrollment.setGrade(e.grade());
                enrollmentRepository.save(enrollment);
            }
        }
    }

}
