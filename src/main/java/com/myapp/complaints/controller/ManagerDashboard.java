package com.myapp.complaints.controller;

import com.myapp.complaints.service.ApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ManagerDashboard {

    private final ApiService apiService;

    @GetMapping("/institution/employees")
    public ResponseEntity<?> getEmployees(){
        return ResponseEntity.ok(apiService.getInstitutionEmployee());
    }
}
