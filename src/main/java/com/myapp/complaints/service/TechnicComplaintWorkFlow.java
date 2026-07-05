package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.complaintStateHandler.ComplaintStateValidator;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintImage;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TechnicComplaintWorkFlow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final ComplaintWorkflowEngine workflowEngine;
    private final AuthorizationService authorizationService;

    public List<ReceptionComplaintResponseDto> getInstitutionComplaints(ComplaintFilterRequestDto filter) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepo.findByAccount_Email(email);

        Specification<Complaint> spec =  (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("governorate").get("id"), employee.getGovernorate().getId()));
            predicates.add(cb.equal(root.get("institution").get("id"), employee.getInstitution().getId()));

            if (filter.state() != null) {

                ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());
                predicates.add(cb.equal(root.get("state"), complaintState));

                if (complaintState == ComplaintState.IN_PROGRESS || complaintState == ComplaintState.RESOLVED) {

                    Join<Complaint, ComplaintTrackingLog> logJoin = root.join("logs");

                    predicates.add(cb.equal(
                            logJoin.get("actionBy").get("id"),
                            employee.getAccount().getId()
                    ));

                    predicates.add(cb.equal(
                            logJoin.get("newState"),
                            complaintState
                    ));

//                        query.distinct(true);
                }

                else if(complaintState == ComplaintState.ASSIGNED){

                predicates.add(cb.equal(root.get("state"), ComplaintState.ASSIGNED));
                predicates.add(cb.equal(root.get("assignedTo"), employee));

                }
            }
            else {
                predicates.add(cb.equal(root.get("state"), ComplaintState.ASSIGNED));
                predicates.add(cb.equal(root.get("assignedTo"), employee));
            }

            // Keyword search
            if (filter.keyword() != null && !filter.keyword().isEmpty()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            query.orderBy(cb.desc(root.get("dateTimeOfAdd")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (filter.page() == null || filter.size() == null) {

            return complaintRepo.findAll(
                            spec,
                            PageRequest.of(0, 100)).stream()
                    .map(complaintMapper::toPerceptionComplaintDto)
                    .toList();

        } else {
            return complaintRepo.findAll(
                            spec,
                            PageRequest.of(filter.page(), filter.size())
                    ).stream()
                    .map(complaintMapper::toPerceptionComplaintDto)
                    .toList();
        }
    }

    @Transactional
    public ReceptionComplaintResponseDto openComplaint(long complaintId) {

        Employee employee =
                employeeRepo.findByAccount_Email(  SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(complaintId).orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        ComplaintState complaintState = complaint.getState();
        if(complaintState.equals(ComplaintState.NEW) ||
                complaintState.equals(ComplaintState.IN_REVIEW) ||
                complaintState.equals(ComplaintState.FORWARDED_TO_MANAGER)
        ){
            throw new ApiException("this complaint is not in your responsibility",HttpStatus.FORBIDDEN);
        }

        if (!authorizationService.checkAccessibility(employee,complaint)) {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        workflowEngine.createActionLog(complaint, employee.getAccount(), ActionType.OPENED);

        return complaintMapper
                .toPerceptionComplaintDto(complaint);

    }




}
