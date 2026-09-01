package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ComplaintState;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final ComplaintRepo complaintRepo;
    private final InstitutionRepo institutionRepo;
    private final EmployeeRepo employeeRepo;
    private final AuthorizationService authorizationService;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;

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
//            return new ApiResponseDto<>(false,"Not supported Yet",null);
            return new TechnicDashBoardStatisticsDto(
                    complaintRepo.countAssignedComplaints(employee.getId()),
                    complaintRepo.countInProgressComplaints(employee.getId()),
                    getDelayedComplaints(employee).size(),
                    complaintTracingLogRepo.countResolvedComplaints(employee.getId()),
                    complaintRepo.countAssignedComplaints(employee.getId())+
                            complaintRepo.countInProgressComplaints(employee.getId())+
                            getDelayedComplaints(employee).size()+
                            complaintTracingLogRepo.countResolvedComplaints(employee.getId())
            );

        }
        else if (authorizationService.isManager()) {

            return new ManagerDashBoardStatisticsDto(
                    getDelayedComplaints(employee).size(),
                    getCountComplaintsByState(employee,ComplaintState.FORWARDED_TO_MANAGER),
                    getCountComplaintsByState(employee,ComplaintState.ASSIGNED),
                    getCountComplaintsByState(employee,ComplaintState.IN_PROGRESS),
                    getCountComplaintsByState(employee,ComplaintState.RESOLVED),
                    employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                            employee.getInstitution().getId(),
                            employee.getGovernorate().getId(),
                            4
                    ).size()
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

    public List<DelayedComplaintDto> getDelayedComplaints(Employee employee) {

        return complaintRepo.delayedComplaints(employee.getAccount().getId())
                .stream()
                .filter(dc -> {
                    

                    int slaDays = dc.getPriority().getSlaDays();

                    LocalDateTime assignmentDate = dc.getAssignedAt();

                    LocalDateTime deadline = assignmentDate.plusDays(slaDays);

//                    boolean delayed = LocalDateTime.now().isAfter(deadline);
                    return LocalDateTime.now().isAfter(deadline);
                })
                .map(dc -> {

                    int slaDays = dc.getPriority().getSlaDays();

                    LocalDateTime deadline =
                            dc.getAssignedAt().plusDays(slaDays);

                    double delayedDays =
                            Math.max(0,
                                    Math.round(
                                            Duration.between(deadline, LocalDateTime.now())
                                                    .toHours() / 24.0 * 100
                                    ) / 100.0
                            );

                    Optional<Complaint> complaint = complaintRepo.findByIdAndDeletedFalse(dc.getComplaintId());

                    return new DelayedComplaintDto(
                            employee.getId(),
                            employee.getAccount().getEmail(),
                            employee.getAccount().getUserName(),
                            dc.getComplaintId(),
                            dc.getTitle(),
                            dc.getPriority(),
                            dc.getAssignedAt(),
                            dc.getState(),
                            delayedDays,
                            new LocationDto(
                                    complaint.get().getGovernorate().getId(),
                                    complaint.get().getGovernorate().getName(),

                                    complaint.get().getSector().getId(),
                                    complaint.get().getSector().getName(),

                                    new AddressDto(
                                            complaint.get().getAddress().getId(),
                                            complaint.get().getAddress().getFullAddressText(),
                                            complaint.get().getAddress().getLongitude(),
                                            complaint.get().getAddress().getLatitude()
                                    )
                            )
                    );
                })
                .toList();
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
