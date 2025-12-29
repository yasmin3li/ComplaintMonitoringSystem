package com.myapp.complaints.service;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.ComplaintCreateDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Address;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.ComplaintState;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiService {

    private final AccountRepo accountRepo;
    private final ServiceAvailableRepo serviceAvailableRepo;
    private final GovernorateRepo governorateRepo;
    private final SectorRepo sectorRepo;
    private final AddressRepo addressRepo;
    private final InstitutionRepo institutionRepo;
    private final ComplaintRepo complaintRepo;

    public Complaint createComplaint(@Valid ComplaintCreateDto dto) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        Account account = accountRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Complaint complaint = new Complaint();
        complaint.setTitle(dto.title());
        complaint.setDescription(dto.description());
        complaint.setState(ComplaintState.NEW);
        complaint.setDeleted(false);
        complaint.setDateTimeOfAdd(LocalDateTime.now());

        complaint.setService(serviceAvailableRepo.findById(dto.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found")));

        complaint.setInstitution(institutionRepo.findById(dto.institutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found")));

        complaint.setGovernorate(governorateRepo.findById(dto.governorateId())
                .orElseThrow(() -> new RuntimeException("Governorate not found")));

        complaint.setSector(sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new RuntimeException("Sector not found")));

        complaint.setAddress(addressRepo.findById(dto.addressId())
                .orElseThrow(() -> new RuntimeException("Address not found")));

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

}
