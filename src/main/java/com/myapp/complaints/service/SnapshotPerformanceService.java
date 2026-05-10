package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.EmployeePerformanceSnapshotRepo;
import com.myapp.complaints.dto.EmployeePerformanceDto;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import com.myapp.complaints.enums.SnapshotSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SnapshotPerformanceService {

    private final StatisticsService statisticsService;
    private final EmployeePerformanceSnapshotRepo snapshotRepo;

    public void scheduledSnapshotGeneration(Long accountId, LocalDateTime start, LocalDateTime end) {
        EmployeePerformanceDto dto =
                statisticsService.getEmployeePerformance(accountId, start, end);

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
                        .source(SnapshotSource.SCHEDULED)
                        .build();

        snapshotRepo.save(snap);
    }

    public void milestoneSnapshotGeneration(Long accountId, LocalDateTime start, LocalDateTime end,
                                            int handled,int all,  String badge, String performanceLabel
                                            ) {

        EmployeePerformanceSnapshot snap =
                EmployeePerformanceSnapshot.builder()
                        .employeeAccountId(accountId)
                        .periodStart(start)
                        .periodEnd(end)
                        .responseRate((double)handled/all)
                        //achieve the gol
                        .normalizedHandled(1.0)
                        .assignedCount(handled)
                        .badge(badge)
                        .score(CommonUtils.getScoreForThresholdOfSolve(handled))
                        .createdCount(all)
                        .performanceLabel(performanceLabel)
                        .computedAt(LocalDateTime.now())
                        .source(SnapshotSource.MILESTONE)
                        .build();

        snapshotRepo.save(snap);
    }

//    TODO: test and refactoring
    public void manualSnapshotGeneration(long accountID, LocalDateTime start, LocalDateTime end, String performanceLabel, String badge) {
        EmployeePerformanceSnapshot snap =
                EmployeePerformanceSnapshot.builder()
                        .employeeAccountId(accountID)
                        .periodStart(start)
                        .periodEnd(end)
                        .badge(badge)
                        .performanceLabel(performanceLabel)
                        .computedAt(LocalDateTime.now())
                        .source(SnapshotSource.MANUAL)
                        .build();

        snapshotRepo.save(snap);
    }

}
