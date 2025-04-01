package com.cst438.controller;

import com.cst438.domain.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@AutoConfigureMockMvc
@SpringBootTest
public class StudentScheduleControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    EnrollmentRepository enrollmentRepository;

    @Test
    public void enrollDuplicateTest() throws Exception {

        MockHttpServletResponse response = mvc.perform(
                        MockMvcRequestBuilders.post("/enrollments/sections/6?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus(), "expected first enrollment to succeed");

        response = mvc.perform(
                        MockMvcRequestBuilders.post("/enrollments/sections/6?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus(), "expected duplicate enrollment to return bad request");

        String errorMessage = response.getErrorMessage();
        assertNotNull(errorMessage, "expected an error message for duplicate enrollment");
        assertEquals("student already enrolled in this section", errorMessage, "error message did not match expected text");
    }

    @Autowired
    private MockMvc mockMvc;
    @Test
    public void enrollBadSectionTest() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(
                        MockMvcRequestBuilders.post("/enrollments/sections/9999?studentId=3")
                                .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(404, response.getStatus(), "expected enrollment with invalid section number to return 404 status");

        String errorMessage = response.getErrorMessage();
        assertNotNull(errorMessage, "expected an error message for invalid section number");
        assertEquals("section not found", errorMessage, "error message did not match expected text");
    }
}


