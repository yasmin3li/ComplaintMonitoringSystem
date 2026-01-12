package com.myapp.complaints.mapper;

import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.AddressDto;
import com.myapp.complaints.dto.ComplaintCreateDto;
import com.myapp.complaints.dto.ComplaintResponseDto;
import com.myapp.complaints.dto.LocationDto;
import com.myapp.complaints.entity.Address;
import com.myapp.complaints.entity.Complaint;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ComplaintMapper {
    private final AddressRepo addressRepo;
    private final ServiceAvailableRepo serviceAvailableRepo;
    private final InstitutionRepo institutionRepo;
    private final GovernorateRepo governorateRepo;
    private final SectorRepo sectorRepo;

    public ComplaintResponseDto toDto(Complaint complaint) {

        return new ComplaintResponseDto(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getDescription(),

                complaint.getService().getName(),
                complaint.getInstitution().getName(),

                complaint.getState().name(),
                complaint.getDateTimeOfAdd(),

                new LocationDto(
                        complaint.getGovernorate().getId(),
                        complaint.getGovernorate().getName(),

                        complaint.getSector().getId(),
                        complaint.getSector().getName(),

                        new AddressDto(
                                complaint.getAddress().getId(),
                                complaint.getAddress().getFullAddressText()
                        )
                )
        );
    }


    public Complaint fromdto(@Valid ComplaintCreateDto dto) {

        // create:  Address
        Address address = new Address();
        address.setLatitude(dto.latitude());
        address.setLongitude(dto.longitude());
        address.setFullAddressText(dto.fullAddressText());

        address = addressRepo.save(address);


        Complaint complaint = new Complaint();
        complaint.setTitle(dto.title());
        complaint.setDescription(dto.description());

//TODO: replace found with Is-Soft-delete? and add this field to db, or only edit the repo to return only isDeletedFalse #done
        complaint.setService(serviceAvailableRepo.findById(dto.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found")));

        complaint.setInstitution(institutionRepo.findById(dto.institutionId())
                .orElseThrow(() -> new RuntimeException("Institution not found")));

        complaint.setGovernorate(governorateRepo.findById(dto.governorateId())
                .orElseThrow(() -> new RuntimeException("Governorate not found")));

        complaint.setSector(sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new RuntimeException("Sector not found")));

        complaint.setAddress(address);

        return complaint;
    }
}
