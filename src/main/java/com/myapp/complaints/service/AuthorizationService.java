package com.myapp.complaints.service;

import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorizationService {
    private final ComplaintTracingLogRepo complaintTracingLogRepo;


    public boolean checkAccess(String realOwnerEmail){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();
        return currentUser.equals(realOwnerEmail);
    }

    private Authentication getAuth(){
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean hasRole(String role){
        return getAuth().getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    public boolean IsReceptionist() {
        return hasRole("ROLE_RECEPTIONIST");
    }

    public boolean isCitizen() {
        return hasRole("ROLE_CITIZEN");
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    public boolean isManager() {
        return hasRole("ROLE_MANAGER");
    }

    //An open complaint is the responsibility of the employee who opened it only
    public boolean checkResponsibility(Employee employee, Complaint complaint){

        return complaint.getAssignedTo().equals(employee);

//        Optional<ComplaintTrackingLog> log = complaintTracingLogRepo.findTopByComplaint_IdOrderByActionDateDesc
//                (complaint.getId());
//
//        return log.isPresent()
//                && log.get().getAssignedTo().getAccount().getId().equals(employee.getId());
    }

    public boolean checkAccessibility(Employee employee, Complaint complaint) {
        Optional<ComplaintTrackingLog> log =
                complaintTracingLogRepo.findTopByComplaint_IdAndActionBy_IdAndNewStateOrderByActionDateDesc(complaint.getId(),employee.getAccount().getId(),complaint.getState());
        return log.isPresent();
    }
}
