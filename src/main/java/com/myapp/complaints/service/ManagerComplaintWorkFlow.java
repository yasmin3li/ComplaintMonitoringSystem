package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.dto.ComplaintFilterRequestDto;
import com.myapp.complaints.dto.ComplaintResponseDto;
import com.myapp.complaints.dto.ReceptionComplaintResponseDto;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ManagerComplaintWorkFlow {

    private final EmployeeRepo employeeRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;

    public List<ComplaintResponseDto> getInstitutionComplaints(ComplaintFilterRequestDto filter) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Employee employee = employeeRepo.findByAccount_Email(email);

        Specification<Complaint> spec =  (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("deleted"), false));
            predicates.add(cb.equal(root.get("governorate").get("id"), employee.getGovernorate().getId()));
            predicates.add(cb.equal(root.get("institution").get("id"), employee.getInstitution().getId()));

            if (filter.state() != null) {

                ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());
                predicates.add(cb.equal(root.get("state"), complaintState));

                    if (!(complaintState == ComplaintState.IN_REVIEW) && !(complaintState == ComplaintState.NEW)) {

                        Join<Complaint, ComplaintTrackingLog> logJoin = root.join("logs");

                        predicates.add(cb.equal(
                                logJoin.get("actionBy").get("id"),
                                employee.getAccount().getId()
                        ));

                        predicates.add(cb.equal(
                                logJoin.get("newState"),
                                complaintState
                        ));
                    }
                }

            else {
                    predicates.add(cb.equal(root.get("state"), ComplaintState.FORWARDED_TO_MANAGER));
            }

            query.orderBy(cb.desc(root.get("dateTimeOfAdd")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        if (filter.page() == null || filter.size() == null) {

            return complaintRepo.findAll(
                            spec,
                            PageRequest.of(0, 100)).stream()
                    .map(complaintMapper::toDto)
                    .toList();

        } else {
            return complaintRepo.findAll(
                            spec,
                            PageRequest.of(filter.page(), filter.size())
                    ).stream()
                    .map(complaintMapper::toDto)
                    .toList();
        }
    }


}
