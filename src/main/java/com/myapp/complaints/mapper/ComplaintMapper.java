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
    private final InstitutionSectorGovernorateRepo institutionSectorGovernorateRepo;
    private final SectorGovernorateRepo sectorGovernorateRepo;

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

    // ------------------------------------------------------------------
    // VALIDATION SECTION
    // ------------------------------------------------------------------
    // The frontend sends governorateId, sectorId, institutionId and serviceId.
    // Although the UI restricts the selection order, the backend must still
    // validate the relationships to prevent inconsistent data (e.g. via Postman).
    // These validations guarantee that the selected location hierarchy is valid.
    // ------------------------------------------------------------------

    // Validate that the selected sector actually exists in the selected governorate.
    // This prevents creating complaints with mismatched sector/governorate pairs.
        boolean sectorExists =
                sectorGovernorateRepo.existsBySectorIdAndGovernorateId(
                        dto.sectorId(),
                        dto.governorateId()
                );

        if (!sectorExists) {
            throw new RuntimeException("Sector does not belong to the selected governorate");
        }

        // Validate that the selected institution is active in the given sector/governorate.
        // This ensures that the institution truly operates within that location.
        boolean institutionValid =
                institutionSectorGovernorateRepo
                        .existsByInstitutionIdAndSectorGovernorateSectorIdAndSectorGovernorateGovernorateId(
                                dto.institutionId(),
                                dto.sectorId(),
                                dto.governorateId()
                        );

        var service = serviceAvailableRepo.findById(dto.serviceId())
                .orElseThrow(() -> new RuntimeException("Service not found"));

        // Validate that the selected service belongs to the chosen institution.
        // This prevents assigning a service from a different institution.
        if (!service.getInstitution().getId().equals(dto.institutionId())) {
            throw new RuntimeException("Service does not belong to the selected institution");
        }

        if (!institutionValid) {
            throw new RuntimeException("Institution not valid for this sector/governorate");
        }


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
        complaint.setService(service);

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
