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
        log.setComments(comment);

        logRepo.save(log);

        //  update complaint
        complaint.setState(newState);
        complaintRepo.save(complaint);
    }


    public void createInitialLog(Complaint complaint, Account actor) {

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(null);
        log.setNewState(ComplaintState.NEW);
        log.setActionBy(actor);
        log.setActionType(ActionType.CREATED);
        log.setComments("تم اضافة شكوى جديدة");

        logRepo.save(log);
    }

    public void createActionLog(Complaint complaint, Account actor,ActionType actionType) {

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(complaint.getState());
        log.setNewState(complaint.getState());
        log.setActionBy(actor);
        log.setActionType(actionType);
        log.setComments(associatedComment(actionType));

        logRepo.save(log);
    }

    private String associatedComment(ActionType actionType){
         return switch (actionType){
            case ActionType.DELETED -> "تم حذف الشكوى";
             case CREATED -> null;
             case OPENED -> null;
             case ASSIGNED -> null;
             case ActionType.UPDATED ->  "تم تحديث الشكوى";
             case STATE_CHANGED -> null;
             case COMMENTED -> null;
             case CLOSED -> null;
             case REJECTED -> null;
         };
    }

}