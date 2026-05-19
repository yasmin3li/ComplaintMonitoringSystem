package com.myapp.complaints.complaintStateHandler;

import com.myapp.complaints.enums.ComplaintState;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Component
public class ComplaintStateMachine {

    private final Map<ComplaintState, Set<ComplaintState>> transitions = Map.of(
            ComplaintState.NEW, Set.of(ComplaintState.IN_REVIEW),
            ComplaintState.IN_REVIEW, Set.of(ComplaintState.FORWARDED_TO_MANAGER, ComplaintState.REJECTED),
            ComplaintState.FORWARDED_TO_MANAGER,Set.of(ComplaintState.ASSIGNED,ComplaintState.REJECTED),
            ComplaintState.ASSIGNED, Set.of(ComplaintState.IN_PROGRESS),
            ComplaintState.IN_PROGRESS, Set.of(ComplaintState.RESOLVED),
            ComplaintState.RESOLVED, Set.of(ComplaintState.CLOSED),
            ComplaintState.REJECTED, Set.of(ComplaintState.NEW)
    );

    public boolean isValidTransition(ComplaintState from, ComplaintState to) {
        return transitions.getOrDefault(from, Set.of()).contains(to);
    }
}
