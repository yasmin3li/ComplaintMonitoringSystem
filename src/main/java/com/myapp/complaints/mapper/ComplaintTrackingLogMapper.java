package com.myapp.complaints.mapper;

import com.myapp.complaints.dto.AssignedToDto;
import com.myapp.complaints.dto.ComplaintTrackingLogDto;
import com.myapp.complaints.entity.ComplaintTrackingLog;
import org.springframework.stereotype.Component;

@Component
public class ComplaintTrackingLogMapper {

    public ComplaintTrackingLogDto dto(ComplaintTrackingLog log) {

        if(log.getAssignedTo() != null){

        return new ComplaintTrackingLogDto(log.getNewState(),
                log.getActionType(),
                log.getComments(),
                log.getActionDate(),

                new AssignedToDto(log.getAssignedTo().getAccount().getUserName(),
                        log.getAssignedTo().getInstitution().getName(),
                        log.getAssignedTo().getGovernorate().getName(),
                        log.getAssignedTo().getSector().getName())
        );
    }
        else {
            return new ComplaintTrackingLogDto(
                    log.getNewState(),
                    log.getActionType(),
                    log.getComments(),
                    log.getActionDate(),
        null);
        }
    }

}

