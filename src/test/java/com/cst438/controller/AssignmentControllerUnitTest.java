package com.cst438.controller;


import com.cst438.domain.Assignment;
import com.cst438.domain.AssignmentRepository;
import com.cst438.domain.GradeRepository;
import com.cst438.dto.AssignmentDTO;
import com.cst438.dto.GradeDTO;
import org.junit.jupiter.api.Test;
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
    public void addAssignmentInvalidSectionNumber() throws Exception {
        MockHttpServletResponse response;

        AssignmentDTO assignmentDTO = new AssignmentDTO(
                0,
                "Collect samples from Miller's planet",
                "2025-03-31",
                "cst363",
                9,
                5
        );

        response = mockMvc.perform(MockMvcRequestBuilders
                        .post("/assignments")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(assignmentDTO)))
                .andReturn().getResponse();

        assertEquals(404, response.getStatus());
        assertEquals("Invalid Section Number", response.getErrorMessage());
    }

    @Test
    public void addAssignmentGrade() throws Exception {
        MockHttpServletResponse response;

        int assignmentId = 1;
        response = mockMvc.perform(MockMvcRequestBuilders
                        .get("/assignments/" + assignmentId + "/grades")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());

        String jsonResponse = response.getContentAsString();
        assertFalse(jsonResponse.isEmpty(), "Grade Not Found");

        String updatedJson = jsonResponse.replace("\"score\":85", "\"score\":95");

        response = mockMvc.perform(MockMvcRequestBuilders
                        .put("/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedJson)) // Send modified JSON
                .andReturn().getResponse();

        assertEquals(200, response.getStatus());
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
                        .get("/assignments/" + invalidAssignmentId + "/grades")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(gradeDTO)))
                .andReturn().getResponse();

        assertEquals(404, response.getStatus());
        assertEquals("Assignment not found", response.getErrorMessage());
    }


}
