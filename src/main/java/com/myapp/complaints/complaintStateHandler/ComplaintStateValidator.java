package com.myapp.complaints.complaintStateHandler;

import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ComplaintStateValidator {

    private final ComplaintStateMachine stateMachine;

    public void validate(ComplaintState from, ComplaintState to) {
        if (!stateMachine.isValidTransition(from, to)) {
            throw new RuntimeException(
                    "Invalid transition from " + from + " to " + to
            );
        }
    }
}
