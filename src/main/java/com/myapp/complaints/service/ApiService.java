package com.myapp.complaints.service;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.enums.ImageType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.AccountInfoMapper;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.persistence.criteria.Predicate;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
    private final InstitutionSectorGovernorateRepo institutionSectorGovernorateRepo;
    private final SectorGovernorateRepo sectorGovernorateRepo;
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
    private final AuthorizationService authorizationService;
    private final VotingService votingService;
    private final NotificationService notificationService;

    @Transactional
    public ApiResponseDto<?> createComplaint(@Valid ComplaintCreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account citizenAccount = accountRepo.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));

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

//Add this Complaint to ComplaintTrackingLog
        ComplaintTrackingLog log = new ComplaintTrackingLog();
        log.setComplaint(complaint);
        log.setPreviousState(null);
        log.setNewState(ComplaintState.NEW);
        log.setActionType(ActionType.CREATED);
        log.setActionBy(citizenAccount);
        log.setComments("Citizen Added Complaint");
        complaint.getLogs().add(log);


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

        Complaint savedComplaint= complaintRepo.save(complaint);

//        notificationService.notifyUsers(complaint,"",List.of(account));
        //find all employee at institution: complaint-institution name, with role: perception employee, to send new notification with content: "new complaint has been added"
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
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));
        Citizen citizen=citizenRepo.findByAccountId(account.getId())
                .orElseThrow(()->new ApiException("no citizen found for account "+account.getEmail(),HttpStatus.NOT_FOUND));
        return accountInfoMapper.citizenInfoToDto(citizen);
    }

    public EmployeeProfileInfoDto getEmployeeInfoInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));
        Employee employee=employeeRepo.findByAccountId(account.getId())
                .orElseThrow(()->new ApiException("no employee found for account "+account.getEmail(),HttpStatus.NOT_FOUND));
        return accountInfoMapper.employeeInfoToDto(employee);
    }


//
    public Object getComplaints(ComplaintFilterRequestDto filter,boolean localUser) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String email = auth.getName();

        Specification<Complaint> spec;

        if (authorizationService.IsReceptionist()){

            Employee employee = employeeRepo.findByAccount_Email(email);

             spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.equal(root.get("deleted"), false));
                predicates.add(cb.equal(root.get("governorate").get("id"), employee.getGovernorate().getId()));
                predicates.add(cb.equal(root.get("institution").get("id"), employee.getInstitution().getId()));

                if (filter.state() == null){
                    predicates.add(cb.equal(root.get("state"), ComplaintState.NEW));
                }

                else {
                    ComplaintState complaintState = CommonUtils.fromArabicState(filter.state());
                    predicates.add(cb.equal(root.get("state"), complaintState));
                }

                query.orderBy(cb.desc(root.get("dateTimeOfAdd")));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
        }

        else{
             spec = (root, query, cb) -> {
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
            if (localUser){
//                if (Boolean.TRUE.equals(filter.myComplaints())) {
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


            return complaintRepo.findAll(
                            spec,
                            PageRequest.of(filter.page(), filter.size())
                    ).stream()
                    .map(complaintMapper::toDto)
                    .toList();
        }


    public List<ComplaintTrackingLogDto> getTimeLine(Long complaintId){

// TODO: dealing with deleted / not found for all case like this
        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

        if (authorizationService.checkAccess(complaint.getAddedBy().getEmail())){
            return  complaintTracingLogRepo.findByComplaintId(complaintId);
        }
        else{
            throw  new ApiException("You are not allowed to view this complaint", HttpStatus.FORBIDDEN);
        }
    }


// chose complaint from the ui to interact with it
    public Object getComplaint(Long complaintId){
        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));


        if (authorizationService.IsReceptionist())
            return complaintMapper.toPerceptionComplaintDto(complaint);
        else {
            return complaintMapper.toDto(complaint);
        }
    }

    @Transactional
    public ApiResponseDto<?> updateCitizenProfile(String email, UpdateCitizenProfileInfoDto dto ) {

        Citizen citizen = citizenRepo.findByAccount_Email(email).
                orElseThrow(() -> new ApiException("account not found",HttpStatus.NOT_FOUND));

         accountInfoMapper.updateAccountFromDto(dto,citizen);
         return new ApiResponseDto<>(
                 true,
                 "your info was updated successfully",
                 null
         );
    }

    public List<Governorate> governorates() {
        List<Governorate> governorates = governorateRepo.findAll();
        if(!governorates.isEmpty()){
            return governorates;
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<SectorGovernorate> sectorGovernorates(Long governorateId) {

        List<SectorGovernorate> sectors = sectorGovernorateRepo.findByGovernorate_Id(governorateId);
        if(!sectors.isEmpty()){
            return sectors;
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<InstitutionSectorGovernorate> institutionSectorGovernorates(Long sectorGovernorateId) {

        List<InstitutionSectorGovernorate> institutions = institutionSectorGovernorateRepo.findBySectorGovernorate_Id(sectorGovernorateId);
        if(!institutions.isEmpty()){
            return institutions;
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<ServiceAvailable> servicesAvailable(Long institutionId) {

        List<ServiceAvailable> services = serviceAvailableRepo.findByInstitutionId(institutionId);
        if(!services.isEmpty()){
            return services;
        }
        else{
            return new ArrayList<>();
        }
    }

    public List<ComplaintImageDto> complaintImages(Long complaintId) {

        return complaintImageRepo.findByComplaint_Id(complaintId);
    }
}
