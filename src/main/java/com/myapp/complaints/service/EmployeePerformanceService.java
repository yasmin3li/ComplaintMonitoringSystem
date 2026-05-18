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
import com.myapp.complaints.entity.EmployeeSnapshotBadge;
import com.myapp.complaints.enums.BadgeType;
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

    private double getAverageResponseTimeInDays(
            Long accountId,
            LocalDateTime start,
            LocalDateTime end
    ) {

        List<ComplaintResponseProjection> responses =
                complaintTracingLogRepo.findComplaintResponseTimes(
                        accountId,
                        ComplaintState.IN_REVIEW,
                        start,
                        end
                );

        if (responses.isEmpty()) {
            return 4.0; // default average response time in days when no data is available
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

    //احصائيات لحظية فقط اي اننا هنا لا نمنح شارات
    public EmployeePerformanceDto getEmployeePerformance(long accountId, LocalDateTime start, LocalDateTime end) {

        Optional<Employee> employee = employeeRepo.findById(accountId);
        if(employee.isEmpty())
        {
            throw new ApiException("employee not found", HttpStatus.NOT_FOUND);
        }

        //all coming complaints to the institution at specified period
        long incomingComplaintsCount = complaintRepo.countCreatedComplaintsBetween(
                employee.get().getInstitution().getId(),
                employee.get().getGovernorate().getId(),
                start,
                end
        );

//        TODO: replace later with responseTime

        long completedComplaintsCount =  complaintTracingLogRepo.countHandledComplaintsBetween(accountId, start, end);//rejected+forwarded

        double avgDays =
                getAverageResponseTimeInDays(
                        accountId,
                        start,
                        end
                );

        double achievementRate,responseRate;

        if (incomingComplaintsCount <= 0) {
            achievementRate = 0.0;
            responseRate = 0.0;
        }
        else {
            achievementRate = (double) completedComplaintsCount / incomingComplaintsCount * 100.0;
            responseRate = avgDays;
        }

        // normalize completedComplaintsCount count against a soft target (e.g., 10 per period)
        // measures how close the employee is to the expected completedComplaintsCount volume
        int softTarget = 10; //TODO: pass as parameter

        double completionEfficiency;

        if (incomingComplaintsCount <= 0) {
            completionEfficiency = 1; //
        }
        else {
            double targetEffective = Math.min((double) softTarget, (double) incomingComplaintsCount);
            completionEfficiency = Math.min((double) completedComplaintsCount / targetEffective, 1.0);
        }

        // score: combine completionEfficiency (0..1 → 50 pts) and achievementRate (0..100% → 50 pts) into a 0–100 score
        double score = completionEfficiency * 50.0 + (responseRate / 100.0) * 50.0; // 50/50 weighting

        List<EmployeeBadgeDto> badges = List.of(
                BadgeFactory.buildPerformanceBadge(score),
                BadgeFactory.buildResponseBadge(responseRate)
        );

        return new EmployeePerformanceDto(
                accountId,
                incomingComplaintsCount,
                completedComplaintsCount,
                achievementRate,
                score,
                completionEfficiency,
                badges
        );    }

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

}