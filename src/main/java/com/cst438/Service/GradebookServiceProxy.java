package com.cst438.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

@Service
public class GradebookServiceProxy {

    Queue gradebookServiceQueue = new Queue("gradebook_service", true);

    @Bean
    public Queue createQueue() {
        return new Queue("registrar_service", true);
    }

    @Autowired
    RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = "registrar_service")
    public void receiveFromGradebook(String message)  {
        //TODO implement this message
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