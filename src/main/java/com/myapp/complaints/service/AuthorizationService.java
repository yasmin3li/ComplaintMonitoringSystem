package com.myapp.complaints.service;

import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

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

        List<ComplaintTrackingLog> check = complaintTracingLogRepo.findByComplaint_IdAndActionBy_Id
                (complaint.getId(), employee.getAccount().getId());

        return !check.isEmpty();
    }

}
