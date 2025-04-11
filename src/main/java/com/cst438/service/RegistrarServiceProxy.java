package com.cst438.service;

import com.cst438.domain.*;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.dto.UserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class RegistrarServiceProxy {

    Queue registrarServiceQueue = new Queue("registrar_service", true);
    @Autowired
    CourseRepository courseRepository;
    @Autowired
    SectionRepository sectionRepository;
    @Autowired
    TermRepository termRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EnrollmentRepository enrollmentRepository;


    @Bean
    public Queue createQueue() {
        return new Queue("gradebook_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    public void updateEnrollment(int enrollmentId){
        sendMessage("updateEnrollment "+enrollmentId);
    }


    @RabbitListener(queues = "gradebook_service")
    public void receiveFromRegistrar(String message)  {
        /// receiving messages from registrar service
        try {
            // debugging
            System.out.println("receiving messages from registrar "+ message);
            String[] parts = message.split(" ", 2);

            if (parts[0].equals("addCourse")){
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course course = new Course();
                course.setCourseId(dto.courseId());
                course.setTitle(dto.title());
                course.setCredits(dto.credits());
                courseRepository.save(course);
            } else if (parts[0].equals("deleteCourse)")) {
                courseRepository.deleteById(parts[1]);
            } else if (parts[0].equals("updateCourse")) {
                CourseDTO dto = fromJsonString(parts[1], CourseDTO.class);
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course == null){
                    System.out.println("Error receivedFromRegistrar: course not found");
                } else {
                    course.setTitle(dto.title());
                    course.setCredits(dto.credits());
                    courseRepository.save(course);
                }
            } else if (parts[0].equals("addSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course  == null){
                    System.out.println("Error receivedFromRegistrar: course not found");
                } else {
                    Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                    Section section = new Section();
                    section.setSectionNo(dto.secNo());
                    section.setTerm(term);
                    section.setCourse(course);
                    section.setSecId(dto.secId());
                    section.setBuilding(dto.building());
                    section.setRoom(dto.room());
                    section.setTimes(dto.times());
                    section.setInstructor_email(dto.instructorEmail());
                    sectionRepository.save(section);
                }
            } else if (parts[0].equals("deleteSection")) {
                sectionRepository.deleteById(Integer.parseInt(parts[1]));
            } else if (parts[0].equals("updateSection")) {
                SectionDTO dto = fromJsonString(parts[1], SectionDTO.class);
                Course course = courseRepository.findById(dto.courseId()).orElse(null);
                if (course  == null){
                    System.out.println("Error receivedFromRegistrar: course not found");
                }else {
                    Term term = termRepository.findByYearAndSemester(dto.year(), dto.semester());
                    Section section = sectionRepository.findById(dto.secNo()).orElse(null);
                    if (section == null){
                        System.out.println("Error receivedFromRegistrar: no such section");
                    }else{
                        section.setSectionNo(dto.secNo());
                        section.setTerm(term);
                        section.setCourse(course);
                        section.setSecId(dto.secId());
                        section.setBuilding(dto.building());
                        section.setRoom(dto.room());
                        section.setTimes(dto.times());
                        section.setInstructor_email(dto.instructorEmail());
                        sectionRepository.save(section);
                    }
                }
            } else if (parts[0].equals("addUser")) {
                User user = new User();
                UserDTO udto = fromJsonString(parts[1], UserDTO.class);
                user.setId(udto.id());
                user.setName(udto.name());
                user.setEmail(udto.email());
                user.setType(udto.type());
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                user.setPassword(encoder.encode(udto.name()+"2024"));
                userRepository.save(user);
            }
            else if (parts[0].equals("deleteUser")) {
                userRepository.deleteById(Integer.parseInt(parts[1]));
            }
            else if (parts[0].equals("updateUser")) {
                User user = userRepository.findById(Integer.parseInt(parts[1])).orElse(null);
                if (user == null) {
                    System.out.println("Error receivedFromRegistrar: user not found");
                }
                UserDTO udto = fromJsonString(parts[1], UserDTO.class);
                assert user != null;
                user.setId(udto.id());
                user.setName(udto.name());
                user.setEmail(udto.email());
                user.setType(udto.type());
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                user.setPassword(encoder.encode(udto.name()+"2024"));
                userRepository.save(user);
            } else if (parts[0].equals("createEnrollment")){
                Enrollment enrollment = new Enrollment();
                EnrollmentDTO edto = fromJsonString(parts[1], EnrollmentDTO.class);
                User user = userRepository.findById(edto.studentId()).orElse(null);
                Date currentDate = new Date();
                Section section = sectionRepository.findById(edto.sectionNo()).orElse(null);
                if (currentDate.after(section.getTerm().getAddDate()) && currentDate.before(section.getTerm().getAddDeadline()) && user!=null && section != null) {
                    // save the id individually for consistency between databases
                    enrollment.setEnrollmentId(edto.enrollmentId());
                    enrollment.setGrade(edto.grade());
                    enrollment.setUser(user);
                    enrollment.setSection(section);
                    enrollmentRepository.save(enrollment);
                }else{
                    System.out.println("Error receivedFromRegistrar: issue creating enrollment");
                }
            } else if (parts[0].equals("deleteEnrollment")) {
                enrollmentRepository.deleteById(Integer.parseInt(parts[1]));
            }
        } catch(Exception e) {
            System.out.println("Exception in receivedFromRegistrar: "+e.getMessage());
        }
    }
    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(registrarServiceQueue.getName(), s);
    }
    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private static <T> T  fromJsonString(String str, Class<T> valueType ) {
        try {
            return new ObjectMapper().readValue(str, valueType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}