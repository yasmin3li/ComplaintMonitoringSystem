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

    public void changeState(Complaint complaint, ComplaintState newState, Account actor, Employee assignedTo, String comment) {

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
        log.setActionType(resolveActionType(currentState, newState));
        log.setComments(comment);

        logRepo.save(log);

        //  update complaint
        complaint.setState(newState);
        complaintRepo.save(complaint);
    }

    private ActionType resolveActionType(ComplaintState from, ComplaintState to) {
        if (from == ComplaintState.NEW && to == ComplaintState.IN_REVIEW) {
            return ActionType.OPENED;
        }
        if (to == ComplaintState.REJECTED) {
            return ActionType.REJECTED;
        }
        return ActionType.UPDATED;
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
}