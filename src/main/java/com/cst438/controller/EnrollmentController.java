package com.cst438.controller;


import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.service.RegistrarServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class EnrollmentController {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    RegistrarServiceProxy registrarServiceProxy;
    /**
     instructor gets list of enrollments for a section
     list of enrollments returned is in order by student name
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{sectionNo}/enrollments")
    public List<EnrollmentDTO> getEnrollments(
            @PathVariable("sectionNo") int sectionNo ) {

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

        for (EnrollmentDTO e : dlist) {
            Enrollment enrollment = enrollmentRepository.findById(e.enrollmentId()).
                    orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more enrollments were not found"));
            enrollment.setGrade(e.grade());
            enrollmentRepository.save(enrollment);
            registrarServiceProxy.updateEnrollment(enrollment.getEnrollmentId());
        }
    }

    @PostMapping("/sections/{sectionNo}/enrollments")
    public EnrollmentDTO createEnrollment(@PathVariable("sectionNo") int sectionNo,
            @RequestBody EnrollmentDTO enrollmentDTO) {

        Section section = sectionRepository.findById(sectionNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));
        User student = userRepository.findById(enrollmentDTO.studentId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student with such ID not found"));;

        Date currentDate = new Date();
        System.out.println(currentDate);
        if(currentDate.after(section.getTerm().getAddDate()) &&
                currentDate.before(section.getTerm().getAddDeadline())) {
            Enrollment enrollment = new Enrollment();
            enrollment.setUser(student);
            enrollment.setSection(section);
            enrollment.setGrade(enrollmentDTO.grade());
            enrollment = enrollmentRepository.save(enrollment);

            EnrollmentDTO savedEnrollmentDTO = new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    student.getId(),
                    student.getName(),
                    student.getEmail(),
                    section.getCourse().getCourseId(),
                    section.getCourse().getTitle(),
                    section.getSecId(),
                    section.getSectionNo(),
                    section.getBuilding(),
                    section.getRoom(),
                    section.getTimes(),
                    section.getCourse().getCredits(),
                    section.getTerm().getYear(),
                    section.getTerm().getSemester()
            );
            return savedEnrollmentDTO;
        } else {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Enrollment period is closed");
        }
    }

    @DeleteMapping("/enrollment/{enrollmentId}/student/{studentId}")
    public void deleteEnrollment(@PathVariable("enrollmentId") int enrollmentId,
                                 @PathVariable("studentId") int studentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        Section section = sectionRepository.findById(enrollment.getSection().getSectionNo()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        Date currentDate = new Date();
        if (currentDate.before(section.getTerm().getDropDeadline())){
            enrollmentRepository.delete(enrollment);
        }else{
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Drop deadline period is closed");
        }
    }

}
