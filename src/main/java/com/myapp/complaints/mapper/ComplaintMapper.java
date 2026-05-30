package com.myapp.complaints.mapper;

import com.myapp.complaints.CommonUtils;
import com.myapp.complaints.DAO.*;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Address;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.service.AuthorizationService;
import com.myapp.complaints.service.Formatter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

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
    private final ComplaintImageRepo complaintImageRepo;
    private final AuthorizationService authorizationService;
    private final Formatter formatter;
    private final ComplaintTracingLogRepo complaintTracingLogRepo;
    private final AccountRepo accountRepo;

    public ComplaintResponseDto toDto(Complaint complaint) {

        return new ComplaintResponseDto(
                complaint.getId(),
                complaint.getTitle(),
                complaint.getDescription(),

                complaint.getService().getName(),
                complaint.getInstitution().getName(),

                CommonUtils.toArabicState(complaint.getState()),
                complaint.getDateTimeOfAdd(),
                complaint.getDateTimeOfUpdate(),

                new LocationDto(
                        complaint.getGovernorate().getId(),
                        complaint.getGovernorate().getName(),

                        complaint.getSector().getId(),
                        complaint.getSector().getName(),

                        new AddressDto(
                                complaint.getAddress().getId(),
                                complaint.getAddress().getFullAddressText(),
                                complaint.getAddress().getLongitude(),
                                complaint.getAddress().getLatitude()
                        )
                ),
                complaint.getState() == ComplaintState.REJECTED || complaint.getState() == ComplaintState.RESOLVED
                        ? getLastRejectReason(complaint)
                        : null,
                authorizationService.checkAccess(complaint.getAddedBy().getEmail()),
                complaintImageRepo.findByComplaint_Id(complaint.getId())
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
            throw new ApiException("Sector does not belong to the selected governorate", HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new ApiException("Service not found",HttpStatus.NOT_FOUND));

        // Validate that the selected service belongs to the chosen institution.
        // This prevents assigning a service from a different institution.
        if (!service.getInstitution().getId().equals(dto.institutionId())) {
            throw new ApiException("Service does not belong to the selected institution",HttpStatus.BAD_REQUEST);
        }

        if (!institutionValid) {
            throw new ApiException("Institution not valid for this sector/governorate",HttpStatus.BAD_REQUEST);
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
                .orElseThrow(() -> new ApiException("Institution not found",HttpStatus.NOT_FOUND)));

        complaint.setGovernorate(governorateRepo.findById(dto.governorateId())
                .orElseThrow(() -> new ApiException("Governorate not found",HttpStatus.NOT_FOUND)));

        complaint.setSector(sectorRepo.findById(dto.sectorId())
                .orElseThrow(() -> new ApiException("Sector not found",HttpStatus.NOT_FOUND)));

        complaint.setAddress(address);

        return complaint;
    }

//TODO: replace email with identifier
    public ReceptionComplaintResponseDto toPerceptionComplaintDto(Complaint complaint) {

        String priority;
        if(complaint.getPriority()==null){
            priority = null;
        }else{
            priority = CommonUtils.toArabicSPriority(complaint.getPriority());
        }

        return new ReceptionComplaintResponseDto(
                toDto(complaint),
                complaint.getAddedBy().getUserName(),
                complaint.getAddedBy().getEmail(),
                formatter.complaintIdFormatter(complaint.getDateTimeOfAdd().getMinute()+complaint.getId()+complaint.getDateTimeOfAdd().getYear()+
                        complaint.getDateTimeOfAdd().getSecond()+complaint.getDateTimeOfAdd().getNano()),
                priority,
                complaint.getState() == ComplaintState.REJECTED
                        ? getLastRejectReason(complaint)
                        : null
        );
    }

    private String getLastRejectReason(Complaint complaint){

    Optional<Account> account = accountRepo.findByEmailAndDeletedFalse(SecurityContextHolder.getContext().getAuthentication().getName());
    Optional<ComplaintTrackingLog> log;
    if (authorizationService.IsReceptionist()) {

        if (account.isEmpty()) {
            throw new ApiException("no account found", HttpStatus.NOT_FOUND);
        }

        log =complaintTracingLogRepo.findTopByComplaint_IdAndActionBy_IdAndNewStateOrderByActionDateDesc(complaint.getId(), account.get().getId(), ComplaintState.REJECTED);
    }
    else{
        log =complaintTracingLogRepo.findTopByComplaint_IdAndNewStateOrderByActionDateDesc(complaint.getId(),complaint.getState());
    }

    return log.map(ComplaintTrackingLog::getComments).orElse(null);

}

    public ManagerComplaintResponseDto toManagerComplaintDto(Complaint complaint) {

        if(!(complaint.getAssignedTo() == null)){
            return new ManagerComplaintResponseDto(
                    toPerceptionComplaintDto(complaint),
                    complaint.getAssignedTo().getId(),
                    complaint.getAssignedTo().getAccount().getUserName(),
                    complaint.getAssignedTo().getAccount().getEmail()
            );
        }
        else{
            return new ManagerComplaintResponseDto(
                    toPerceptionComplaintDto(complaint),
                    null,
                    null,
                    null
            );
        }
    }


}
