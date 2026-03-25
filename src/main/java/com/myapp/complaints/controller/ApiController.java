package com.myapp.complaints.controller;


import com.myapp.complaints.dto.ComplaintCreateDto;
import com.myapp.complaints.dto.ComplaintFilterRequestDto;
import com.myapp.complaints.dto.ComplaintResponseDto;
import com.myapp.complaints.enums.VotingType;
import com.myapp.complaints.service.ApiService;
import com.myapp.complaints.service.StatisticsService;
import com.myapp.complaints.service.VotingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {


    private  final ApiService apiService;
    private final StatisticsService statisticsService;
    private final VotingService votingService;

// content-type: multipart/form-data not Json, before data+images
    @PostMapping("/complaint")
    public ResponseEntity<?> createComplaint(
            @Valid @RequestBody ComplaintCreateDto dto) {

        return ResponseEntity.ok(
                apiService.createComplaint(dto)
        );
    }
//    @PostMapping(value="/complaint", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> createComplaint(
//
//            @Valid @RequestPart("data") ComplaintCreateDto dto,
//
//            @RequestPart(value="images", required=false)
//            List<MultipartFile> images
//    ) {
//
//        return ResponseEntity.ok(
//                apiService.createComplaint(dto, images)
//        );
//    }
//    @GetMapping("/homepage/dashboard/top10complaints")
//    public ResponseEntity<List<ComplaintResponseDto>> getLastComplaints() {
//
//        return ResponseEntity.ok(
//                apiService.getLast10Complaints()
//        );
//    }

//TODO: Replace with specification Query
        @GetMapping("/homepage/dashboard/statistics")
        public Map<String, Long> getHomeStatistics() {
            return Map.of(
                    "totalComplaints", statisticsService.getTotalComplaints(),
                    "newComplaints", statisticsService.getNewComplaints(),
                    "inProgressComplaints", statisticsService.getInProgressComplaints(),
                    "solvedComplaints", statisticsService.getSolvedComplaints(),
                    "institutionsCount", statisticsService.getDistinctInstitutionsCount(),
                    "todayComplaints",statisticsService.countTodayComplaints()
            );
        }

//        @GetMapping("citizenAccount/{accountId}")
    @GetMapping("/citizen/profile")
    public ResponseEntity<?> citizenProfile(
//                @PathVariable Long accountId
        ){
            return ResponseEntity.ok(apiService.getCitizenInfo());
        }

    @GetMapping("/employee/profile")
    public ResponseEntity<?> employeeProfile(
    ){
        return ResponseEntity.ok(apiService.getEmployeeInfoInfo());
    }

//    @GetMapping("/dashboard/citizen")
//    public ResponseEntity<?> getCitizenDashboard() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//        return ResponseEntity.ok(statisticsService.buildCitizenDashboardResponse(email));
//    }
//TODO: Replace with specification Query
    @GetMapping("/citizen/dashboard/statistics")
    public ResponseEntity<?> getCitizenDashboardStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(statisticsService.getCitizenDashboardStatistics(email));
    }

//    @GetMapping("/citizen/dashboard/top3Complaints")
//    public ResponseEntity<?> getTop3CitizenComplaints() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//        return ResponseEntity.ok(statisticsService.getTop3ComplaintsForCitizen(email));
//    }

    @GetMapping("/citizen/myComplaints")
    public ResponseEntity<?> getMyComplaints(ComplaintFilterRequestDto filter) {

        return ResponseEntity.ok(apiService.getComplaints(filter,true));
    }

    @GetMapping("/homepage/complaints")
    public ResponseEntity<?> getComplaints(ComplaintFilterRequestDto filter) {
        return ResponseEntity.ok(apiService.getComplaints(filter,false));
    }

    @GetMapping("/citizen/complaints/{complaintId}/timeline")
    public ResponseEntity<?> getTimeLine(@PathVariable Long complaintId) throws AccessDeniedException, NotFoundException {
        return ResponseEntity.ok(apiService.getTimeLine(complaintId));
    }

    @GetMapping("/homepage/complaints/{complaintId}")
    public ResponseEntity<?> getComplaint(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(apiService.getComplaint(complaintId));
    }

    @GetMapping("/homepage/complaint/{complaintId}/votes")
    public ResponseEntity<?> getVotes(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(votingService.getVotes(complaintId));
    }

    @PostMapping("/homepage/complaint/{complaintId}/like")
    public ResponseEntity<?> addLike(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(votingService.Voting(complaintId, VotingType.LIKE));
    }
    @PostMapping("/homepage/complaint/{complaintId}/disLike")
    public ResponseEntity<?> addDisLike(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(votingService.Voting(complaintId, VotingType.DISLIKE));
    }

//
//    @PatchMapping("/{id}/state")
//    public ResponseEntity<?> changeState(
//            @PathVariable Long id,
//            @Valid @RequestBody ComplaintUpdateStateDto dto) {
//
//        complaintService.changeState(id, dto);
//        return ResponseEntity.ok("State updated");
//    }
//    public void changeState(Long complaintId, ComplaintUpdateStateDto dto) {
//
//        Complaint complaint = complaintRepo.findById(complaintId)
//                .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//        complaint.setState(dto.newState());
//
//        // EventHandler سي:
//        // - يتحقق من صحة الانتقال
//        // - ينشئ Tracking Log
//        // - يضبط dateTimeOfSolve إذا لزم
//        complaintRepo.save(complaint);
//    }
//    public void deleteComplaint(Long id) {
//        Complaint complaint = complaintRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Complaint not found"));
//
//        complaintRepo.delete(complaint);
//        // EventHandler يمنع الحذف الفيزيائي
//    }
//
//    // حذف شكوى (soft delete)
//    @DeleteMapping("/{id}")
//    public ResponseEntity<?> deleteComplaint(@PathVariable Long id) {
//        complaintService.deleteComplaint(id);
//        return ResponseEntity.ok("Complaint deleted");
//    }
//    public record ComplaintUpdateStateDto(
//            @NotNull
//            ComplaintState newState
//    ) {}


}
