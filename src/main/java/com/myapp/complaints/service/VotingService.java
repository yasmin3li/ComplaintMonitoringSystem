package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.VotingRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.VotingDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Voting;
import com.myapp.complaints.enums.VotingType;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
@Transactional
public class VotingService {
    private final AuthorizationService authorizationService;
    private final VotingRepo votingRepo;
    private final ComplaintRepo complaintRepo;
    private final AccountRepo accountRepo;

    public VotingDto getVotes(Long complaintId) {
        Long likesNumber = likesCount(complaintId);
        Long disLikesNumber = disLikesCount(complaintId);
        return new VotingDto(likesNumber,
                disLikesNumber
        );
    }

    public Long likesCount(Long complaintId) {
        return votingRepo.countByComplaintIdAndType(complaintId,VotingType.LIKE);
    }

    public Long disLikesCount(Long complaintId) {
        return votingRepo.countByComplaintIdAndType(complaintId, VotingType.DISLIKE);
    }

    public ApiResponseDto<?> Voting(Long complaintId, VotingType votingType) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        Account account = accountRepo.findByEmail(currentUser).
                orElseThrow(()-> new ApiException("account not found", HttpStatus.NOT_FOUND));

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint with id "+complaintId+" not found",HttpStatus.NOT_FOUND));

        String addedBy = complaint.getAddedBy().getEmail();

        if(authorizationService.checkAccess(addedBy)){
            throw new ApiException("current user can't vote because is the owner for this complaint",HttpStatus.FORBIDDEN );
        }

        Optional<Voting> voting = votingRepo.findByAccountIdAndComplaintId(account.getId(),complaintId);

        if(voting.isPresent()){

            Voting updateVoting = voting.get();

            if(updateVoting.getType().equals(votingType)){

                votingRepo.deleteById(updateVoting.getId());

                return new ApiResponseDto<>(
                    true,
                    "vote deleted",
                    null
            );
            }

            updateVoting.setType(votingType);
            updateVoting.setDateTimeOfVoting(LocalDateTime.now());
            votingRepo.save(updateVoting);

            return new ApiResponseDto<>(
                    true,
                    "vote updated",
                    null
            );
        }

        Voting newVote = new Voting();
        newVote.setAccount(account);
        newVote.setComplaint(complaint);
        newVote.setDateTimeOfVoting(LocalDateTime.now());
        newVote.setType(votingType);
        votingRepo.save(newVote);

        return new ApiResponseDto<>(
                true,
                "voting done successfully",
                null
        );

    }

}
