package com.cst438.controller;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.dto.AssignmentDTO;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.mockito.junit.MockitoJUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static com.cst438.test.utils.TestUtils.fromJsonString;
import static org.junit.jupiter.api.Assertions.*;

import static com.cst438.test.utils.TestUtils.asJsonString;

@AutoConfigureMockMvc
@SpringBootTest
public class AssignmentControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Test
    public void addAssignment() throws Exception {
        MockHttpServletResponse response;

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Collect samples from Miller's planet",
                "2025-03-31",
                "cst363",
                9,
                8
        );

        response = mockMvc.perform(MockMvcRequestBuilders
                .post("/assignments")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(assignmentDTO)))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
        AssignmentDTO result = fromJsonString(response.getContentAsString(), AssignmentDTO.class);
        Assignment assignment = assignmentRepository.findById(result.id()).orElse(null);
        assertNotNull(assignment);
        assertNotEquals(0, result.id());
        assertEquals("Collect samples from Miller's planet", result.title());
        assertEquals(9, result.secId());

        response = mockMvc.perform(MockMvcRequestBuilders
                .delete("/assignments/"+result.id()))
                .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());
        assignment = assignmentRepository.findById(result.id()).orElse(null);
        assertNull(assignment);
    }

    @Test
    public void addAssignmentInvalidDueDate() throws Exception {
        MockHttpServletResponse response;

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Invalid due date assignment",
                "2025-06-01",
                "cst363",
                9,
                8
        );

        response = mockMvc.perform(MockMvcRequestBuilders
                .post("/assignments")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(assignmentDTO)))
                .andReturn().getResponse();

        assertEquals(400, response.getStatus());
        String errorMessage = response.getErrorMessage();
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("invalid due date"));
    }

}
