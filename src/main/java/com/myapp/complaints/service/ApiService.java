package com.myapp.complaints.service;

import com.myapp.complaints.BadgeFactory;
import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.mapper.AccountInfoMapper;
import com.myapp.complaints.mapper.ComplaintMapper;
import com.myapp.complaints.mapper.ComplaintTrackingLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final TechnicComplaintWorkFlow technicComplaintWorkFlow;
    private final ReceptionistComplaintWorkflow receptionistComplaintWorkflow;
    private final ComplaintTrackingLogMapper trackingLogMapper;
    private final ManagerComplaintWorkFlow managerComplaintWorkFlow;
    private final StatisticsService statisticsService;
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
    public Object getComplaints(ComplaintFilterRequestDto filter) {

//TODO :later when dealing with admin_role or manager_role will do like this role_condition and local user will be always true

        if (authorizationService.IsReceptionist()) {

            return receptionistComplaintWorkflow.getInstitutionComplaints(filter);

        }

        else if (authorizationService.isManager()) {

            if(filter.myComplaints()!=null && filter.myComplaints().equals(true))
            {
                return technicComplaintWorkFlow.getInstitutionComplaints(filter);

            }
            return managerComplaintWorkFlow.getInstitutionComplaints(filter);

        }

        else if(authorizationService.isTechnic()) {
            return technicComplaintWorkFlow.getInstitutionComplaints(filter);
        }

        else {
            return citizenComplaintWorkFlow.getCitizensComplaints(filter);
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
    public Object openComplaint(Long complaintId){

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint not found",HttpStatus.NOT_FOUND));

// open complaint by receptionist employee
        if (authorizationService.IsReceptionist()){

            return receptionistComplaintWorkflow.openComplaint(complaint,complaint.getState());

        } else if (authorizationService.isManager()) {

            return managerComplaintWorkFlow.openComplaint(complaint.getId());

        }
    else if (authorizationService.isTechnic()) {

        return technicComplaintWorkFlow.openComplaint(complaint.getId());

    }
// open complaint by citizen
        else {
            //            TODO: feat(ApiService): add access control for rejected complaints based on ownership
//            Account account = accountRepo.findByEmailAndDeletedFalse(SecurityContextHolder.getContext().getAuthentication().getName())
//                    .orElseThrow(() -> new ApiException("User not found",HttpStatus.NOT_FOUND));
//
//             citizenRepo.findByAccountId(account.getId())
//                    .orElseThrow(() -> new ApiException("Citizen not found for account "+account.getEmail(),HttpStatus.NOT_FOUND));
//
//             if(!complaint.getAddedBy().getId().equals(account.getId()) && complaint.getState().equals(ComplaintState.REJECTED)){
//                 throw new ApiException("Access denied, you aren't the owner of this complaint",HttpStatus.FORBIDDEN);
//             }
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
//
//        //later when employee want to add complaint's images after solve, [only add images]
//        else if (authorizationService.IsReceptionist() || authorizationService.isManager()) {
//            return employeeComplaintWorkFlow.updateComplaint(email,dto);
//        }
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

    @Transactional
    public ApiResponseDto<?> addComplaintPriority(ComplaintPriorityDto dto) {

        Employee employee = employeeRepo.findByAccount_Email
                (SecurityContextHolder.getContext().getAuthentication().getName());

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(dto.complaintId())
                .orElseThrow(() -> new ApiException("Complaint not found", HttpStatus.NOT_FOUND));


        if(complaint.getState().equals(ComplaintState.REJECTED))
        {
            throw new ApiException("not allowed method [add priority] at this state",HttpStatus.BAD_REQUEST);
        }
        else if (complaint.getState().equals(ComplaintState.NEW)) {
            complaint.setPriority(CommonUtils.fromArabicPriority(dto.priority()));
            complaintRepo.save(complaint);
            return new ApiResponseDto<>(true,"priority "+complaint.getPriority()+" was added successfully",null);
        }
        else {
            if(!authorizationService.checkResponsibility(employee,complaint)){
                throw new ApiException("Access denied, you aren't the responsible of this complaint",HttpStatus.FORBIDDEN);
            }
        }

        complaint.setPriority(CommonUtils.fromArabicPriority(dto.priority()));
        complaintRepo.save(complaint);

        return new ApiResponseDto<>(true,"priority "+complaint.getPriority()+" was added successfully",null);
    }

    public List<Employee> getInstitutionEmployee(){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        Employee employee = employeeRepo.findByAccount_Email(auth.getName());

        return
                employeeRepo.findByGovernorate_IdAndInstitution_IdAndAccount_Role_Id(
                        employee.getGovernorate().getId(),
                        employee.getInstitution().getId(),
                        4);

    }

    //جلب الشكاوى المتاخرة في مؤسسة المدير الحالي
    public List<DelayedComplaintDto> getDelayedComplaints(boolean myComplaints) {

        Employee currentEmployee =
                employeeRepo.findByAccount_Email(SecurityContextHolder.getContext().getAuthentication().getName());

        if(!myComplaints && authorizationService.isManager()){

            List<Employee> employees =
                    employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                            currentEmployee.getInstitution().getId(),
                            currentEmployee.getGovernorate().getId()
                            ,
                            4L
                    );

            employees.addAll(employeeRepo.findByInstitution_IdAndGovernorate_IdAndAccount_Role_Id(
                    currentEmployee.getInstitution().getId(),
                    currentEmployee.getGovernorate().getId(),
                    3L
            ));

            List<List<DelayedComplaintDto>> allDelayedComplaints = new ArrayList<>();

            for (Employee employee : employees) {

                allDelayedComplaints.add(statisticsService.getDelayedComplaints(employee));

            }

            return allDelayedComplaints.stream()
                    .flatMap(List::stream)
                    .toList();

        }
        else return statisticsService.getDelayedComplaints(currentEmployee);

    }
}
