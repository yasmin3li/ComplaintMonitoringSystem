package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.CitizenDashBoardStatisticsDto;
import com.myapp.complaints.dto.EmployeeDashBoardStatisticsDto;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import com.myapp.complaints.enums.ComplaintState;
import jakarta.transaction.Transactional;
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
    public long getTotalCitizenComplaint(String email){
        return complaintRepo.countByAddedBy_EmailAndDeletedFalse(email);
    }
//    countByStateAndDeleteFalseAndAddedBy_Id

    public long getCitizenInProgressComplaints(String email){
        return complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.IN_PROGRESS,email);
    }

    public long getCitizenSolvedComplaints(String email){
        return complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.RESOLVED,email);
    }

    public double completionRate(String email){
        long completionSum = complaintRepo.countByStateAndAddedBy_EmailAndDeletedFalse(ComplaintState.RESOLVED, email);
        long total = complaintRepo.countByAddedBy_EmailAndDeletedFalse(email);
        if(total == 0) return 0;
        return  (double) completionSum / total * 100;
    }

    public CitizenDashBoardStatisticsDto getCitizenDashboardStatistics(String email){

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

//    TODO: pass source as param/enum
    @Transactional
    public void createSnapshotForEmployeePerformance(Long accountId,  LocalDateTime start,  LocalDateTime end) {
//
//        Optional<EmployeePerformanceSnapshot> existing =
//                snapshotRepo.findByEmployeeAccountIdAndPeriodStartAndPeriodEnd(
//                        accountId,
//                        start,
//                        end
//                );
//
//        if (existing.isPresent()) {
//            return;
//        }

        EmployeePerformanceDto dto =
                getEmployeePerformance(accountId, start, end);

        EmployeePerformanceSnapshot snap =
                EmployeePerformanceSnapshot.builder()
                        .employeeAccountId(accountId)
                        .periodStart(start)
                        .periodEnd(end)
                        .createdCount((int) dto.createdCount())
                        .assignedCount((int) dto.assignedCount())
                        .responseRate(dto.responseRate())
                        .normalizedHandled(dto.normalizedHandled())
                        .score(dto.score())
                        .badge(dto.badge())
                        .performanceLabel(dto.performanceLabel())
                        .computedAt(LocalDateTime.now())
                        .responseLabel(dto.responseLabel())
                        .source("scheduled")
                        .build();

        snapshotRepo.save(snap);
    }
//احصائيات لحظية فقط اي اننا هنا لا نمنح شارات
    public EmployeePerformanceDto getEmployeePerformance(long accountId,LocalDateTime start, LocalDateTime end) {

        Optional<Employee> employee = employeeRepo.findById(accountId);

        //created and updated action
        long createdCount = complaintTracingLogRepo.countComplaintsWithNewStateByInstitutionAndGovernorateBetween(
                employee.get().getInstitution().getId(),
                employee.get().getGovernorate().getId(),
                start,
                end
        );

        long assignedCount = complaintTracingLogRepo.countComplaintAssignedToAccountBetween(accountId, start, end);

        double responseRate;
        if (createdCount <= 0) {
            responseRate = 100.0;
        } else {
            responseRate = (double) assignedCount / createdCount * 100.0;
            responseRate = Math.min(responseRate, 100.0); // cap at 100%
        }

        // normalize handled count against a soft target (e.g., 10 per period)
        // measures how close the employee is to the expected handled volume
        int softTarget = 10;
        double normalizedHandled;
        if (createdCount <= 0) {
            // لا شكاوى واردة: نعتبر الموظف غير مسؤول عن تقصير الهدف
            normalizedHandled = 1; //
        } else {
            double targetEffective = Math.min((double) softTarget, (double) createdCount);
            normalizedHandled = Math.min((double) assignedCount / targetEffective, 1.0);
        }
        // score: combine normalizedHandled (0..1 → 50 pts) and responseRate (0..100% → 50 pts) into a 0–100 score
        double score = normalizedHandled * 50.0 + (responseRate / 100.0) * 50.0; // 50/50 weighting

        String performanceLabel = CommonUtils.getPerformanceLabel(score);
        String responseLabel = CommonUtils.getResponseLabel(responseRate);
        String badge = CommonUtils.getBadgeKey(score);

//        return employeePerformanceSnapshotRepo.findByEmployeeAccountIdOrderByPeriodStartDesc(accountId);

        return new EmployeePerformanceDto(accountId, createdCount, assignedCount, responseRate, score,performanceLabel, responseLabel,badge,normalizedHandled);
    }



    private long getTotalNewComplaints(Employee employee) {
        return complaintRepo.countByStateAndGovernorate_IdAndInstitution_IdAndSector_Id(
                ComplaintState.NEW,
                employee.getGovernorate().getId(),
                employee.getInstitution().getId(),
                employee.getSector().getId() );
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

    public List<EmployeePerformanceDto> getEmployeeBadges(Long accountId) {

        List<EmployeePerformanceSnapshot> snapshots =
                snapshotRepo.findByEmployeeAccountIdOrderByComputedAtDesc(accountId);

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
