package com.myapp.complaints.service;

import com.myapp.complaints.BadgeFactory;
import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.EmployeePerformanceSnapshotRepo;
import com.myapp.complaints.dto.EmployeeBadgeDto;
import com.myapp.complaints.dto.EmployeePerformanceDto;
import com.myapp.complaints.entity.EmployeePerformanceSnapshot;
import com.myapp.complaints.entity.EmployeeSnapshotBadge;
import com.myapp.complaints.enums.BadgeLevel;
import com.myapp.complaints.enums.BadgeType;
import com.myapp.complaints.enums.SnapshotSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SnapshotPerformanceService {

    private final StatisticsService statisticsService;
    private final EmployeePerformanceSnapshotRepo snapshotRepo;

    public void scheduledSnapshotGeneration(Long accountId,
                                            LocalDateTime start,
                                            LocalDateTime end) {

        EmployeePerformanceDto dto =
                statisticsService.getEmployeePerformance(accountId, start, end);

        if (dto.comingCount() == 0) {
            return;
        }

        EmployeeBadgeDto performanceBadge =
                dto.badges()
                        .stream()
                        .filter(b -> b.type() == BadgeType.PERFORMANCE)
                        .findFirst()
                        .orElse(null);

        EmployeeBadgeDto responseBadge =
                dto.badges()
                        .stream()
                        .filter(b -> b.type() == BadgeType.RESPONSE)
                        .findFirst()
                        .orElse(null);

        EmployeePerformanceSnapshot snap = EmployeePerformanceSnapshot.builder()
                .employeeAccountId(accountId)
                .periodStart(start)
                .periodEnd(end)
                .comingCount((int) dto.comingCount())
                .handledCount((int) dto.handledCount())
                .responseRate(dto.responseRate())
                .normalizedHandled(dto.normalizedHandled())
                .score(dto.score())
                .computedAt(LocalDateTime.now())
                .source(SnapshotSource.SCHEDULED)
                .build();

        if(snap.getBadges()==null){
            snap.setBadges(List.of(
                    buildBadgeEntity(performanceBadge, snap),
                    buildBadgeEntity(responseBadge, snap)
            ));
        }
        else {
            snap.getBadges().addAll(
                    List.of(
                                    buildBadgeEntity(performanceBadge, snap),
                                    buildBadgeEntity(responseBadge, snap)
                            ).stream()
                            .filter(b -> b != null)
                            .toList()
            );
        }

        Optional<EmployeePerformanceSnapshot> existing =
                snapshotRepo.findByEmployeeAccountIdAndPeriodStartAndPeriodEnd(
                        accountId, start, end
                );
        if (existing.isPresent()) return;
        snapshotRepo.save(snap);
    }

    public void milestoneSnapshotGeneration(Long accountId,
                                            LocalDateTime start,
                                            LocalDateTime end,
                                            int handled,
                                            int all,long milestone
                                            ) {

        EmployeePerformanceSnapshot snap =
                EmployeePerformanceSnapshot.builder()
                        .employeeAccountId(accountId)
                        .periodStart(start)
                        .periodEnd(end)
                        .responseRate((all == 0) ? 0 : (double) handled / all)
                        .normalizedHandled(1.0)
                        .handledCount(handled)
                        .score(CommonUtils.getScoreForThresholdOfSolve(handled))
                        .comingCount(all)
                        .computedAt(LocalDateTime.now())
                        .source(SnapshotSource.MILESTONE)
                        .build();

        EmployeeBadgeDto employeeBadgeDto =BadgeFactory.buildMilestoneBadge(handled, milestone);

        if(snap.getBadges()==null){
            snap.setBadges(List.of(buildBadgeEntity(employeeBadgeDto, snap)));
        }
        else{
            snap.getBadges().add(buildBadgeEntity(employeeBadgeDto, snap));
        }

        Optional<EmployeePerformanceSnapshot> existing =
                snapshotRepo.findByEmployeeAccountIdAndPeriodStartAndPeriodEnd(
                        accountId, start, end
                );
        if (existing.isPresent()) return;
        snapshotRepo.save(snap);
    }

    // TODO: test and refactoring
    public void manualSnapshotGeneration(long accountID,
                                         LocalDateTime start,
                                         LocalDateTime end,
                                         String performanceLabel,
                                         String badge) {

        EmployeePerformanceSnapshot snap =
                EmployeePerformanceSnapshot.builder()
                        .employeeAccountId(accountID)
                        .periodStart(start)
                        .periodEnd(end)
                        .computedAt(LocalDateTime.now())
                        .source(SnapshotSource.MANUAL)
                        .build();

        snap.getBadges().add(
                EmployeeSnapshotBadge.builder()
                        .title(performanceLabel)
                        .description("Manual snapshot")
                        .icon(badge)
                        .level(BadgeLevel.BRONZE)
                        .type(BadgeType.PERFORMANCE)
                        .snapshot(snap)
                        .build()
        );


        snapshotRepo.save(snap);
    }


    private EmployeeSnapshotBadge buildBadgeEntity(EmployeeBadgeDto dto,
                                                   EmployeePerformanceSnapshot snap) {

        if (dto == null) return null;

        return EmployeeSnapshotBadge.builder()
                .title(dto.title())
                .description(dto.description())
                .icon(dto.icon())
                .level(dto.level())
                .type(dto.type())
                .snapshot(snap)
                .build();
    }

}