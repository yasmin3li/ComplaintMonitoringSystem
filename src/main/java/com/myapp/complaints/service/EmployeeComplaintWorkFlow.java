package com.myapp.complaints.service;

import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.complaintStateHandler.ComplaintStateValidator;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.ComplaintRejectDto;
import com.myapp.complaints.dto.UpdateComplaintDto;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintImage;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeeComplaintWorkFlow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintWorkflowEngine workflowEngine;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;
    private final ComplaintStateValidator validator;

//    we will not use this action/state at this version.
    @Transactional
    public ApiResponseDto<?> closeComplaint(ComplaintRejectDto dto) {

        Employee employee = employeeRepo.findByAccount_Email(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (complaint.getAssignedTo() == null) {
            throw new ApiException("Not assigned yet", HttpStatus.BAD_REQUEST);
        }

        if(!authorizationService.isManager()){
            if(!authorizationService.checkResponsibility(employee,complaint)){
                throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
            }
        }
        else {
            if(!authorizationService.checkAccessibility(employee,complaint)){
                throw new ApiException("Access denied, you can't access to this complaint",HttpStatus.FORBIDDEN);
            }
        }

        workflowEngine.changeState(complaint,ComplaintState.CLOSED,employee.getAccount(),
                null, dto.reason() == null ? null : dto.reason(), ActionType.CLOSED);

        return new ApiResponseDto<>(
                true,
                "تم إغلاق الشكوى بنجاح",
                null
        );

    }

    @Transactional
    public ApiResponseDto<?> updateComplaint(String email, UpdateComplaintDto dto) {

        Employee employee = employeeRepo.findByAccount_Email(email);

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (complaint.getAssignedTo() == null) {
            throw new ApiException("Not assigned yet", HttpStatus.BAD_REQUEST);
        }

        if(!authorizationService.checkResponsibility(employee,complaint)){
            throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
        }

        if (!(complaint.getState().equals(ComplaintState.IN_PROGRESS))) {

            throw new RuntimeException("you can upload complaint's image only at state in_progress");
        }

        if (dto.images() != null) {

            for (String img : dto.images()) {

                ComplaintImage complaintImage = new ComplaintImage();
                complaintImage.setImageUrl(img);
                complaintImage.setType(ImageType.AFTER_SOLVE);
                complaintImage.setComplaint(complaint);
                complaintImage.setAddedBy(employee.getAccount());

                complaint.getImages().add(complaintImage);
            }
        }

        complaintRepo.save(complaint);
        complaint.setDateTimeOfUpdate(LocalDateTime.now());

        workflowEngine.createActionLog(complaint,employee.getAccount(), ActionType.UPLOAD_IMAGE);

        return new ApiResponseDto<>(
                true,
                "images uploaded and complaint updated  successfully",
                null
        );

    }

    @Transactional
    public ApiResponseDto<?> solveComplaint(ComplaintRejectDto dto){

        Employee employee = employeeRepo.findByAccount_Email(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (complaint.getAssignedTo() == null) {
            throw new ApiException("Not assigned yet", HttpStatus.BAD_REQUEST);
        }

//        if(!authorizationService.isManager()){
            if(!authorizationService.checkResponsibility(employee,complaint)){
                throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
            }
//        }
//        else {
//            if(!authorizationService.checkAccessibility(employee,complaint)){
//                throw new ApiException("Access denied, you can't access to this complaint",HttpStatus.FORBIDDEN);
//            }
//        }

        workflowEngine.changeState(complaint,
                ComplaintState.RESOLVED,employee.getAccount(),
                employee,
                dto.reason() == null ? null : dto.reason(),
                ActionType.FINISHED
        );

        return new ApiResponseDto<>(
                true,
                "تم اضافة الشكوى الى الشكاوى المحلولة",
                null
        );

    }

    @Transactional
    public ApiResponseDto<?> rejectComplaint(String email, ComplaintRejectDto dto) {

        Employee employee = employeeRepo.findByAccount_Email(email);

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (complaint.getAssignedTo() == null) {
            throw new ApiException("Not assigned yet", HttpStatus.BAD_REQUEST);
        }

        if(!authorizationService.isManager()){
            if(!authorizationService.checkResponsibility(employee,complaint)){
                throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
            }
        }
        else {
            if(!authorizationService.checkAccessibility(employee,complaint)){
                throw new ApiException("Access denied, you can't access to this complaint",HttpStatus.FORBIDDEN);
            }
        }

            if((complaint.getState().equals(ComplaintState.IN_REVIEW)) || (complaint.getState().equals(ComplaintState.FORWARDED_TO_MANAGER))){
                workflowEngine.changeState(complaint,ComplaintState.REJECTED,employee.getAccount(),null,dto.reason(),ActionType.REJECTED);

                notificationService.notifyUsers(complaint,dto.reason(), List.of(complaint.getAddedBy()));
            }
            else{
                throw new ApiException("you can't reject a complaint at this state",HttpStatus.BAD_REQUEST);
            }

        return new ApiResponseDto<>(
                true,
                String.format("تم رفض شكواك: \"%s\" بسبب \"%s\" ",complaint.getTitle(),dto.reason()),
                null
        );
    }

    public ApiResponseDto<?> startSolveComplaint(long complaintId) {


        Employee employee =
                employeeRepo.findByAccount_Email(
                        SecurityContextHolder.getContext().getAuthentication().getName()
                );

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (complaint.getAssignedTo() == null) {
            throw new ApiException("Not assigned yet", HttpStatus.BAD_REQUEST);
        }

        if(!authorizationService.checkResponsibility(employee,complaint)){
            throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
        }


        validator.validate(
                complaint.getState(),
                ComplaintState.IN_PROGRESS
        );

        workflowEngine.changeState(
                complaint,
                ComplaintState.IN_PROGRESS,
                employee.getAccount(),
                employee,
                null,
                ActionType.STARTED
        );
        return null;
    }
}
