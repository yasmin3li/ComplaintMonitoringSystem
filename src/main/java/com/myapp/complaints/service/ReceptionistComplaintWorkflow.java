package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReceptionistComplaintWorkflow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final ComplaintWorkflowEngine workflowEngine;
    private final AccountRepo accountRepo;
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;


    public PerceptionComplaintResponseDto reviewComplaint(Complaint complaint) {

        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        if(complaint.getInstitution().getId().equals(employee.getInstitution().getId())){

            if(complaint.getState().equals(ComplaintState.NEW)){

                workflowEngine.changeState(complaint,ComplaintState.IN_REVIEW,employee.getAccount(),null,"الشكوى قيد المراجعة");

                return complaintMapper.toPerceptionComplaintDto(complaint);

            } else if (!authorizationService.checkResponsibility(employee,complaint)) {

                throw new ApiException("This complaint is not your responsibility, there are another employee working on it", HttpStatus.FORBIDDEN);
            }
            else {
                return complaintMapper.toPerceptionComplaintDto(complaint);
            }

        }

        else throw new ApiException("this complaint doesn't belong to your institution",HttpStatus.FORBIDDEN);
    }

    public Specification<Complaint> getInstitutionComplaints(ComplaintFilterRequestDto filter) {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee employee = employeeRepo.findByAccount_Email(email);

            return  (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("deleted"), false));
                predicates.add(cb.equal(root.get("governorate").get("id"), employee.getGovernorate().getId()));
                predicates.add(cb.equal(root.get("institution").get("id"), employee.getInstitution().getId()));

                if (filter.state() == null) {
                    predicates.add(cb.equal(root.get("state"), ComplaintState.NEW));
                } else {
                    ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());
                    predicates.add(cb.equal(root.get("state"), complaintState));

                    if (complaintState == ComplaintState.IN_REVIEW || complaintState == ComplaintState.REJECTED ) {

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

                query.orderBy(cb.desc(root.get("dateTimeOfAdd")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }


    public ApiResponseDto<?> rejectComplaint(String email, ComplaintRejectDto dto) {

        Optional<Complaint> complaint = complaintRepo.findByIdAndDeletedFalse(dto.complaintId());
        if(complaint.isEmpty()){
            throw new ApiException("complaint not found",HttpStatus.NOT_FOUND);
        }

        Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(email);

        List<ComplaintTrackingLog> log =
                complaintTracingLogRepo.findByComplaint_IdAndActionBy_Id(
                        complaint.get().getId(),account.get().getId());

                if(!log.isEmpty() && complaint.get().getState().equals(ComplaintState.IN_REVIEW)){
                   workflowEngine.changeState(complaint.get(),ComplaintState.REJECTED,account.get(),null,dto.reason());
                   notificationService.notifyUsers(complaint.get(),dto.reason(),List.of(complaint.get().getAddedBy()));
                }
                else{
                    throw new ApiException("you aren't allowed to reject this complaint, you aren't working on it",HttpStatus.FORBIDDEN);
                }
        return new ApiResponseDto<>(
                true,
                String.format("تم رفض شكواك: \"%s\" بسبب \"%s\" ",complaint.get().getTitle(),dto.reason()),
                null
        );
    }

    public ApiResponseDto<?> updateComplaint(String email, UpdateComplaintDto dto) {

        Employee employee = employeeRepo.findByAccount_Email(email);

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

        if(!authorizationService.checkResponsibility(employee,complaint)){
            throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
        }

        ComplaintState complaintState = complaint.getState();

        if (!(complaintState.equals(ComplaintState.RESOLVED))) {

            throw new RuntimeException("you can upload complaint's image only at state RESOLVED");
        }

        if (dto.images() != null) {

            complaint.getImages().clear();

            for (String img : dto.images()) {

                ComplaintImage complaintImage = new ComplaintImage();
                complaintImage.setImageUrl(img);
                complaintImage.setType(ImageType.AFTER_SOLVE);
                complaintImage.setComplaint(complaint);
                complaintImage.setAddedBy(employee.getAccount());

                complaint.getImages().add(complaintImage);
            }
        }
        else{
           throw new ApiException("it is not allowed for you to delete images citizen's complaint",HttpStatus.BAD_REQUEST);
        }

        complaintRepo.save(complaint);

        return new ApiResponseDto<>(
                true,
                "images uploaded and complaint updated  successfully",
                null
        );
    }
}


