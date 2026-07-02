package com.myapp.complaints.service;

import com.myapp.complaints.BadgeFactory;
import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.complaintStateHandler.ComplaintStateValidator;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.annotation.Nonnull;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerComplaintWorkFlow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final ComplaintStateValidator validator;
    private final ComplaintWorkflowEngine workflowEngine;
    private final NotificationService notificationService;
    private final AuthorizationService authorizationService;
    private final ComplaintTracingLogRepo logRepo;
    private final StatisticsService statisticsService;
    private final ComplaintTracingLogRepo tracingLogRepo;

    public List<ManagerComplaintResponseDto> getInstitutionComplaints(
            ComplaintFilterRequestDto filter
    ) {

        String email =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getName();

        Employee employee =
                employeeRepo.findByAccount_Email(email);

        Specification<Complaint> spec =
                (root, query, cb) -> {

                    List<Predicate> predicates =
                            new ArrayList<>();

                    predicates.add(
                            cb.equal(
                                    root.get("deleted"),
                                    false
                            )
                    );

                    predicates.add(
                            cb.equal(
                                    root.get("governorate")
                                            .get("id"),
                                    employee
                                            .getGovernorate()
                                            .getId()
                            )
                    );

                    if(filter.employeeId() != null){
                        predicates.add(
                                cb.equal(
                                        root.get("assignedTo")
                                                .get("id"),
                                        filter.employeeId()
                                )
                        );
                    }

                    predicates.add(
                            cb.equal(
                                    root.get("institution")
                                            .get("id"),
                                    employee
                                            .getInstitution()
                                            .getId()
                            )
                    );

                    if (filter.state() != null) {

                        ComplaintState complaintState =
                                CommonUtils.fromArabicState(
                                        filter.state()
                                );

                        predicates.add(
                                cb.equal(
                                        root.get("state"),
                                        complaintState
                                )
                        );

                    } else {

                        predicates.add(
                                cb.equal(
                                        root.get("state"),
                                        ComplaintState
                                                .FORWARDED_TO_MANAGER
                                )
                        );
                    }

                    query.orderBy(
                            cb.desc(
                                    root.get(
                                            "dateTimeOfAdd"
                                    )
                            )
                    );

                    return cb.and(
                            predicates.toArray(
                                    new Predicate[0]
                            )
                    );
                };

        int page =
                filter.page() == null
                        ? 0
                        : filter.page();

        int size =
                filter.size() == null
                        ? 100
                        : filter.size();

        return complaintRepo.findAll(
                        spec,
                        PageRequest.of(page, size)
                )
                .stream()
                .map(complaintMapper::toManagerComplaintDto)
                .toList();
    }


    @Transactional
    public ManagerComplaintResponseDto openComplaint(long complaintId) {

        Employee employee =
                employeeRepo.findByAccount_Email(  SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(complaintId).orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        ComplaintState complaintState = complaint.getState();
        if(complaintState.equals(ComplaintState.NEW) ||
                complaintState.equals(ComplaintState.IN_REVIEW)
        ){
            throw new ApiException("this complaint is not in your responsibility",HttpStatus.FORBIDDEN);
        }

        if (!authorizationService.checkAccessibility(employee,complaint)) {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        workflowEngine.createActionLog(complaint, employee.getAccount(), ActionType.OPENED);

        return complaintMapper
                .toManagerComplaintDto(complaint);

    }


    @Transactional
    public ApiResponseDto<?> assignComplaintToEmployee(
            AssignComplaintDto dto
    ) {

        Employee manager =
                employeeRepo.findByAccount_Email(SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(dto.complaintId())
                        .orElseThrow(() ->
                                new ApiException(
                                        "Complaint not found",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        validator.validate(
                complaint.getState(),
                ComplaintState.ASSIGNED
        );

        if (!authorizationService
                .checkAccessibility(manager, complaint))
        {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        Employee assignedEmployee =
                employeeRepo.findById(dto.assignedTo())
                        .orElseThrow(() ->
                                new ApiException(
                                        "Employee not found",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        if (!assignedEmployee.getInstitution().getId().equals(complaint.getInstitution().getId())||
                !assignedEmployee.getGovernorate().getId().equals(complaint.getGovernorate().getId())
        ) {

            throw new ApiException(
                    "Employee does not belong to complaint institution",
                    HttpStatus.BAD_REQUEST
            );
        }

        workflowEngine.changeState(
                complaint,
                ComplaintState.ASSIGNED,
                manager.getAccount(),
                assignedEmployee,
                null,
                ActionType.ASSIGNED
        );

        notificationService.notifyUsers(complaint,"no thing",List.of(assignedEmployee.getAccount()));

        return new ApiResponseDto<>(
                true,
                "Complaint assigned successfully",
                null
        );
    }

    @Transactional
    public ApiResponseDto<?> assignToMyself(long complaintId) {

        Employee manager =
                employeeRepo.findByAccount_Email(SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(complaintId) .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));


        if (!authorizationService.checkAccessibility(manager,complaint)) {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        workflowEngine.changeState(
                complaint,
                ComplaintState.ASSIGNED,
                manager.getAccount(),
                manager,
                null,
                ActionType.ASSIGNED
        );

        return new ApiResponseDto<>(
                true,
                "تم إسناد الشكوى لك بنجاح",
                null
        );
    }

    public List<ManagerEmployeeRecommendationDto> getEmployeesRecommendation(
            ConfigFilterDto dto
    ) {

        LocalDateTime end,start;

        if (dto.start() == null || dto.end() == null) {

            end = LocalDateTime.now();
            start = end.minusMonths(1);

        }else {
            start = dto.start();
            end = dto.end();
        }

        Employee manager =
                employeeRepo.findByAccount_Email(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName()
                );

        List<Employee> employees =
                employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                        manager.getInstitution().getId(),
                        manager.getGovernorate().getId(),
                        4L
                );

        List<ManagerEmployeeRecommendationDto> result = new ArrayList<>();

        for (Employee employee : employees) {

            long assignedTasks =
                    complaintRepo.countAssignedComplaints(
                            employee.getAccount().getId()
                    );

            long inProgressTasks =
                    complaintRepo.countInProgressComplaints(
                            employee.getAccount().getId()
                    );

            long resolved =
                    logRepo.countResolvedComplaintsBetween(
                            employee.getAccount().getId(),
                            start,
                            end
                    );

            List<DelayedComplaintDto> delayedComplaintDtoList = statisticsService.getDelayedComplaints(employee);
//            EmployeePerformanceDto performance =
//                    performanceService.getEmployeePerformance(
//                            employee.getId(),
//                            start,
//                            end
//                    );

            long inComingComplaints =
                    tracingLogRepo.countIncomingComplaintsBetween(
                            start,
                            end,
                            employee.getGovernorate().getId(),
                            employee.getInstitution().getId()
                    );

            double loadRatio =
                    inComingComplaints == 0
                            ? 0
                            : ((double)(inProgressTasks * 1.5 + assignedTasks) * 100)
                            / inComingComplaints;


            LoadTagDto loadTagDto = BadgeFactory.buildRecommendationBadge(loadRatio);

            result.add(
                    new ManagerEmployeeRecommendationDto(
                            employee.getId(),
                            employee.getAccount().getUserName(),
                            assignedTasks,
                            inProgressTasks,
                            resolved,
                            delayedComplaintDtoList,
                            loadTagDto
//                            performance.responseRate(),
//                            performance.score(),
//                            performance.badges()
                    )
            );
        }

        result.sort(
                Comparator
                        .comparingDouble(
                                (ManagerEmployeeRecommendationDto e) ->
                                        e.employeeBadgeDto().loadRatio()
                        )
                        .thenComparing(
                                Comparator.comparingLong(
                                        ManagerEmployeeRecommendationDto::resolvedComplaints
                                )
                        )
        );

        if (!result.isEmpty()) {

            ManagerEmployeeRecommendationDto updatedEmployee = getManagerEmployeeRecommendationDto(result);

            result.set(0, updatedEmployee);
        }

        return result;
    }

    @Nonnull
    private static ManagerEmployeeRecommendationDto getManagerEmployeeRecommendationDto(List<ManagerEmployeeRecommendationDto> result) {
        ManagerEmployeeRecommendationDto leastLoaded =
                result.get(0);

        LoadTagDto oldBadge =
                leastLoaded.employeeBadgeDto();

        LoadTagDto updatedBadge =
                new LoadTagDto(
                        oldBadge.type(),
                        oldBadge.title(),
                        "الأقل حمل عمل - موصى به بشدة",
                        oldBadge.level(),
                        "star",
                        oldBadge.loadRatio()
                );

        ManagerEmployeeRecommendationDto updatedEmployee =
                new ManagerEmployeeRecommendationDto(
                        leastLoaded.employeeId(),
                        leastLoaded.employeeName(),
                        leastLoaded.assignedTasks(),
                        leastLoaded.inProgressTasks(),
                        leastLoaded.resolvedComplaints(),
                        leastLoaded.delayedComplaints(),
                        updatedBadge
                );
        return updatedEmployee;
    }

    @Transactional
    public ApiResponseDto<?> reAssignComplaintToEmployee(
            AssignComplaintDto dto
    ) {

        Employee manager =
                employeeRepo.findByAccount_Email(SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(dto.complaintId())
                        .orElseThrow(() ->
                                new ApiException(
                                        "Complaint not found",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        if (!authorizationService
                .checkAccessibility(manager, complaint))
        {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        Employee assignedEmployee =
                employeeRepo.findById(dto.assignedTo())
                        .orElseThrow(() ->
                                new ApiException(
                                        "Employee not found",
                                        HttpStatus.NOT_FOUND
                                )
                        );

        if (!assignedEmployee.getInstitution().getId().equals(complaint.getInstitution().getId())||
                !assignedEmployee.getGovernorate().getId().equals(complaint.getGovernorate().getId())
        ) {

            throw new ApiException(
                    "Employee does not belong to complaint institution",
                    HttpStatus.BAD_REQUEST
            );
        }

        if(complaint.getAssignedTo() != null && complaint.getAssignedTo().getId().equals(assignedEmployee.getId())
        ){
            throw new ApiException(
                    "Complaint is already assigned to this employee",
                    HttpStatus.BAD_REQUEST
            );
        }

        complaint.setAssignedTo(assignedEmployee);
        complaint.setDateTimeOfUpdate(LocalDateTime.now());
        complaintRepo.save(complaint);

        workflowEngine.createActionLog(
                complaint,
                manager.getAccount(),
                ActionType.REASSIGNED);

        notificationService.notifyUsers(complaint,"no thing",List.of(assignedEmployee.getAccount()));

        return new ApiResponseDto<>(
                true,
                "Complaint reassigned successfully",
                null
        );
    }

    public List<ManagerComplaintResponseDto> getUrgentComplaints() {

        Employee manager =
                employeeRepo.findByAccount_Email(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName()
                );

        return
                complaintRepo.findByPriorityAndStateAndGovernorateIdAndInstitutionId(
                        ComplaintPriority.CRITICAL,
                        ComplaintState.FORWARDED_TO_MANAGER,
                        manager.getGovernorate().getId(),
                        manager.getInstitution().getId()
                        )
                        .stream()
                        .map(complaintMapper::toManagerComplaintDto)
                        .toList();
    }
}