package com.myapp.complaints.service;

import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.ComplaintRepo;
import com.myapp.complaints.DAO.RatingRepo;
import com.myapp.complaints.dto.ApiResponseDto;
import com.myapp.complaints.dto.RatingDto;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Complaint;
import com.myapp.complaints.entity.Rating;
import com.myapp.complaints.exceptionHandller.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final AccountRepo accountRepo;
    private final ComplaintRepo complaintRepo;
    private final RatingRepo ratingRepo;

    public ApiResponseDto<?> rating(Long complaintId, Integer starNumber) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUser = auth.getName();

        Account account = accountRepo.findByEmailAndDeletedFalse(currentUser).
                orElseThrow(()-> new ApiException("account not found", HttpStatus.NOT_FOUND));

        Complaint complaint = complaintRepo.findByIdAndDeletedFalse(complaintId)
                .orElseThrow(() -> new ApiException("Complaint with id "+complaintId+" not found",HttpStatus.NOT_FOUND));

        Optional<Rating> rating = ratingRepo.findByAccountIdAndComplaintId(account.getId(),complaintId);

        if(rating.isPresent()){

            Rating updateRating = rating.get();

            if(updateRating.getRatingValue().equals(starNumber)){

                ratingRepo.deleteById(updateRating.getId());

                return new ApiResponseDto<>(
                        true,
                        "rate deleted",
                        null
                );
            }

            updateRating.setRatingValue(starNumber);
            updateRating.setDateTimeOfRating(LocalDateTime.now());
            ratingRepo.save(updateRating);

            return new ApiResponseDto<>(
                    true,
                    "rate updated",
                    null
            );
        }

        Rating newRate = new Rating();
        newRate.setAccount(account);
        newRate.setComplaint(complaint);
        newRate.setDateTimeOfRating(LocalDateTime.now());
        newRate.setRatingValue(starNumber);
        ratingRepo.save(newRate);

        return new ApiResponseDto<>(
                true,
                "rating done successfully",
                null
        );

    }

    public RatingDto getRate(Long complaintId){

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAuthenticated =
                auth != null
                        && auth.isAuthenticated()
                        && !"anonymousUser".equals(auth.getName());

        if (!isAuthenticated) {
            return new RatingDto(
                    false,
                    0,
                    calculateRate(complaintId)
            );
        }

        Account account = accountRepo
                .findByEmailAndDeletedFalse(auth.getName())
                .orElse(null);

        Optional<Rating> rating =
                ratingRepo.findByAccountIdAndComplaintId(
                        account.getId(),
                        complaintId
                );

        return rating.map(value ->
                new RatingDto(
                        true,
                        rating.get().getRatingValue(),
                        calculateRate(complaintId)
                )
        ).orElseGet(() ->
                new RatingDto(
                        false,
                        0,
                        calculateRate(complaintId)
                )
        );
    }

    public Double calculateRate(Long complaintId){

        Optional<Complaint> complaint = complaintRepo.findByIdAndDeletedFalse(complaintId);
        if(complaint.isEmpty()){
            throw new ApiException("complaint not found",HttpStatus.NOT_FOUND);
        }
        else{

            double total = 0.0;

            List<Rating> ratings = ratingRepo.findByComplaintId(complaintId);

            if(ratings.isEmpty()){
                return 0.0;
            }
            else{

                for(Rating r : ratings){
                    total+=r.getRatingValue();
                }
                double average = total/ ratings.size();

                return Math.round(average * 10.0) / 10.0;
            }
        }
    }

}
