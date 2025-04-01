package com.cst438.controller;

import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static com.cst438.test.utils.TestUtils.fromJsonString;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;


/**
 * 1. Unit test to enroll into a course that is past add deadline
 * 2. Unit test to enroll into a section
 * 3. Unit test to update enrollment grade
 */
@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    /**
     * Unit test to enroll into a course that is past add deadline
     * Test ensuring enrolling past ADDDEADLINE is not allowed
     * @throws Exception
     */
    @Test
    public void EnrollPastAddDeadlineTest() throws Exception {
        MockHttpServletResponse response;
        response = mockMvc.perform(MockMvcRequestBuilders.post(String.format("/enrollments/sections/%s?studentId=3", "5")))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
        assertEquals("enrollment closed", response.getErrorMessage());
    }

    /**
     * Unit test to enroll into a section
     * Test ensuring successful enrollment into a section
     * @throws Exception
     */
    @Test
    public void enrollIntoSectionTest() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/enrollments/sections/{sectionNo}?studentId=3", "9")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                .getResponse();;
        assertEquals(200, response.getStatus());
        EnrollmentDTO result = fromJsonString(response.getContentAsString(), EnrollmentDTO.class);
        assertNotEquals(0, result.sectionNo());

        int enrollmentId = result.enrollmentId();
        response = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/enrollments/{enrollmentNo}", enrollmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                        .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());
    }

    /**
     * Unit test to update enrollment grade
     * Test updating enrollment grades updating B to A
     */
    @Test
    public void updateEnrollmentGradeTest() throws Exception {
        MockHttpServletResponse response;
        response = mockMvc.perform(MockMvcRequestBuilders
                .get("/sections/8/enrollments")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
        String check = response.getContentAsString();
        ObjectMapper objectMapper = new ObjectMapper();


        List<EnrollmentDTO> enrollments = objectMapper.readValue(
                response.getContentAsString(),
                new TypeReference<List<EnrollmentDTO>>() {}
        );
        List<EnrollmentDTO> updatedEnrollments = new ArrayList<>();
        for (EnrollmentDTO enrollment : enrollments) {
            updatedEnrollments.add(new EnrollmentDTO(
                    enrollment.enrollmentId(),
                    "A",  // Update grade to "A"
                    enrollment.studentId(),
                    enrollment.name(),
                    enrollment.email(),
                    enrollment.courseId(),
                    enrollment.title(),
                    enrollment.sectionId(),
                    enrollment.sectionNo(),
                    enrollment.building(),
                    enrollment.room(),
                    enrollment.times(),
                    enrollment.credits(),
                    enrollment.year(),
                    enrollment.semester()));
        }

        response = mockMvc.perform(MockMvcRequestBuilders
                        .put("/enrollments")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(updatedEnrollments))) // Send modified JSON
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
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
