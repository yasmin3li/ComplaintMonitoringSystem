package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ComplaintRepo complaintRepo;
    private final InstitutionRepo institutionRepo;
    private final EmployeeRepo employeeRepo;
    private final AuthorizationService authorizationService;

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

//    public long getComplaintForThisDay() {
//        LocalDate localDateTime=LocalDate.now();
//        return complaintRepo.countTodayComplaints(localDateTime);
//    }

    public long countTodayComplaints() {

        LocalDateTime startOfToday =
                LocalDate.now().atStartOfDay();//00:00

        LocalDateTime endOfToday =
                startOfToday.plusDays(1);//tomorrow

        return complaintRepo.countByDateTimeOfAddBetween(
                startOfToday,
                endOfToday
        );
    }

    //Citizen Info
    public long getTotalCitizenComplaint(String email) {
        return complaintRepo.countByAddedBy_EmailAndDeletedFalse(email);
    }
//    countByStateAndDeleteFalseAndAddedBy_Id

    public long getCitizenInProgressComplaints(String email) {
        return complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.IN_PROGRESS, email);
    }

    public long getCitizenSolvedComplaints(String email) {
        return complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.RESOLVED, email);
    }

    public double completionRate(String email) {
        long completionSum = complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.RESOLVED, email);
        long total = complaintRepo.countByAddedBy_EmailAndDeletedFalse(email);
        if (total == 0) return 0;
        return (double) completionSum / total * 100;
    }

    public CitizenDashBoardStatisticsDto getCitizenDashboardStatistics(String email) {

        return new CitizenDashBoardStatisticsDto(
                getTotalCitizenComplaint(email),
                getCitizenInProgressComplaints(email),
                getCitizenSolvedComplaints(email),
                completionRate(email)
        );
    }

    public Object getEmployeeDashboardStatistics() {

        Employee employee = employeeRepo.findByAccount_Email(
                SecurityContextHolder.getContext().getAuthentication().getName());

        if(authorizationService.isTechnic()){

//            TODO: correct logic
            return new ApiResponseDto<>(false,"Not supported Yet",null);

        }
        else if (authorizationService.isManager()) {

            return new ManagerDashBoardStatisticsDto(
                    getDelayedComplaints(employee),
                    getCountComplaintsByState(employee,ComplaintState.FORWARDED_TO_MANAGER),
                    getCountComplaintsByState(employee,ComplaintState.ASSIGNED),
                    getCountComplaintsByState(employee,ComplaintState.IN_PROGRESS),
                    getCountComplaintsByState(employee,ComplaintState.RESOLVED)
            );

        }
        else
            return new ReceptionistDashBoardStatisticsDto(
                    getCountComplaintsByState(employee,ComplaintState.NEW),
                    getInReviewComplaints(employee),
                    getForwardedComplaints(employee),
                    getRejected(employee)
            );
    }

    private long getDelayedComplaints(Employee manager) {

        long threshold =3;
        long delayedComplaintCount =0;
        List<Employee> employees =
                employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                        manager.getInstitution().getId(),
                        manager.getGovernorate().getId(),
                        4L
                );

        for (Employee employee : employees) {

             delayedComplaintCount =delayedComplaintCount+
                    complaintRepo.delayedComplaints(
                                    employee.getAccount().getId(),
                                    LocalDateTime.now().minusDays(threshold)
                            )
                            .size();
        }
        return delayedComplaintCount;
    }


    private long getCountComplaintsByState(Employee employee, ComplaintState state) {
        return complaintRepo.countByStateAndGovernorate_IdAndInstitution_IdAndSector_Id(
                state,
                employee.getGovernorate().getId(),
                employee.getInstitution().getId(),
                employee.getSector().getId());
    }

    private long getInReviewComplaints(Employee employee) {
        return complaintRepo.countComplaintsByStateForEmployee(
                ComplaintState.IN_REVIEW,
                employee.getAccount().getId(),
                employee.getGovernorate().getId(),
                employee.getInstitution().getId(),
                employee.getSector().getId()
        );
    }

    private long getRejected(Employee employee) {
        return complaintRepo.countComplaintsByStateForEmployee(
                ComplaintState.REJECTED,
                employee.getAccount().getId(),
                employee.getGovernorate().getId(),
                employee.getInstitution().getId(),
                employee.getSector().getId()
        );
    }

    private long getForwardedComplaints(Employee employee) {
        return complaintRepo.countComplaintsByStateForEmployee(
                ComplaintState.FORWARDED_TO_MANAGER,
                employee.getAccount().getId(),
                employee.getGovernorate().getId(),
                employee.getInstitution().getId(),
                employee.getSector().getId()
        );
    }

//
//    public List<ComplaintResponseDto> getAllComplaintsForCitizen(String email){
//        return complaintRepo.findByAddedBy_Email(email)
//                .stream()
//                .map(complaintMapper::toDto)
//                .toList();
//    }
//
//    public List<ComplaintResponseDto> getTop3ComplaintsForCitizen(String email){
//        return complaintRepo.findTop3ByAddedBy_EmailAndDeletedFalse(email)
//                .stream()
//                .map(complaintMapper::toDto)
//                .toList();
//    }
//    public CitizenDashboardResponseDto buildCitizenDashboardResponse(String email){
//        return new CitizenDashboardResponseDto(
//                getCitizenDashboardStatistics(email),
////                getAllComplaintsForCitizen(email)
//                getTop3ComplaintsForCitizen(email)
//        );
//    }

}
