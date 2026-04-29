package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.AccountInfoMapper;
import com.myapp.complaints.mapper.ComplaintMapper;
import com.myapp.complaints.mapper.ComplaintTrackingLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final ComplaintRepo complaintRepo;
    private final ComplaintMapper complaintMapper;
    private final CitizenRepo citizenRepo;
    private final ComplaintImageRepo complaintImageRepo;
    private final AuthorizationService authorizationService;
    private final CitizenComplaintWorkFlow citizenComplaintWorkFlow;
    private final NotificationService notificationService;
    private final ReceptionistComplaintWorkflow receptionistComplaintWorkflow;
    private final ComplaintTrackingLogMapper trackingLogMapper;

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

        Account account = accountRepo.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));
        Citizen citizen=citizenRepo.findByAccountId(account.getId())
                .orElseThrow(()->new ApiException("no citizen found for account "+account.getEmail(),HttpStatus.NOT_FOUND));
        return accountInfoMapper.citizenInfoToDto(citizen);
    }

    public EmployeeProfileInfoDto getEmployeeInfoInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));
        Employee employee=employeeRepo.findByAccountId(account.getId())
                .orElseThrow(()->new ApiException("no employee found for account "+account.getEmail(),HttpStatus.NOT_FOUND));
        return accountInfoMapper.employeeInfoToDto(employee);
    }


//display list of complaints based on account's role and special filter
    public Object getComplaints(ComplaintFilterRequestDto filter,boolean localUser) {

        Specification<Complaint> spec;
//TODO :later when dealing with admin_role or manager_role will do like this role_condition and local user will be always true

        if (authorizationService.IsReceptionist()) {

            spec = receptionistComplaintWorkflow.getInstitutionComplaints(filter);

            if (filter.page() == null || filter.size() == null) {

                return complaintRepo.findAll(
                                spec,
                                PageRequest.of(0, 100)).stream()
                        .map(complaintMapper::toPerceptionComplaintDto)
                        .toList();

            } else {
                return complaintRepo.findAll(
                                spec,
                                PageRequest.of(filter.page(), filter.size())
                        ).stream()
                        .map(complaintMapper::toPerceptionComplaintDto)
                        .toList();
            }
        }

        else {

            spec = citizenComplaintWorkFlow.getCitizensComplaints(localUser,filter);
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

    public List<ComplaintTrackingLogDto> getTimeLine(Long complaintId){

// TODO: dealing with deleted / not found for all case like this
        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

        if (authorizationService.checkAccess(complaint.getAddedBy().getEmail())){
            return  complaintTracingLogRepo.findByComplaintId(complaintId)
                    .stream()
                    .map(trackingLogMapper::dto)
                    .toList();
        }
        else{
            throw  new ApiException("You are not allowed to view this complaint", HttpStatus.FORBIDDEN);
        }
    }


// chose complaint from the ui to interact with it
    @Transactional
    public Object getComplaint(Long complaintId){

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

// open complaint by receptionist employee
        if (authorizationService.IsReceptionist()){

            return receptionistComplaintWorkflow.openComplaint(complaint,complaint.getState());

        }

// open complaint by citizen
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

    @Transactional
    public ApiResponseDto<?> updateEmployeeProfile(String email, UpdateEmployeeProfileInfoDto dto ) {

        Employee employee = employeeRepo.findByAccount_Email(email);

        accountInfoMapper.updateAccountFromDto(dto,employee);
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

    public ApiResponseDto<?> updateComplaint(String email, UpdateComplaintDto dto){

        if(authorizationService.isCitizen()){
            return citizenComplaintWorkFlow.updateComplaint(email,dto);
        }

        //later when employee want to add complaint's images after solve, [only add images]
        else if (authorizationService.IsReceptionist() || authorizationService.isManager()) {
            return receptionistComplaintWorkflow.updateComplaint(email,dto);
        }
        else {
            throw new ApiException("Unsupported role for this operation yet", HttpStatus.FORBIDDEN);
        }
    }

    public ApiResponseDto<?> deleteComplaint(String email, Long complaintId) {

            return citizenComplaintWorkFlow.deleteComplaint(email,complaintId);
    }

    @Transactional
    public ApiResponseDto<?> deleteAccount(String email) {

        Account account = accountRepo.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));

            account.setDeleted(true);
            accountRepo.save(account);

        return new ApiResponseDto<>(
                true,
                String.format("تم حذف الحساب للمستخدم : \"%s\" بنجاح",account.getUserName()),
                null
        );
    }

    public ApiResponseDto<?> rejectComplaint(String email, ComplaintRejectDto dto) {

// open complaint by receptionist employee
        if (authorizationService.IsReceptionist()){
            return receptionistComplaintWorkflow.rejectComplaint(email,dto);
        }

//TODO: later open complaint by manager / employee ...
        else {
            throw new ApiException("Unsupported role for this operation yet", HttpStatus.FORBIDDEN);
        }
    }
}
