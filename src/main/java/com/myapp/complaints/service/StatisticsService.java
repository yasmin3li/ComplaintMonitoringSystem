package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.CitizenDashBoardStatisticsDto;
import com.myapp.complaints.dto.EmployeeDashBoardStatisticsDto;
import com.myapp.complaints.dto.EmployeePerformanceBadges;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.myapp.complaints.dto.EmployeePerformanceDto;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ComplaintRepo complaintRepo;
    private final InstitutionRepo institutionRepo;
    private final EmployeeRepo employeeRepo;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final EmployeePerformanceSnapshotRepo snapshotRepo;
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

    public EmployeeDashBoardStatisticsDto getEmployeeDashboardStatistics() {

        Employee employee = employeeRepo.findByAccount_Email(
                SecurityContextHolder.getContext().getAuthentication().getName());

        return new EmployeeDashBoardStatisticsDto(
                getTotalNewComplaints(employee),
                getInReviewComplaints(employee),
                getForwardedComplaints(employee),
                getRejected(employee)
        );
    }


    //احصائيات لحظية فقط اي اننا هنا لا نمنح شارات
    public EmployeePerformanceDto getEmployeePerformance(long accountId, LocalDateTime start, LocalDateTime end) {

        Optional<Employee> employee = employeeRepo.findById(accountId);

        //all coming complaints to the institution at specified period
        long incomingComplaintsCount = complaintRepo.countCreatedComplaintsBetween(
                employee.get().getInstitution().getId(),
                employee.get().getGovernorate().getId(),
                start,
                end
        );

//        TODO: replace later with responseTime
        long respondedComplaintsCount = complaintTracingLogRepo.countComplaintAssignedToAccountBetween(accountId, start, end);

        long completedComplaintsCount =  complaintTracingLogRepo.countHandledComplaintsBetween(accountId, start, end);//rejected+forwarded

        double achievementRate,activityCount;

        if (incomingComplaintsCount <= 0) {
            achievementRate = 100.0;
            activityCount = 100.0;
        } else {
            achievementRate = (double) completedComplaintsCount / incomingComplaintsCount * 100.0;
            activityCount = (double) respondedComplaintsCount / incomingComplaintsCount * 100.0;
        }

        // normalize completedComplaintsCount count against a soft target (e.g., 10 per period)
        // measures how close the employee is to the expected completedComplaintsCount volume
        int softTarget = 10; //TODO: pass as parameter
        double completionEfficiency;
        if (incomingComplaintsCount <= 0) {
            // لا شكاوى واردة: نعتبر الموظف غير مسؤول عن تقصير الهدف
            completionEfficiency = 1; //
        } else {
            double targetEffective = Math.min((double) softTarget, (double) incomingComplaintsCount);
            completionEfficiency = Math.min((double) completedComplaintsCount / targetEffective, 1.0);
        }
        // score: combine completionEfficiency (0..1 → 50 pts) and achievementRate (0..100% → 50 pts) into a 0–100 score
        double score = completionEfficiency * 50.0 + (achievementRate / 100.0) * 50.0; // 50/50 weighting

        String performanceLabel = CommonUtils.getPerformanceLabel(score);
        String responseLabel = CommonUtils.getResponseLabel(activityCount);
        String badge = CommonUtils.getBadgeKey(score);

        return new EmployeePerformanceDto(accountId, incomingComplaintsCount, respondedComplaintsCount, achievementRate, score, performanceLabel, responseLabel, badge, completionEfficiency);
    }


    private long getTotalNewComplaints(Employee employee) {
        return complaintRepo.countByStateAndGovernorate_IdAndInstitution_IdAndSector_Id(
                ComplaintState.NEW,
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

    public Object getEmployeeBadges(Long accountId) {

        List<EmployeePerformanceSnapshot> snapshots =
                snapshotRepo.findByEmployeeAccountIdOrderByComputedAtDesc(accountId);

        if (authorizationService.isManager()){

        return snapshots.stream()
                .map(snapshot -> new EmployeePerformanceDto(
                        snapshot.getEmployeeAccountId(),
                        snapshot.getCreatedCount(),
                        snapshot.getAssignedCount(),
                        snapshot.getResponseRate(),
                        snapshot.getScore(),
                        snapshot.getPerformanceLabel(),
                        snapshot.getResponseLabel(),
                        snapshot.getBadge(),
                        snapshot.getNormalizedHandled()
                ))
                .toList();
    }
        else{
            return snapshots.stream()
                    .map(snapshot -> new EmployeePerformanceBadges(
                            snapshot.getPerformanceLabel(),
                            snapshot.getResponseLabel(),
                            snapshot.getBadge()
                    ))
                    .toList();
        }
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
