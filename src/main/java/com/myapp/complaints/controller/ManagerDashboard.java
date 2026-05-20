package com.myapp.complaints.controller;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.dto.AssignComplaintDto;
import com.myapp.complaints.dto.ConfigFilterDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ManagerDashboard {

    private final ApiService apiService;
    private final ManagerComplaintWorkFlow managerComplaintWorkFlow;
    private final AccountRepo accountRepo;
    private final SnapshotPerformanceService snapshotPerformanceService;
    private final EmployeePerformanceService employeePerformanceService;

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

    @GetMapping("/employeses/recommendation")
    public ResponseEntity<?> getEmployeesRecommendation(ConfigFilterDto dto){
        return ResponseEntity.ok(managerComplaintWorkFlow.getEmployeesRecommendation(dto));
    }

























    //    TODO: test and refactoring
    @PostMapping("/employee/addBadge")
    public void addEmployeeBadge(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDateTime to,
            @RequestParam(required = false) String performanceLabel ,
            @RequestParam(required = false) String badge
    ) {

        Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(SecurityContextHolder.getContext().getAuthentication().getName());

        LocalDateTime start = (from == null)
                ? LocalDate.now().minusDays(30).atStartOfDay()
                : from;

        LocalDateTime end = (to == null)
                ? LocalDate.now().plusDays(1).atStartOfDay()
                : to;

        long accountID = (accountId == null)
                ?account.get().getId() : accountId;

        snapshotPerformanceService.manualSnapshotGeneration(accountID,start, end, performanceLabel, badge);
    }

    @GetMapping("/employee/performance")
    public ResponseEntity<?> getEmployeePerformance(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(SecurityContextHolder.getContext().getAuthentication().getName());

        LocalDateTime start = (from == null)
                ? LocalDate.now().minusDays(30).atStartOfDay()
                : from.atStartOfDay();

        LocalDateTime end = (to == null)
                ? LocalDate.now().plusDays(1).atStartOfDay()
                : to.plusDays(1).atStartOfDay();

        long accountID = (accountId == null)
                ?account.get().getId() : accountId;

        return ResponseEntity.ok(employeePerformanceService.getEmployeePerformance(accountID,start, end));
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, org.springframework.beans.TypeMismatchException.class})
    public ResponseEntity<?> handleTypeMismatch(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid date format. Expected format: yyyy-MM-dd",
                "details", ex.getMessage()
        ));
    }


}
