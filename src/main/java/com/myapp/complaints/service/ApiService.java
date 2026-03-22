package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.mapper.AccountInfoMapper;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final EmployeeRepo employeeRepo;

    private final AccountRepo accountRepo;
    private final ServiceAvailableRepo serviceAvailableRepo;
    private final GovernorateRepo governorateRepo;
    private final SectorRepo sectorRepo;
    private final AddressRepo addressRepo;
    private final InstitutionRepo institutionRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final CitizenRepo citizenRepo;
    private final ComplaintImageRepo complaintImageRepo;

    @Transactional
    public ApiResponseDto<Object> createComplaint(@Valid ComplaintCreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = complaintMapper.fromdto(dto);

/**moved to mapper
 // create:  Address
 Address address = new Address();
 address.setLatitude(dto.latitude());
 address.setLongitude(dto.longitude());
 address.setFullAddressText(dto.fullAddressText());

 address = addressRepo.save(address);


 Complaint complaint = new Complaint();
 complaint.setTitle(dto.title());
 complaint.setDescription(dto.description());
 **/

        complaint.setState(ComplaintState.NEW);
        complaint.setDeleted(false);
        complaint.setDateTimeOfAdd(LocalDateTime.now());

/** moved to mapper
 //        complaint.setService(serviceAvailableRepo.findById(dto.serviceId())
 //                .orElseThrow(() -> new RuntimeException("Service not found")));
 //
 //        complaint.setInstitution(institutionRepo.findById(dto.institutionId())
 //                .orElseThrow(() -> new RuntimeException("Institution not found")));
 //
 //        complaint.setGovernorate(governorateRepo.findById(dto.governorateId())
 //                .orElseThrow(() -> new RuntimeException("Governorate not found")));
 //
 //        complaint.setSector(sectorRepo.findById(dto.sectorId())
 //                .orElseThrow(() -> new RuntimeException("Sector not found")));

 //       complaint.setAddress(address);
 */

        complaint.setAddedBy(account);

        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(null);
        log.setNewState(ComplaintState.NEW);
        log.setActionType(ActionType.CREATED);
        log.setActionBy(null);
        log.setComments("Citizen Added Complaint");
        complaint.getLogs().add(log);

    Complaint savedComplaint= complaintRepo.save(complaint);
        complaintTracingLogRepo.save(log);

//TODO    dealing with images
        if(dto.images() != null) {

            for(String url : dto.images()) {

                ComplaintImage img = new ComplaintImage();

                img.setComplaint(savedComplaint);
                img.setImageUrl(url);
                img.setAddedBy(account);
                img.setType(ImageType.BEFORE_SOLVE);

                //حتى تبقى البيانات متزامنة بحال طلبت الصور في نفس المناقلة
                savedComplaint.getImages().add(img);
                complaintImageRepo.save(img);
            }
        }

//        return savedComplaint;
        return new ApiResponseDto<Object>(
                true,
                "your complaint < "+savedComplaint.getTitle()+" > was added successfully",
                null
        );
    }


//Get data for homePage (last complaints)
//    public List<ComplaintResponseDto> getLast10Complaints() {
//
//        return complaintRepo
//                .findByDeletedFalseOrderByDateTimeOfAddDesc(PageRequest.of(0,10))
//                .stream()
//                .map(complaintMapper::toDto)
//                .toList();
//    }

// Get profile info
    private final AccountInfoMapper accountInfoMapper;
    public CitizenProfileInfoDto getCitizenInfo() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Citizen citizen=citizenRepo.findByAccountId(account.getId())
                .orElseThrow(()->new RuntimeException("no citizen found for account "+account.getEmail()));
        return accountInfoMapper.citizenInfoToDto(citizen);
    }

    public EmployeeProfileInfoDto getEmployeeInfoInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Employee employee=employeeRepo.findByAccountId(account.getId())
                .orElseThrow(()->new RuntimeException("no employee found for account "+account.getEmail()));
        return accountInfoMapper.employeeInfoToDto(employee);
    }


//
    public List<ComplaintResponseDto> getComplaints(ComplaintFilterRequestDto filter) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Specification<Complaint> spec = (root, query, cb) -> {
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

            if (filter.state() != null) {
                predicates.add(cb.equal(root.get("state"), filter.state()));
            }

            // citizen only
            if (Boolean.TRUE.equals(filter.myComplaints())) {
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

        return complaintRepo.findAll(
                        spec,
                        PageRequest.of(filter.page(), filter.size())
                ).stream()
                .map(complaintMapper::toDto)
                .toList();
    }

}
