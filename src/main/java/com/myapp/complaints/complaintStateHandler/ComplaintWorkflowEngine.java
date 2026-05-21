package com.myapp.complaints.complaintStateHandler;

import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ComplaintWorkflowEngine {

    private final ComplaintRepo complaintRepo;
    private final ComplaintTracingLogRepo logRepo;
    private final ComplaintStateValidator validator;

    public void changeState(Complaint complaint, ComplaintState newState, Account actor, Employee assignedTo, String comment,ActionType actionType) {

        ComplaintState currentState = complaint.getState();

        //  validation
        validator.validate(currentState, newState);

        //  create log
        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(currentState);
        log.setNewState(newState);
        log.setActionBy(actor);
        log.setAssignedTo(assignedTo);
        log.setActionType(actionType);
        log.setComments(buildComment(actionType,newState,comment));

        logRepo.save(log);

        //  update complaint
        complaint.setState(newState);
        complaint.setAssignedTo(assignedTo);
        complaint.setDateTimeOfUpdate(LocalDateTime.now());
        complaintRepo.save(complaint);
    }


    public void createInitialLog(Complaint complaint, Account actor) {

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(null);
        log.setNewState(ComplaintState.NEW);
        log.setActionBy(actor);
        log.setActionType(ActionType.CREATED);
        log.setComments(buildComment(ActionType.CREATED,ComplaintState.NEW,null));

        logRepo.save(log);
    }

    public void createActionLog(Complaint complaint, Account actor,ActionType actionType) {

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(complaint.getState());
        log.setNewState(complaint.getState());
        log.setActionBy(actor);
        log.setActionType(actionType);
        log.setAssignedTo(complaint.getAssignedTo());
        log.setComments(buildComment(actionType,complaint.getState(),null));

        logRepo.save(log);
    }

    private String buildComment(ActionType actionType, ComplaintState newState, String customComment) {

        if (customComment != null && !customComment.isBlank()) {
            return customComment;
        }

        return switch (actionType) {
            case DELETED -> "تم حذف الشكوى";
            case UPDATED -> "تم تحديث الشكوى";
            case OPENED -> "تم فتح الشكوى من قبل الموظف";
            case IN_REVIEW -> "الشكوى قيد المراجعة";
            case CREATED ->  "تم اضافة شكوى جديدة";
            case ACCEPTED -> "تم قبول الشكوى وتحويلها للمدير";
            case ASSIGNED -> "تم اسناد الشكوى للموظف المسؤول لمتابعة حلها";
            case REJECTED -> "تم رفض الشكوى";
            case STARTED -> "تم بدء العمل على الشكوى";
            case FINISHED -> "تم الانتهاء من العمل على الشكوى";
            case CLOSED -> "تم إغلاق الشكوى";
            case UPLOAD_IMAGE -> "تم ارفاق صور لحل الشكوى";


            default -> "تم تنفيذ إجراء على الشكوى";
        };
    }

}