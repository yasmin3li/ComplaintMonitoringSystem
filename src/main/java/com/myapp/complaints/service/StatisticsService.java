package com.myapp.complaints.service;

import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.InstitutionRepo;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ComplaintRepo complaintRepo;
    private final InstitutionRepo institutionRepo;

    public long getTotalComplaints() {
        return complaintRepo.countByDeletedFalse();
    }

    public long getNewComplaints() {
        return complaintRepo.countByStateAndDeletedFalse(ComplaintState.NEW);
    }

    public long getInProgressComplaints() {
        return complaintRepo.countByStateAndDeletedFalse(ComplaintState.IN_PROGRESS);
    }

    public long getSolvedComplaints() {
        return complaintRepo.countByStateAndDeletedFalse(ComplaintState.RESOLVED);
    }

    public long getDistinctInstitutionsCount() {
        return institutionRepo.count();
    }
}
