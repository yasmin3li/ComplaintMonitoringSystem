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

import java.time.Duration;
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
    private final EmployeePerformanceService performanceService;
    private final ComplaintTracingLogRepo tracingLogRepo;

    public List<ReceptionComplaintResponseDto> getInstitutionComplaints(
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
                .map(complaintMapper::toPerceptionComplaintDto)
                .toList();
    }


    @Transactional
    public ComplaintResponseDto openComplaint(long complaintId) {

        Employee employee =
                employeeRepo.findByAccount_Email(  SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(complaintId).orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        if (!authorizationService.checkAccessibility(employee,complaint)) {
            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        workflowEngine.createActionLog(complaint, employee.getAccount(), ActionType.OPENED);

        return complaintMapper
                .toDto(complaint);

    }


    @Transactional
    public ApiResponseDto<?> assignComplaintToEmployee(
            AssignComplaintDto dto
    ) {

        Employee manager =
                employeeRepo.findByAccount_Email(
                        SecurityContextHolder
                                .getContext()
                                .getAuthentication()
                                .getName()
                );

        Complaint complaint =
                complaintRepo
                        .findByIdAndDeletedFalse(
                                dto.complaintId()
                        )
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
                .checkAccessibility(
                        manager,
                        complaint
                )) {

            throw new ApiException(
                    "Access denied",
                    HttpStatus.FORBIDDEN
            );
        }

        Employee assignedEmployee =
                employeeRepo.findById(
                                dto.assignedTo()
                        )
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
                "تم اسناد الشكوى بنجاح",
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
        long threshold;

        if (dto.start() == null || dto.end() == null) {

            end = LocalDateTime.now();
            start = end.minusMonths(1);

        }else {
            start = dto.start();
            end = dto.end();
        }

        if(dto.threshold()<=0){
            threshold = 3;
        }else {
            threshold = dto.threshold();
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

            List<DelayedComplaintDto> delayedComplaintDtoList =
                    complaintRepo.delayedComplaints(
                                    employee.getAccount().getId(),
                                    LocalDateTime.now().minusDays(threshold)
                            )
                            .stream()
                            .map(dc -> {

                                double delayedDays =
                                        Math.round(
                                                Duration.between(
                                                        dc.getLastUpdate(),
                                                        LocalDateTime.now()).
                                                        toHours() / 24.0 * 100) / 100.0;

                                return new DelayedComplaintDto(
                                        dc.getComplaintId(),
                                        dc.getTitle(),
                                        dc.getPriority(),
                                        dc.getLastUpdate(),
                                        dc.getState(),
                                        delayedDays
                                );
                            })
                            .toList();
//
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
                            : ((double)(inProgressTasks + assignedTasks) * 100)
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
                Comparator.comparingDouble(
                                (ManagerEmployeeRecommendationDto e) ->
                                        e.employeeBadgeDto().loadRatio()
                        )
//                        .thenComparing(
//                                Comparator.comparingDouble(
//                                        ManagerEmployeeRecommendationDto::resolvedComplaints
//                                ).reversed().reversed()
//                        )
        );

        if (!result.isEmpty()) {

            ManagerEmployeeRecommendationDto leastLoaded =
                    result.stream()
                            .min(
                                    Comparator
                                            .comparingDouble(
                                                    (ManagerEmployeeRecommendationDto e) ->
                                                            e.employeeBadgeDto().loadRatio()
                                            )
                                            .thenComparing(
                                                    Comparator.comparingLong(
                                                            ManagerEmployeeRecommendationDto::resolvedComplaints
                                                    ).reversed().reversed()
                                            )

                            )
                            .orElse(null);

            if (leastLoaded != null) {

                int index = result.indexOf(leastLoaded);

                ManagerEmployeeRecommendationDto updatedEmployee = getManagerEmployeeRecommendationDto(leastLoaded);

                result.set(index, updatedEmployee);
            }
        }

        return result;
    }

    @Nonnull
    private static ManagerEmployeeRecommendationDto getManagerEmployeeRecommendationDto(ManagerEmployeeRecommendationDto leastLoaded) {
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

        return new ManagerEmployeeRecommendationDto(
                leastLoaded.employeeId(),
                leastLoaded.employeeName(),
                leastLoaded.assignedTasks(),
                leastLoaded.inProgressTasks(),
                leastLoaded.resolvedComplaints(),
                leastLoaded.delayedComplaints(),
                updatedBadge
        );
    }

}