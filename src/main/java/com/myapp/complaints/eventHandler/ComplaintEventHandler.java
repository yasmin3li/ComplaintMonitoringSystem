package com.myapp.complaints.eventHandler;

import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@RepositoryEventHandler(Complaint.class)
@Component
public class ComplaintEventHandler {

    private final ComplaintRepo complaintRepo;

    @HandleBeforeCreate
    public void beforeCreate(Complaint c) {
        c.setState(ComplaintState.NEW);
        c.setDeleted(false);
        c.setDateTimeOfAdd(LocalDateTime.now());

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(c);
        log.setPreviousState(null);
        log.setNewState(ComplaintState.NEW);
        log.setActionType(ActionType.CREATED);
// TODO handling later using jwt
        log.setActionBy(null);
        log.setAssignedTo(null);

        c.getLogs().add(log);
    }

    @HandleBeforeCreate
    public void validateCreate(Complaint c) {
        if (c.getAddedBy() == null) throw new RuntimeException("Complaint must have an owner");
        if (c.getInstitution() == null) throw new RuntimeException("Complaint must belong to an institution");
        if (c.getService() == null) throw new RuntimeException("Complaint must have a service");
        if (c.getGovernorate() == null) throw new RuntimeException("Complaint must have a governorate");
        if (c.getSector() == null) throw new RuntimeException("Complaint must have a sector");
        if (c.getAddress() == null) throw new RuntimeException("Complaint must have an address");
        if (c.getTitle() == null || c.getTitle().isEmpty()) throw new RuntimeException("Title is required");
        if (c.getDescription() == null || c.getDescription().isEmpty()) throw new RuntimeException("Description is required");
    }


    @HandleBeforeSave
    public void beforeSave(Complaint c) {
        Complaint old = complaintRepo.findById(c.getId())
                .orElseThrow(() -> new RuntimeException("Complaint not found"));

        if (!old.getState().equals(c.getState())) {
            if (c.getState() == ComplaintState.RESOLVED) {
                c.setDateTimeOfSolve(LocalDateTime.now());
            }


            // if  state didn't change do not do anything
            if (old.getState() == c.getState()) {
                return;
            }

            // validate that the transition is logical
            if (!isValidTransition(old.getState(), c.getState())) {
                throw new IllegalStateException(
                        "Invalid state transition: " +
                                old.getState() + " -> " + c.getState()
                );
            }

            ComplaintTrackingLog log = new ComplaintTrackingLog();
            log.setComplaint(c);
            log.setPreviousState(old.getState());
            log.setNewState(c.getState());
            log.setActionType(ActionType.STATE_CHANGED);

// TODO handling later using jwt
            log.setActionBy(null);
            log.setAssignedTo(null);

            c.getLogs().add(log);
        }
    }

    @HandleBeforeDelete
    public void beforeDelete(Complaint c) {

        if (c.getState() == ComplaintState.ASSIGNED ||
                c.getState() == ComplaintState.IN_PROGRESS ||
                c.getState() == ComplaintState.RESOLVED) {
            throw new IllegalStateException(
                    "Cannot delete complaint after it has been assigned or processed"
            );
        }
        c.setDeleted(true);

        //add this action to the complaint tracing log
        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(c);
        log.setPreviousState(c.getState());
        log.setNewState(c.getState());
        log.setActionType(ActionType.DELETED);

//TODO later using jwt
        log.setActionBy(null);
        log.setAssignedTo(null);
        c.getLogs().add(log);
        throw new RuntimeException("Physical delete not allowed");

    }


    private boolean isValidTransition(ComplaintState from, ComplaintState to) {

        return switch (from) {

            case NEW ->
                    to == ComplaintState.ASSIGNED ||
                            to == ComplaintState.REJECTED ||
                            to == ComplaintState.CANCELLED;

            case ASSIGNED ->
                    to == ComplaintState.IN_PROGRESS ||
                            to == ComplaintState.CANCELLED;

            case IN_PROGRESS ->
                    to == ComplaintState.RESOLVED ||
                            to == ComplaintState.REJECTED;

            case RESOLVED ->
                    to == ComplaintState.CLOSED ;
            default -> false;
        };
    }


}
