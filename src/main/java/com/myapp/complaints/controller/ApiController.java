package com.myapp.complaints.controller;


import com.myapp.complaints.dto.ComplaintCreateDto;
import com.myapp.complaints.dto.ComplaintResponseDto;
import com.myapp.complaints.service.ApiService;
import com.myapp.complaints.service.StatisticsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {


    private  final ApiService apiService;
    @PostMapping("/complaint")
    public ResponseEntity<?> createComplaint(
            @Valid @RequestBody ComplaintCreateDto dto) {

        return ResponseEntity.ok(
                apiService.createComplaint(dto)
        );
    }

    @GetMapping("/latest")
    public ResponseEntity<List<ComplaintResponseDto>> getLastComplaints() {

        return ResponseEntity.ok(
                apiService.getLast10Complaints()
        );
    }

        private final StatisticsService statisticsService;

        @GetMapping("/statistics/homePage")
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
