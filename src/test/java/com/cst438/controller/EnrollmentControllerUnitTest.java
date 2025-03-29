package com.cst438.controller;

import com.cst438.domain.EnrollmentRepository;
import com.cst438.dto.EnrollmentDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
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
}
