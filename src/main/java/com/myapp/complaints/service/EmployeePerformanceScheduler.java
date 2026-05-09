package com.myapp.complaints.service;

import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.entity.Employee;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeePerformanceScheduler {

    private final EmployeeRepo employeeRepo;
    private final StatisticsService statisticsService;

    @Scheduled(cron = "0 0 0 1 * *")
    public void generateMonthlySnapshots() {
        System.out.println("Monthly Scheduler started");
        LocalDateTime start =
                LocalDate.now()
                        .minusMonths(1)
                        .withDayOfMonth(1)
                        .atStartOfDay();

        LocalDateTime end =
                LocalDate.now()
                        .withDayOfMonth(1)
                        .atStartOfDay();

        List<Employee> employees =
                employeeRepo.findAll();

        for (Employee employee : employees) {

            statisticsService.createSnapshotForEmployeePerformance(
                    employee.getAccount().getId(),
                    start,
                    end
            );
        }
    }

    //0 0 0 */3 * * every 3Day
    @Scheduled(cron = "0 * * * * *")
    public void generatePerformanceSnapshots() {

        LocalDateTime end = LocalDateTime.now()
                            .withSecond(0)
                            .withNano(0);
        LocalDateTime start = end.minusMinutes(1);

        List<Employee> employees = employeeRepo.findAll();
        System.out.println("min Scheduler started");
        for (Employee employee : employees) {

            statisticsService.createSnapshotForEmployeePerformance(
                    employee.getAccount().getId(),
                    start,
                    end
            );
        }
    }

}