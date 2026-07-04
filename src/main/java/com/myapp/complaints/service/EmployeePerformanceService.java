package com.myapp.complaints.service;

import com.myapp.complaints.BadgeFactory;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.ComplaintResponseProjection;
import com.myapp.complaints.dto.EmployeeBadgeDto;
import com.myapp.complaints.dto.EmployeePerformanceDto;
import com.myapp.complaints.dto.SpecifiedEmployeePerformanceResponseDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmployeePerformanceService {

    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final ComplaintRepo complaintRepo;
    private final EmployeeRepo employeeRepo;
    private final EmployeePerformanceSnapshotRepo snapshotRepo;
    private final AccountRepo accountRepo;
    private final AuthorizationService authorizationService;

    private double getAverageResponseTimeInDays(
            Employee employee,
            LocalDateTime start,
            LocalDateTime end
    ) {


        if(employee.getAccount().getRole().getId() == 2L){
            return getReceptionistAvgResponseTime(employee.getAccount().getId(),start,end);
        }
        if(employee.getAccount().getRole().getId() == 4L || employee.getAccount().getRole().getId() == 3L){
            return getTechnicalAvgResponseTime(employee.getAccount().getId(),start,end);
        }
        else {
            throw new ApiException("nt supported statistic yet",HttpStatus.NOT_FOUND);
        }
    }

    public Object getEmployeeBadges() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(auth.getName());
        if(account.isEmpty())
        {
            throw new ApiException("account not found", HttpStatus.NOT_FOUND);
        }
        List<EmployeePerformanceSnapshot> snapshots =
                snapshotRepo.findByEmployeeAccountIdOrderByComputedAtDesc(account.get().getId());

        return snapshots.stream()
                .map(snapshot -> {

                    List<EmployeeBadgeDto> badges =

                            snapshot.getBadges()
                                    .stream()
                                    .map(b -> new EmployeeBadgeDto(
                                            b.getType(),
                                            b.getTitle(),
                                            b.getDescription(),
                                            b.getLevel(),
                                            b.getIcon()
                                    ))
                                    .toList();

                    return new SpecifiedEmployeePerformanceResponseDto(

                            badges
                    );

                })
                .toList();

//
//        Map<BadgeType, EmployeeBadgeDto> badges = new LinkedHashMap<>();
//
//        if (onlyLatest) {
//
//            for (EmployeePerformanceSnapshot snapshot : snapshots) {
//                if (snapshot.getBadges() == null) continue;
//                for (EmployeeSnapshotBadge b : snapshot.getBadges()) {
//                    // if we already recorded this type, skip (we want the first/newest)
//                    if (badges.containsKey(b.getType())) continue;
//
//                    EmployeeBadgeDto dto = new EmployeeBadgeDto(
//                            b.getType(),
//                            b.getTitle(),
//                            b.getDescription(),
//                            b.getLevel(),
//                            b.getIcon()
//                    );
//
//                    badges.put(b.getType(), dto);
//
//                }
//            }
//            return badges.values().stream().toList();
//        } else
//        {
//            List<EmployeeBadgeDto> allBadges = new ArrayList<>();
//
//            for (EmployeePerformanceSnapshot snapshot : snapshots) {
//                if (snapshot.getBadges() == null) continue;
//                for (EmployeeSnapshotBadge b : snapshot.getBadges()) {
//                    allBadges.add(new EmployeeBadgeDto(
//                            b.getType(), b.getTitle(), b.getDescription(), b.getLevel(), b.getIcon()
//                    ));
//                }
//            }
//            return allBadges;
//        }

    }


    //احصائيات لحظية فقط اي اننا هنا لا نمنح شارات
    public EmployeePerformanceDto getEmployeePerformance(long accountId, LocalDateTime start, LocalDateTime end) {

        Optional<Employee> employee = employeeRepo.findById(accountId);

        if(employee.isEmpty())
        {
            throw new ApiException("employee not found", HttpStatus.NOT_FOUND);
        }

        long incomingComplaintsCount;
        long completedComplaintsCount;
        double avgDays;
        long assignedComplaints;

        if(employee.get().getAccount().getRole().getId() == 2L){

            assignedComplaints = complaintRepo.countAssignedComplaints(accountId);

            //all coming complaints to the institution at specified period
            incomingComplaintsCount = complaintRepo.countCreatedComplaintsBetween(
                    employee.get().getInstitution().getId(),
                    employee.get().getGovernorate().getId(),
                    start,
                    end
            );

            completedComplaintsCount =
                    complaintTracingLogRepo.countHandledComplaintsBetween(accountId, start, end);//rejected+forwarded

        }
        else if (employee.get().getAccount().getRole().getId() == 4L || employee.get().getAccount().getRole().getId() == 3L){

             assignedComplaints = complaintRepo.countAssignedComplaints(accountId);

            //all coming complaints to the institution at specified period
             incomingComplaintsCount = complaintTracingLogRepo.countDistinctComplaintAssignedToAccountBetween(
                    accountId,
                    start,
                    end
            );

            completedComplaintsCount =
                    complaintTracingLogRepo.countResolvedComplaintsBetween(accountId, start, end);

        }
        else {
            throw new ApiException("not supported statistic yet",HttpStatus.NOT_FOUND);
        }

        if (incomingComplaintsCount <= 0) {
            return new EmployeePerformanceDto(
                    accountId,
                    employee.get().getAccount().getEmail(),
                    employee.get().getAccount().getUserName(),
                    incomingComplaintsCount,
                    assignedComplaints,
                    completedComplaintsCount,
                    0,
                    0,
                    Collections.emptyList()
            );

        }

        avgDays =
                getAverageResponseTimeInDays(
                        employee.get(),
                        start,
                        end
                );

        double achievementRate,responseRate;

         if(avgDays == -1.0) {
            return new EmployeePerformanceDto(
                    accountId,
                    employee.get().getAccount().getEmail(),
                    employee.get().getAccount().getUserName(),
                    incomingComplaintsCount,
                    assignedComplaints,
                    completedComplaintsCount,
                    0,
                    0,
                    Collections.emptyList()
            );

        }
        else {
            achievementRate = (double) completedComplaintsCount / incomingComplaintsCount * 100.0;
            responseRate = avgDays;
        }

//        // normalize completedComplaintsCount count against a soft target (e.g., 10 per period)
//        // measures how close the employee is to the expected completedComplaintsCount volume
//        long softTarget = incomingComplaintsCount; //TODO: pass as parameter
//
//        double completionEfficiency;
//
//        if (incomingComplaintsCount <= 0) {
//            completionEfficiency = 0; //
//        }
//        else {
//            double targetEffective = Math.min((double) softTarget, (double) incomingComplaintsCount);
//            completionEfficiency = Math.min((double) completedComplaintsCount / targetEffective, 1.0);
//        }

        // score: combine completionEfficiency (0..1 → 50 pts) and achievementRate (0..100% → 50 pts) into a 0–100 score
//        double score = achievementRate * 2 + (responseRate / 100.0) * 5; // 50/50 weighting

        double score = getScore(responseRate, achievementRate);

        List<EmployeeBadgeDto> badges = List.of(
                BadgeFactory.buildPerformanceBadge(score),
                BadgeFactory.buildResponseBadge(responseRate)
        );

        return new EmployeePerformanceDto(
                accountId,
                employee.get().getAccount().getEmail(),
                employee.get().getAccount().getUserName(),
                incomingComplaintsCount,
                assignedComplaints,
                completedComplaintsCount,
                Math.round(responseRate * 100.0) / 100.0,
                score,
//                completionEfficiency,
                badges
        );    }

    private static double getScore(
            double responseRate,
            double achievementRate
    ) {

        // تحويل response days → score
        double responseScore =
                Math.max(
                        20,
                        100 - (responseRate * 20)
                );

        // وزن الإنجاز والاستجابة
        double score =
                (achievementRate * 0.7)
                        +
                        (responseScore * 0.3);

        return Math.round(score * 100.0) / 100.0;
    }
    public double getTechnicalAvgResponseTime(Long accountId, LocalDateTime start, LocalDateTime end) {

        List<Long> complaintIds =
                complaintTracingLogRepo.findDistinctComplaintIdsAssignedToAccountBetween(accountId, start, end);

        int startedCount = 0;

        if (complaintIds == null || complaintIds.isEmpty()) {
            return -1.0;
        }

        double totalDays = 0.0;

        for (Long complaintId : complaintIds) {
            LocalDateTime assignedAt = complaintTracingLogRepo.findAssignedAt(complaintId, accountId);
            LocalDateTime startedAt = complaintTracingLogRepo.findStartedAt(complaintId, accountId);

            if (assignedAt == null || startedAt == null) continue;

            long millis = Duration.between(assignedAt, startedAt).toMillis();

            totalDays += millis / (1000.0 * 60 * 60 * 24);
            startedCount++;

        }

        if (startedCount == 0) {
            return -1.0;
        }

        double average =
                totalDays / startedCount;

        return Math.round(
                average * 100.0
        ) / 100.0;

    }

    private double getReceptionistAvgResponseTime(Long accountId, LocalDateTime start, LocalDateTime end) {

        List<ComplaintResponseProjection> responses =
                complaintTracingLogRepo.findComplaintResponseTimes(
                        accountId,
                        ComplaintState.IN_REVIEW,
                        start,
                        end
                );

        if (responses.isEmpty()) {
            return 4; // default average response time in days when no data is available
        }

        double totalDays = 0;

        for (ComplaintResponseProjection response : responses) {

            LocalDateTime created = response.getCreatedAt();
            LocalDateTime reviewed = response.getReviewedAt();

            if (created == null || reviewed == null) {
                continue;
            }

            long millis =
                    Duration.between(created, reviewed).toMillis();

            totalDays += millis / (1000.0 * 60 * 60 * 24);
        }

        double average = totalDays / responses.size();

        return Math.round(average * 100.0) / 100.0;
    }

}