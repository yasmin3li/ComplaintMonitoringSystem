package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.ComplaintTracingLogRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.dto.ComplaintFilterRequestDto;
import com.myapp.complaints.dto.PerceptionComplaintResponseDto;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceptionistComplaintWorkflow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    
    public PerceptionComplaintResponseDto reviewComplaint(Complaint complaint) {

        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        if(complaint.getInstitution().getId().equals(employee.getInstitution().getId())){

            if(complaint.getState().equals(ComplaintState.NEW)){

                ComplaintTrackingLog trackingLog = CommonUtils.buildComplaintTrackingLog(complaint, employee.getAccount(),ComplaintState.IN_REVIEW,
                        null, ActionType.OPENED,"الشكوى قيد المراجعة");

                complaintTracingLogRepo.save(trackingLog);

                complaint.setState(ComplaintState.IN_REVIEW);
                complaintRepo.save(complaint);

                return complaintMapper.toPerceptionComplaintDto(complaint);

                //ensure same employee is handling complaint in review state
            } else if (complaint.getState().equals(ComplaintState.IN_REVIEW) &&
                    (complaintTracingLogRepo.findByComplaint_IdAndActionBy_IdAndNewState(complaint.getId(), employee.getAccount().getId(),ComplaintState.IN_REVIEW).isEmpty())
            ) {
                throw new ApiException("there are other employee working on this complaint", HttpStatus.FORBIDDEN);
            }
            else {
                return complaintMapper.toPerceptionComplaintDto(complaint);
            }

        }

        else throw new ApiException("this complaint doesn't belong to your institution",HttpStatus.FORBIDDEN);
    }

    public Specification<Complaint> getInstitutionComplaints(ComplaintFilterRequestDto filter) {

            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Employee employee = employeeRepo.findByAccount_Email(email);

            return  (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("deleted"), false));
                predicates.add(cb.equal(root.get("governorate").get("id"), employee.getGovernorate().getId()));
                predicates.add(cb.equal(root.get("institution").get("id"), employee.getInstitution().getId()));

                if (filter.state() == null) {
                    predicates.add(cb.equal(root.get("state"), ComplaintState.NEW));
                } else {
                    ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());
                    predicates.add(cb.equal(root.get("state"), complaintState));

                    if (complaintState == ComplaintState.IN_REVIEW) {

                        Join<Complaint, ComplaintTrackingLog> logJoin = root.join("logs");

                        predicates.add(cb.equal(
                                logJoin.get("actionBy").get("id"),
                                employee.getAccount().getId()
                        ));

                        predicates.add(cb.equal(
                                logJoin.get("newState"),
                                ComplaintState.IN_REVIEW
                        ));

//                        query.distinct(true);
                    }
                }

                query.orderBy(cb.desc(root.get("dateTimeOfAdd")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }

    }


