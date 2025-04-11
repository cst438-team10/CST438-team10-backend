package com.cst438.Service;

import com.cst438.domain.Enrollment;
import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.CourseDTO;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {

    @Autowired
    EnrollmentRepository enrollmentRepository;
    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    public void addCourse(CourseDTO course){
        sendMessage("addCourse "+asJsonString(course));
    }
    public void updateCourse(CourseDTO course){
        sendMessage("updateCourse "+asJsonString(course));
    }
    public void deleteCourse(String courseId){
        sendMessage("deleteCourse "+courseId);
    }

    public void addSection(SectionDTO section){
        sendMessage("addSection "+section);
    }
    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message)  {
        //receive message from Gradebook service
        try {
            System.out.println("receive from Gradebook " + message);
            String[] parts = message.split(" ", 2);
            if (parts[0].equals("updateEnrollment")) {
                EnrollmentDTO dto = fromJsonString(parts[1], EnrollmentDTO.class);
                Enrollment e = enrollmentRepository.findById(dto.enrollmentId()).orElse(null);
                if (e == null) {
                    System.out.println("Error receiveFromGradebook: Enrollment not found " + dto.enrollmentId());
                } else {
                    e.setGrade(dto.grade());
                    enrollmentRepository.save(e);
                }
            }
        } catch (Exception e) {
            System.out.println("Exception in receivedFromGradebook: "+ e.getMessage());
        }
    }


    /**
     * o whenever a course, section, user, enrollment entity is created, deleted or updated, a
     * message is sent to the queue for the gradebook service
     * o receive messages from the gradebook service when a final grade is posted to an
     * enrollment. The grade should be updated in the Enrollment entity.
     * o You will have to modify the controller classes to call methods on the
     * GradebookServiceProxy as well as complete the implementation of the proxy class.
     * o Use try-catch when receiving a message to avoid an exception that would result in an
     * infinite loop (an exception will put the message back to the queue, only to be received
     * again and repeat the error)
     * @param s
     */
    private void sendMessage(String s) {
        rabbitTemplate.convertAndSend(gradebookServiceQueue.getName(), s);
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