package com.myapp.complaints.service;

import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.DAO.EmployeeMilestoneRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.entity.EmployeeMilestone;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EmployeePerformanceScheduler {

    private final EmployeeRepo employeeRepo;
    private final StatisticsService statisticsService;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final SnapshotPerformanceService snapshotPerformanceService;

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

            snapshotPerformanceService.scheduledSnapshotGeneration(
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

            snapshotPerformanceService.scheduledSnapshotGeneration(
                    employee.getAccount().getId(),
                    start,
                    end
            );
        }
    }

    private final ComplaintRepo complaintRepo;
    private final EmployeeMilestoneRepo employeeMilestoneRepo;

    @Scheduled(cron = "0 * * * * *")
    public void reachedToX() {


        LocalDateTime end = LocalDateTime.now()
                .withSecond(0)
                .withNano(0);

        LocalDateTime start;
        long assignedCount;
        List<Employee> employees = employeeRepo.findAll();

        for (Employee employee : employees) {

            Optional<EmployeeMilestone> employeeMilestone = employeeMilestoneRepo.findByEmployee_Id(employee.getAccount().getId());

                   if(employeeMilestone.isPresent()) {
                       EmployeeMilestone milestone = employeeMilestone.get();

                       if (milestone.getStart() == null) {
                           start = complaintTracingLogRepo.findFirstHandledByEmp(employee.getAccount().getId());
                           milestone.setStart(start);
                           employeeMilestoneRepo.save(milestone);
                       } else {
                           start = milestone.getStart();
                       }

                          assignedCount = complaintTracingLogRepo.countHandledComplaintsBetween(employee.getAccount().getId(), start, end);

                       if (assignedCount >= milestone.getNextMilestone()) {

                           int all =(int)complaintRepo.
                                   countCreatedComplaintsBetween(
                                           employee.getInstitution().getId(),
                                           employee.getGovernorate().getId(),
                                           start,
                                           end
                                   );

                           snapshotPerformanceService.milestoneSnapshotGeneration(
                                   employee.getAccount().getId(),
                                   start,
                                   end,
                                   (int)assignedCount,
                                   all ,
                                   "Blue",
                                   "أداء جيد لقد وصلت الى معالجة "+assignedCount+" شكوى"
                           );

                           // Update the next milestone
                           milestone.setNextMilestone(milestone.getNextMilestone() * 2);
                           milestone.setStart(end);
                           employeeMilestoneRepo.save(milestone);
                       }
                   } else {
                       // If no milestone record exists, create one with the initial milestone value
                       EmployeeMilestone newMilestone = new EmployeeMilestone();
                       newMilestone.setEmployee(employee);
                       newMilestone.setNextMilestone(2L); // Initial milestone value
                       employeeMilestoneRepo.save(newMilestone);
            }

            }
        }

}