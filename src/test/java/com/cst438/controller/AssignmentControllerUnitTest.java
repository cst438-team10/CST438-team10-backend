package com.cst438.controller;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.Grade;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
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

    @Autowired
    GradeRepository gradeRepository;

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
    public void addInvalidAssignmentDueDate() throws Exception {
        MockHttpServletResponse response;

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Collect samples from Miller's planet",
                "2027-03-31",  // Invalid due date
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

        assertEquals(400, response.getStatus());  // Expecting 400 Bad Request

        String errorMessage = response.getContentAsString();
        System.out.println(errorMessage);
        assertTrue(errorMessage.contains("Due date is invalid: Must be within the course start and end dates"));
    }

    @Test
    public void addAssignmentGrade() throws Exception {
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
        AssignmentDTO assignmentResult = fromJsonString(response.getContentAsString(), AssignmentDTO.class);

        GradeDTO gradeDTO = new GradeDTO(
                1,
                "John Smith",
                "john.smith@csumb.edu",
                "Collect samples from Miller's planet",
                "cst363",
                9,
                95
        );

        response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gradeDTO)))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
        GradeDTO result = fromJsonString(response.getContentAsString(), GradeDTO.class);
        Grade grade = gradeRepository.findById(result.gradeId()).orElse(null);
        assertNotNull(grade);
        assertEquals(1, result.gradeId());
        assertEquals("John Smith", result.studentName());
        assertEquals("john.smith@csumb.edu", result.studentEmail());
        assertEquals("Collect samples from Miller's planet", result.assignmentTitle());
        assertEquals(1, result.gradeId());
        assertEquals(9, result.sectionId());
        assertEquals(95, result.score());

        response = mockMvc.perform(MockMvcRequestBuilders
                        .delete("/assignments/"+assignmentResult.id()))
                .andReturn()
                .getResponse();
        assertEquals(200, response.getStatus());
        Assignment assignment = assignmentRepository.findById(assignmentResult.id()).orElse(null);
        assertNull(assignment);
    }

    @Test
    public void addGradeToInvalidAssignment() throws Exception {
        MockHttpServletResponse response;

        int invalidAssignmentId = 9999; //assignment ID that does not exist
        GradeDTO gradeDTO = new GradeDTO(
                1,
                "John Smith",
                "john.smith@csumb.edu",
                "Collect samples from Miller's planet",
                "cst363",
                9,
                95
        );

        response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/assignments/" + invalidAssignmentId + "/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gradeDTO)))
                .andReturn().getResponse();

        assertEquals(405, response.getStatus());
    }


}
