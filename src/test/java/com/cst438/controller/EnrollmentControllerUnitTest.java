package com.cst438.controller;

import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
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

@AutoConfigureMockMvc
@SpringBootTest
public class EnrollmentControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void EnrollPastAddDeadlineTest() throws Exception {
        MockHttpServletResponse response;
        response = mockMvc.perform(MockMvcRequestBuilders.post(String.format("/enrollments/sections/%s?studentId=3", "5")))
                .andReturn().getResponse();
        assertEquals(400, response.getStatus());
        assertEquals("enrollment closed", response.getErrorMessage());
    }

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
}
