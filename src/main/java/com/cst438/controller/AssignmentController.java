package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.AssignmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
public class AssignmentController {

    @Autowired
    AssignmentRepository assignmentRepository;
    @Autowired
    TermRepository termRepository;
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
    public ResponseEntity<?> createAssignment(@RequestBody AssignmentDTO dto) {
        Section section = sectionRepository.findById(dto.secId()).orElse(null);
        if (section == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No section found with id: " + dto.secId());
        }

        Term term = termRepository.findById(dto.secId()).orElse(null); // Fetch course term
        if (term == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No term found for section id: " + dto.secId());
        }

        Date dueDate = Date.valueOf(dto.dueDate());

        if (dueDate.before(term.getStartDate()) || dueDate.after(term.getEndDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Due date is invalid: Must be within the course start and end dates");
        }

        Assignment assignment = new Assignment();
        Course course = new Course();
        course.setCourseId(dto.courseId());

        section.setCourse(course);
        assignment.setAssignmentId(dto.id());
        assignment.setTitle(dto.title());
        assignment.setDueDate(dueDate);
        assignment.setSection(section);

        Assignment savedAssignment = assignmentRepository.save(assignment);

        return ResponseEntity.ok(new AssignmentDTO(
                savedAssignment.getAssignmentId(),
                savedAssignment.getTitle(),
                savedAssignment.getDueDate().toString(),
                savedAssignment.getSection().getCourse().toString(),
                savedAssignment.getSection().getSecId(),
                savedAssignment.getSection().getSectionNo()
        ));
    }

    /**
     instructor updates an assignment for a section.
     only title and dueDate may be changed
     updated assignment data is returned
     logged in user must be the instructor for the section (assignment 7)
     */
    @PutMapping("/assignments")
    public ResponseEntity<?> updateAssignment(@RequestBody AssignmentDTO dto) {
        Assignment assignment = assignmentRepository.findById(dto.id()).orElse(null);
        if (assignment == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No Assignment found with id: " + dto.id());
        }

        Term term = termRepository.findById(dto.secId()).orElse(null);
        if (term == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("No term found for section id: " + dto.secId());
        }

        Date dueDate = Date.valueOf(dto.dueDate());

        if (dueDate.before(term.getStartDate()) || dueDate.after(term.getEndDate())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Due date is invalid: Must be within the course start and end dates");
        }

        assignment.setTitle(dto.title());
        assignment.setDueDate(dueDate);

        Assignment updatedAssignment = assignmentRepository.save(assignment);

        return ResponseEntity.ok(new AssignmentDTO(
                updatedAssignment.getAssignmentId(),
                updatedAssignment.getTitle(),
                updatedAssignment.getDueDate().toString(),
                updatedAssignment.getSection().getCourse().toString(),
                updatedAssignment.getSection().getSecId(),
                updatedAssignment.getSection().getSectionNo()
        ));
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
