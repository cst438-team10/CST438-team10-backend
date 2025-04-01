package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SectionRepository sectionRepository;

    /**
     instructor lists assignments for a section.
     Assignment data is returned ordered by due date.
     logged in user must be the instructor for the section (assignment 7)
     */
    @GetMapping("/sections/{secNo}/assignments")
    public List<AssignmentDTO> getAssignments(
            @PathVariable("secNo") int secNo) {
		// hint: use the assignment repository method 
		//  findBySectionNoOrderByDueDate to return 
		//  a list of assignments
        List<Assignment> assignments = assignmentRepository.findBySectionNoOrderByDueDate(secNo);
        if(assignments == null){
            System.out.println("Assignments not found with section number: "+ secNo);
            return null;
        }
        List<AssignmentDTO> assignmentDTOs = new ArrayList<>();
        for (Assignment assignment : assignments) {
            assignmentDTOs.add(new AssignmentDTO(assignment.getAssignmentId(), assignment.getTitle(), assignment.getDueDate().toString(), assignment.getSection().getCourse().toString(), assignment.getSection().getSecId(), assignment.getSection().getSectionNo()));
        }

        return assignmentDTOs;
    }

    /**
     instructor creates an assignment for a section.
     Assignment data with primary key is returned.
     logged in user must be the instructor for the section (assignment 7)
     */
    @PostMapping("/assignments")
    public AssignmentDTO createAssignment(
            @RequestBody AssignmentDTO dto) {
        // Get the existing section from the database
        Section section = sectionRepository.findById(dto.secNo())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "section not found"));

        Assignment assignment = new Assignment();
        assignment.setAssignmentId(dto.id());
        assignment.setTitle(dto.title());
        assignment.setDueDate(Date.valueOf(dto.dueDate()));
        assignment.setSection(section);

        // Validate due date against term dates
        Date dueDate = Date.valueOf(dto.dueDate());
        if (dueDate.before(section.getTerm().getStartDate()) || dueDate.after(section.getTerm().getEndDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid due date");
        }

        // Save the assignment using the repository
        Assignment savedAssignment = assignmentRepository.save(assignment);

        // Convert saved assignment back to DTO, using the secId from the input DTO
        return new AssignmentDTO(
            savedAssignment.getAssignmentId(), 
            savedAssignment.getTitle(), 
            savedAssignment.getDueDate().toString(), 
            savedAssignment.getSection().getCourse().toString(), 
            dto.secId(),  // Use the secId from the input DTO
            savedAssignment.getSection().getSectionNo()
        );
    }

    /**
     instructor updates an assignment for a section.
     only title and dueDate may be changed
     updated assignment data is returned
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/assignments")
    public AssignmentDTO updateAssignment(@RequestBody AssignmentDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.id()).orElse(null);

        if(assignment == null) {
           System.out.println("No Assignment found with id: "+dto.id());
           return null;
        }
        assignment.setTitle(dto.title());
        assignment.setDueDate(Date.valueOf(dto.dueDate()));

        Assignment updatedAssignment = assignmentRepository.save(assignment);
        return new AssignmentDTO(updatedAssignment.getAssignmentId(), updatedAssignment.getTitle(), updatedAssignment.getDueDate().toString(), updatedAssignment.getSection().getCourse().toString(), updatedAssignment.getSection().getSecId(), updatedAssignment.getSection().getSectionNo());
    }


    /**
     instructor deletes an assignment for a section.
     logged in user must be the instructor for the section (assignment 7)
     */
    @DeleteMapping("/assignments/{assignmentId}")
    public void deleteAssignment(@PathVariable("assignmentId") int assignmentId) {
        if (!assignmentRepository.existsById(assignmentId)) {
            System.out.println("No Assignment found with id: "+ assignmentId);
            return;
        }

        assignmentRepository.deleteById(assignmentId);
    }
}
