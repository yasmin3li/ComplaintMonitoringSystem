package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.dto.ComplaintFilterRequestDto;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.enums.ComplaintState;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CitizenComplaintWorkFlow {

    public Specification<Complaint> getCitizensComplaints(boolean localUser, ComplaintFilterRequestDto filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // deleted = false
            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.governorateId() != null) {
                predicates.add(cb.equal(root.get("governorate").get("id"), filter.governorateId()));
            }

            if (filter.sectorId() != null) {
                predicates.add(cb.equal(root.get("sector").get("id"), filter.sectorId()));
            }

            if (filter.institutionId() != null) {

                predicates.add(cb.equal(root.get("institution").get("id"), filter.institutionId()));
            }

            if (filter.state() != null && !filter.state().isBlank()) {

                ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());

                predicates.add(cb.equal(root.get("state"), complaintState));
            }

            // citizen only
            if (localUser) {
                String email = SecurityContextHolder.getContext().getAuthentication().getName();
                predicates.add(cb.equal(root.get("addedBy").get("email"), email));
            }


            // Keyword search
            if (filter.keyword() != null && !filter.keyword().isEmpty()) {
                String pattern = "%" + filter.keyword().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern)
                ));
            }

            query.orderBy(cb.desc(root.get("dateTimeOfAdd")));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
//TODO: refactor, translate add complaint from ApiService to here