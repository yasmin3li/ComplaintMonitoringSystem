package com.myapp.complaints.controller;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class ManagerDashboard {

    private final ApiService apiService;
    private final ManagerComplaintWorkFlow managerComplaintWorkFlow;
    private final EmployeePerformanceService employeePerformanceService;
    private final StatisticsService statisticsService;
    private final EmployeeRepo employeeRepo;

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

    @GetMapping("/employees/recommendation")
    public ResponseEntity<?> getEmployeesRecommendation(ConfigFilterDto dto){
        return ResponseEntity.ok(managerComplaintWorkFlow.getEmployeesRecommendation(dto));
    }

    @PostMapping("/complaint/reAssign")
    public ResponseEntity<?> reAssignComplaint(
            @RequestBody AssignComplaintDto assignComplaintDto
    ){
        return ResponseEntity.ok(managerComplaintWorkFlow.reAssignComplaintToEmployee(assignComplaintDto));
    }


@GetMapping("/employee/performance")
public Object getEmployeePerformance(
        @RequestParam(required = false) Long accountId,
        ConfigFilterDto dto) {

    LocalDateTime end,start;

    if (dto.start() == null || dto.end() == null) {

        end = LocalDateTime.now();
        start = end.minusMonths(1);

    }else {
        start = dto.start();
        end = dto.end();
    }

    Employee account =
            employeeRepo.findByAccount_Email(SecurityContextHolder.getContext().getAuthentication().getName());

    List<Object> employeePerformanceDtoList = new ArrayList<>();


    if(accountId == null){

        List<Employee> employeeList = employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                account.getInstitution().getId(),
                account.getGovernorate().getId(),
                4L
        );

        for(Employee employee : employeeList){
            EmployeePerformanceDto performanceDto =
                    employeePerformanceService.getEmployeePerformance(
                            employee.getAccount().getId(),
                            start,
                            end
                    );

            List<DelayedComplaintDto> delayedComplaintDtoList =statisticsService.getDelayedComplaints(employee);

            employeePerformanceDtoList.addAll(
                    List.of(
                            new EmployeePerformanceForManager(
                                    performanceDto,
                                    delayedComplaintDtoList
                            )
                    )
            );
        }

        return ResponseEntity.ok(employeePerformanceDtoList);
    }
    else {
        return ResponseEntity.ok(employeePerformanceService.getEmployeePerformance(accountId,start,end));
    }

}

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, org.springframework.beans.TypeMismatchException.class})
    public ResponseEntity<?> handleTypeMismatch(Exception ex) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Invalid date format. Expected format: yyyy-MM-dd",
                "details", ex.getMessage()
        ));
    }

    @GetMapping("/urgentComplaints")
    public ResponseEntity<?> getUrgentComplaints(){
        return ResponseEntity.ok(managerComplaintWorkFlow.getUrgentComplaints());
    }

}
