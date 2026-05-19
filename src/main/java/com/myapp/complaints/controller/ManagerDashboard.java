package com.myapp.complaints.controller;

import com.myapp.complaints.dto.AssignComplaintDto;
import com.myapp.complaints.service.ApiService;
import com.myapp.complaints.service.ManagerComplaintWorkFlow;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ManagerDashboard {

    private final ApiService apiService;
    private final ManagerComplaintWorkFlow managerComplaintWorkFlow;

    @GetMapping("/institution/employees")
    public ResponseEntity<?> getEmployees(){
        return ResponseEntity.ok(apiService.getInstitutionEmployee());
    }

    @PostMapping("/complaint/assign")
    public ResponseEntity<?> assignComplaint(
            @RequestBody AssignComplaintDto assignComplaintDto
            ){
        return ResponseEntity.ok(managerComplaintWorkFlow.assignComplaintToEmployee(assignComplaintDto));
    }

    @PostMapping("/complaint/assignToMi/{complaintId}")
    public ResponseEntity<?> assignToMySelf(@PathVariable long complaintId){
        return ResponseEntity.ok(managerComplaintWorkFlow.assignToMyself(complaintId));
    }

}
