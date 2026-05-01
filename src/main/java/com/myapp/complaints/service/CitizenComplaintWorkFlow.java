package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.ComplaintMapper;
import com.myapp.complaints.complaintStateHandler.ComplaintWorkflowEngine;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CitizenComplaintWorkFlow {

    private final EmployeeRepo employeeRepo;
    private final AccountRepo accountRepo;
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final NotificationService notificationService;
    private final ComplaintWorkflowEngine workflowEngine;
    private final AuthorizationService authorizationService;
    private final ServiceAvailableRepo serviceAvailableRepo ;
    private final GovernorateRepo governorateRepo;
    private  final SectorRepo sectorRepo;
    private final InstitutionRepo institutionRepo;


    @Transactional
    public ApiResponseDto<?> createComplaint(@Valid ComplaintCreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account citizenAccount = accountRepo.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException("User not found", HttpStatus.NOT_FOUND));

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

        complaint.setAddedBy(citizenAccount);

//TODO    dealing with images
        if(dto.images() != null) {

            for(String url : dto.images()) {

                ComplaintImage img = new ComplaintImage();

                img.setComplaint(complaint);
                img.setImageUrl(url);
                img.setAddedBy(citizenAccount);
                img.setType(ImageType.BEFORE_SOLVE);

                //حتى تبقى البيانات متزامنة بحال طلبت الصور في نفس المناقلة
                complaint.getImages().add(img);
//                complaintImageRepo.save(img);
            }
        }

        complaint.setPriority(null);
        Complaint savedComplaint= complaintRepo.save(complaint);

/**
 * Add this event to ComplaintTrackingLogDto
 */
        workflowEngine.createInitialLog(savedComplaint, citizenAccount);

/**
 *  find all employee at institution: complaint-institution name,
 * with role: perception employee, to send new notification with content: "new complaint has been added"
 */
        List<Employee> employees = employeeRepo.findByInstitution_IdAndAccount_Role_Id(complaint.getInstitution().getId(),2);

        List<Account> accounts = new ArrayList<>(List.of());

        for (Employee employee : employees) {
            accounts.add(employee.getAccount());
        }

//        List<Account> accounts = accountRepo.findByRoleId(2L);
        accounts.add(citizenAccount);
        notificationService.notifyUsers(complaint,"no reason to add with state NEW",accounts);

        return new ApiResponseDto<>(
                true,
                String.format("تم حفظ شكواك: \"%s\" بنجاح",savedComplaint.getTitle()),
                null
        );
    }

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

    public ApiResponseDto<?> updateComplaint(String email, UpdateComplaintDto dto) {

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

        Account account = accountRepo.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException("Account not found",HttpStatus.NOT_FOUND));


        if (!authorizationService.checkAccess(complaint.getAddedBy().getEmail())) {
            throw new ApiException("Access denied, you aren't the owner for this complaint",HttpStatus.FORBIDDEN);
        }

        ComplaintState complaintState = complaint.getState();

        if (!(complaintState.equals(ComplaintState.NEW) ||
                complaintState.equals(ComplaintState.REJECTED))) {

            throw new RuntimeException("Cannot update complaint in this state");
        }

        if (dto.title() != null) {
            complaint.setTitle(dto.title());
        }

        if (dto.description() != null) {
            complaint.setDescription(dto.description());
        }

        if (dto.latitude() != null && dto.longitude() != null) {
            Address address = complaint.getAddress();

            if (address == null) {
                address = new Address();
            }

            address.setLatitude(dto.latitude());
            address.setLongitude(dto.longitude());
            address.setFullAddressText(dto.fullAddressText());
            complaint.setAddress(address);
        }

        if (dto.serviceId() != null) {
            ServiceAvailable service = serviceAvailableRepo.findById(dto.serviceId())
                    .orElseThrow(() -> new RuntimeException("Service not found"));
            complaint.setService(service);
        }

        if (dto.governorateId() != null) {
            Governorate governorate = governorateRepo.findById(dto.governorateId())
                    .orElseThrow(() -> new RuntimeException("Governorate not found"));
            complaint.setGovernorate(governorate);
        }

        if (dto.sectorId() != null) {
            Sector sector = sectorRepo.findById(dto.sectorId())
                    .orElseThrow(() -> new RuntimeException("Sector not found"));
            complaint.setSector(sector);
        }

        if (dto.institutionId() != null) {
            Institution institution = institutionRepo.findById(dto.institutionId())
                    .orElseThrow(() -> new RuntimeException("institution not found"));
            complaint.setInstitution(institution);
        }

//        if (dto.fullAddressText() != null) {
//            co;
//        }

        if (dto.images() != null) {

        complaint.getImages().clear();

            for (String img : dto.images()) {

                ComplaintImage complaintImage = new ComplaintImage();
                complaintImage.setImageUrl(img);
                complaintImage.setType(ImageType.BEFORE_SOLVE);
                complaintImage.setComplaint(complaint);
                complaintImage.setAddedBy(account);

                complaint.getImages().add(complaintImage);
            }
        }
        else{
            if(!complaint.getImages().isEmpty())
                complaint.getImages().clear();
        }

        if(complaintState.equals(ComplaintState.NEW)){
            workflowEngine.createActionLog(complaint,account,ActionType.UPDATED);
        }
        else {
            workflowEngine.changeState(complaint, ComplaintState.NEW, account, null, "تم تحديث الشكوى", ActionType.UPDATED);
        }
        complaintRepo.save(complaint);

        return new ApiResponseDto<>(
                true,
                "complaint updated successfully",
                null
        );
    }

    @Transactional
    public ApiResponseDto<?> deleteComplaint(String email, Long complaintId) {

        Complaint complaint = complaintRepo
                .findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));

        Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(email);

        if(account.isPresent()){
            if (authorizationService.isAdmin()) {

                complaint.setDeleted(true);
                complaintRepo.save(complaint);

                workflowEngine.createActionLog(complaint,account.get(), ActionType.DELETED);

                return new ApiResponseDto<>(
                        true,
                        String.format("تم حذف الشكوى: \"%s\" بنجاح", complaint.getTitle()),
                        null
                );
            }

            else {

                if (authorizationService.checkAccess(complaint.getAddedBy().getEmail())) {

                    if (complaint.getState().equals(ComplaintState.NEW) || complaint.getState().equals(ComplaintState.REJECTED)) {
                        complaint.setDeleted(true);
                        complaintRepo.save(complaint);

                        workflowEngine.createActionLog(complaint,account.get(), ActionType.DELETED);

                        return new ApiResponseDto<>(
                                true,
                                String.format("تم حذف شكواك: \"%s\" بنجاح", complaint.getTitle()),
                                null
                        );
                    } else {
                        throw new ApiException(
                                "you can't delete this complaint at this state",
                                HttpStatus.BAD_REQUEST
                        );
                    }
                } else {
                    throw new ApiException(
                            "you can't delete this complaint because you are not the owner",
                            HttpStatus.FORBIDDEN
                    );
                }
            }

        }
        else
            throw new ApiException("account not found",HttpStatus.NOT_FOUND);
    }
}