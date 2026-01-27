package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.CitizenProfileInfoDto;
import com.myapp.complaints.dto.ComplaintCreateDto;
import com.myapp.complaints.dto.ComplaintResponseDto;
import com.myapp.complaints.dto.EmployeeProfileInfoDto;
import com.myapp.complaints.entity.*;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.mapper.AccountInfoMapper;
import com.myapp.complaints.mapper.ComplaintMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {
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


    public Complaint createComplaint(@Valid ComplaintCreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint=complaintMapper.fromdto(dto);

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
        complaint.getLogs().add(log);

        return complaintRepo.save(complaint);
    }



    public List<ComplaintResponseDto> getLast10Complaints() {

        return complaintRepo
                .findTop10ByDeletedFalseOrderByDateTimeOfAddDesc()
                .stream()
                .map(complaintMapper::toDto)
                .toList();
    }


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
}
