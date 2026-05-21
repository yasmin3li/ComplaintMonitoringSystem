package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.complaintStateHandler.ComplaintStateValidator;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReceptionistComplaintWorkflow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final ComplaintWorkflowEngine workflowEngine;
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;
    private final ComplaintStateValidator validator;

    public ReceptionComplaintResponseDto openComplaint(Complaint complaint, ComplaintState complaintState) {

        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        if(complaint.getInstitution().getId().equals(employee.getInstitution().getId())) {

            switch (complaintState) {
                case ComplaintState.NEW ->
                    {
//
//                        int updated = complaintRepo.openIfNew(complaint.getId());
//
//                        if (updated == 0) {
//                            throw new ApiException(
//                                    "Complaint already taken by another employee",
//                                    HttpStatus.CONFLICT
//                            );
//                        }
//
                        workflowEngine.createActionLog(complaint, employee.getAccount(), ActionType.OPENED);

                        return complaintMapper.toPerceptionComplaintDto(complaint);
                    }

                case ComplaintState.REJECTED, ComplaintState.FORWARDED_TO_MANAGER -> {
                    {
                        if (!authorizationService.checkAccessibility(employee, complaint)) {

                            throw new ApiException("This complaint is not your responsibility, there are another employee working on it", HttpStatus.FORBIDDEN);
                        } else {
                            return complaintMapper.toPerceptionComplaintDto(complaint);
                        }
                    }
                }

                case ComplaintState.IN_REVIEW ,ComplaintState.RESOLVED, ComplaintState.CLOSED, ComplaintState.ASSIGNED,
                     ComplaintState.CANCELLED, ComplaintState.IN_PROGRESS->
                    {
                        if (!authorizationService.checkResponsibility(employee, complaint)) {

                            throw new ApiException("This complaint is not your responsibility, there are another employee working on it", HttpStatus.FORBIDDEN);
                        } else {
                            return complaintMapper.toPerceptionComplaintDto(complaint);
                        }
                    }
                default -> throw new ApiException("Invalid state: " + complaintState, HttpStatus.BAD_REQUEST);
            }

        }
        else throw new ApiException("this complaint doesn't belong to your institution",HttpStatus.FORBIDDEN);
    }

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

                    if (complaintState == ComplaintState.IN_REVIEW || complaintState == ComplaintState.REJECTED
                            || complaintState == ComplaintState.FORWARDED_TO_MANAGER ) {

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
                }
                else {
                    predicates.add(cb.equal(root.get("state"), ComplaintState.NEW));
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
    public ApiResponseDto<?> inReview(long complaintId) {

        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        ComplaintState complaintState = complaint.getState();

        if(complaint.getInstitution().getId().equals(employee.getInstitution().getId())) {

            switch (complaintState) {
                case ComplaintState.NEW ->
                {

                    int updated = complaintRepo.openIfNew(complaint.getId());

                    if (updated == 0) {
                        throw new ApiException(
                                "Complaint already taken by another employee",
                                HttpStatus.CONFLICT
                        );
                    }

                    workflowEngine.changeState(complaint, ComplaintState.IN_REVIEW, employee.getAccount(), employee, null, ActionType.IN_REVIEW);


                    return new ApiResponseDto<>(true,"الشكوى الان ضمن مسؤلياتك",null);
                }

                case ComplaintState.IN_REVIEW ->
                {
                    if(!authorizationService.checkResponsibility(employee,complaint)){
                        throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
                    }
                    //workflowEngine.createActionLog(complaint,employee.getAccount(),ActionType.IN_REVIEW);
                    return new ApiResponseDto<>(false,"الشكوى مسندة اليك مسبقا",null);
                }
                case ComplaintState.RESOLVED, ComplaintState.REJECTED, ComplaintState.CLOSED, ComplaintState.ASSIGNED,
                     ComplaintState.CANCELLED, ComplaintState.IN_PROGRESS -> {
                    throw new ApiException
                            ("لايمكن تغيير حالة الشكوى الحالية الى قيد المراجعة -عملية غير مسموحة وفق حالتها الحالية-",HttpStatus.BAD_REQUEST);
                }
                default -> throw new ApiException("Invalid state: " + complaintState, HttpStatus.BAD_REQUEST);
            }

        }
        else throw new ApiException("this complaint doesn't belong to your institution",HttpStatus.FORBIDDEN);
    }

    @Transactional
    public ApiResponseDto<?> acceptAndForwardToManager(long complaintId) {

        // the receptionist employee
        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        ComplaintState complaintState = ComplaintState.FORWARDED_TO_MANAGER;

        validator.validate(complaint.getState(),complaintState);

        if(!authorizationService.checkResponsibility(employee,complaint)){
            throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
        }

        if(complaint.getPriority() == null){
            throw new ApiException("you must add priority to the complaint before forward it",HttpStatus.BAD_REQUEST);
        }

        //receptionist assigns this complaint to the manager by default
        List<Employee> forwardTO =
                employeeRepo.findByGovernorate_IdAndInstitution_IdAndAccount_Role_Id(
                        complaint.getGovernorate().getId()
                        ,complaint.getInstitution().getId(),
                        3);

        workflowEngine.changeState
                (complaint,ComplaintState.FORWARDED_TO_MANAGER,employee.getAccount(),null,null, ActionType.ACCEPTED);

        List<Account> accounts = new ArrayList<>(List.of());

        for (Employee assignTo : forwardTO) {
            accounts.add(assignTo.getAccount());
        }

        accounts.add(complaint.getAddedBy());
        notificationService.notifyUsers(complaint,"priority: "+complaint.getPriority().toString(),accounts);

        return new ApiResponseDto<>(true,"تم قبول الشكوى وتحويلها الى المدير",null);
    }

}


