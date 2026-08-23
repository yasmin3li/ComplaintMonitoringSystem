package com.myapp.complaints.controller;


import com.myapp.complaints.DAO.AccountRepo;
import com.myapp.complaints.DAO.EmployeeRepo;
import com.myapp.complaints.dto.*;
import com.myapp.complaints.entity.Account;
import com.myapp.complaints.entity.Employee;
import com.myapp.complaints.enums.ActionType;
import com.myapp.complaints.enums.VotingType;
import com.myapp.complaints.exceptionHandller.ApiException;
import com.myapp.complaints.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.javassist.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.Map;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ApiController {


    private  final ApiService apiService;
    private final StatisticsService statisticsService;
    private final VotingService votingService;
    private final NotificationService notificationService;
    private final CitizenComplaintWorkFlow citizenComplaintWorkFlow;
    private final ReceptionistComplaintWorkflow receptionistComplaintWorkflow;
    private final EmployeePerformanceService employeePerformanceService;
    private final EmployeeComplaintWorkFlow employeeComplaintWorkFlow;
    private final EmployeeRepo employeeRepo;
    private final RatingService ratingService;

    @PostMapping("/complaint")
    public ResponseEntity<?> createComplaint(
            @Valid @RequestBody ComplaintCreateDto dto) {

        return ResponseEntity.ok(
                citizenComplaintWorkFlow.createComplaint(dto)
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

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/profile")
    public ResponseEntity<?> citizenProfile(
//                @PathVariable Long accountId
        ){
            return ResponseEntity.ok(apiService.getCitizenInfo());
        }

//    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
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
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/dashboard/statistics")
    public ResponseEntity<?> getCitizenDashboardStatistics() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(statisticsService.getCitizenDashboardStatistics(email));
    }


    @GetMapping("/employees/badges")
    public ResponseEntity<Object> getEmployeeBadges(
//            @RequestParam(name = "onlyLatest", required = false, defaultValue = "false") boolean onlyLatest
    ) {
        return ResponseEntity.ok(employeePerformanceService.getEmployeeBadges());
    }

    @GetMapping("/employee/dashboard/statistics")
    public ResponseEntity<?> getEmployeeDashboardStatistics() {
        return ResponseEntity.ok(statisticsService.getEmployeeDashboardStatistics());
    }

//    @GetMapping("/citizen/dashboard/top3Complaints")
//    public ResponseEntity<?> getTop3CitizenComplaints() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        String email = auth.getName();
//        return ResponseEntity.ok(statisticsService.getTop3ComplaintsForCitizen(email));
//    }
    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/myComplaints")
    public ResponseEntity<?> getMyComplaints(ComplaintFilterRequestDto filter) {

        return ResponseEntity.ok(apiService.getComplaints(filter));
    }

    @GetMapping("/homepage/complaints")
    public ResponseEntity<?> getComplaints(ComplaintFilterRequestDto filter) {
        return ResponseEntity.ok(apiService.getComplaints(filter));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/complaints/{complaintId}/timeline")
    public ResponseEntity<?> getTimeLine(@PathVariable Long complaintId) throws AccessDeniedException, NotFoundException {
        return ResponseEntity.ok(apiService.getTimeLine(complaintId));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/myComplaints/{complaintId}")
    public ResponseEntity<?> getCitizenComplaint(@PathVariable Long complaintId){
        return ResponseEntity.ok(apiService.openComplaint(complaintId));
    }

    @GetMapping("/homepage/complaints/{complaintId}")
    public ResponseEntity<?> getComplaint(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(apiService.openComplaint(complaintId));
    }

//    @PreAuthorize("hasRole('RECEPTIONIST')")
    @GetMapping("/institutions/complaints/{complaintId}")
    public ResponseEntity<?> getInstitutionComplaint(@PathVariable Long complaintId){
        return ResponseEntity.ok(apiService.openComplaint(complaintId));
    }

//    @PreAuthorize("hasRole('RECEPTIONIST')")
    @GetMapping("/institutions/complaints")
    public ResponseEntity<?> geNewInstitutionComplaints(ComplaintFilterRequestDto filter){
        return ResponseEntity.ok(apiService.getComplaints(filter));
    }

    @GetMapping("/images/complaint/{complaintId}")
    public ResponseEntity<?> complaintImages( @PathVariable Long complaintId){
        return ResponseEntity.ok(apiService.complaintImages(complaintId));
    }

    @GetMapping("/homepage/complaint/{complaintId}/votes")
    public ResponseEntity<?> getVotes(@PathVariable Long complaintId) {
        return ResponseEntity.ok(votingService.getVotes(complaintId));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping("/homepage/complaint/{complaintId}/like")
    public ResponseEntity<?> addLike(@PathVariable Long complaintId){
        return ResponseEntity.ok(votingService.Voting(complaintId, VotingType.LIKE));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping("/homepage/complaint/{complaintId}/disLike")
    public ResponseEntity<?> addDisLike(@PathVariable Long complaintId) throws NotFoundException {
        return ResponseEntity.ok(votingService.Voting(complaintId, VotingType.DISLIKE));
    }

//TODO: add logic change phone number / email / and dealing with employee's account
    @PreAuthorize("hasRole('CITIZEN')")
    @PatchMapping("/citizen/profile")
    public ResponseEntity<?> updateProfile(@RequestBody @Valid UpdateCitizenProfileInfoDto dto,
                                           Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(apiService.updateCitizenProfile(email,dto));
    }
    @PatchMapping("/employee/profile")
    public ResponseEntity<?> updateEmployeeProfile(@RequestBody @Valid UpdateEmployeeProfileInfoDto dto,
                                                  Authentication auth) {
        String email = auth.getName();
        return ResponseEntity.ok(apiService.updateEmployeeProfile(email,dto));
    }
//    @PostMapping("/citizen/sendNotification")
//    public ResponseEntity<?> sendNotifications(Authentication auth,@RequestBody String reason){
//        String email = auth.getName();
//        return ResponseEntity.ok(notificationService.buildNotification();
//    }

    @GetMapping("/notifications/statistics")
    public ResponseEntity<?> notificationStatistics(Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.getNotificationStatistics(email));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/notifications")
    public ResponseEntity<?> displayNotifications(Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.displayNotifications(email));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @GetMapping("/citizen/notifications/{notificationId}")
    public ResponseEntity<?> openNotification(Authentication auth, @PathVariable Long notificationId){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.openNotification(email,notificationId));
    }

    @PreAuthorize("hasRole('CITIZEN')")
    @PostMapping("/citizen/notifications/mark")
    public ResponseEntity<?> marksAsReadAllCitizenNotification(Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.marksAsReadAllNotifications(email));
    }

//    @PreAuthorize("hasRole('RECEPTIONIST')")
    @PostMapping("/employee/notifications/mark")
    public ResponseEntity<?> marksAsReadAllEmployeeNotification(Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.marksAsReadAllNotifications(email));
    }

//    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @GetMapping("/employee/notifications")
    public ResponseEntity<?> displayEmployeeNotifications(Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.displayNotifications(email));
    }

//    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @GetMapping("/employee/notifications/{notificationId}")
    public ResponseEntity<?> openEmployeeNotification(Authentication auth, @PathVariable Long notificationId){
        String email = auth.getName();
        return ResponseEntity.ok(notificationService.openNotification(email,notificationId));
    }

    @GetMapping("/allGovernorates")
    public ResponseEntity<?> governorates(){

        return ResponseEntity.ok(apiService.governorates());
    }

    @GetMapping("/sectorGovernorates/findByGovernorateId/{governorateId}")
    public ResponseEntity<?> sectorGovernorates( @PathVariable Long governorateId){

        return ResponseEntity.ok(apiService.sectorGovernorates(governorateId));
    }

    @GetMapping("/institutionSectorGovernorates/findBySectorGovernorateIdAndIsActiveTrue/{sectorGovernorateId}")
    public ResponseEntity<?> institutionSectorGovernorates( @PathVariable Long sectorGovernorateId){
        return ResponseEntity.ok(apiService.institutionSectorGovernorates(sectorGovernorateId));
    }

    @GetMapping("/servicesAvailable/findByInstitutionId/{institutionId}")
    public ResponseEntity<?> servicesAvailable( @PathVariable Long institutionId){
        return ResponseEntity.ok(apiService.servicesAvailable(institutionId));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @PostMapping("/employee/complaint/reject")
    public ResponseEntity<?> rejectComplaint(@RequestBody ComplaintRejectDto dto,Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(employeeComplaintWorkFlow.rejectComplaint(email,dto));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @PostMapping("/employee/complaint/accept/{complaintId}")
    public ResponseEntity<?> acceptComplaintAndForward(@PathVariable Long complaintId){
        return ResponseEntity.ok(receptionistComplaintWorkflow.acceptAndForwardToManager(complaintId));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @PostMapping("/employee/complaint/priority")
    public ResponseEntity<?> addComplaintPriority(@RequestBody ComplaintPriorityDto dto){
        return ResponseEntity.ok(apiService.addComplaintPriority(dto));
    }

    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @PostMapping("/employee/complaint/{complaintId}/review")
    public ResponseEntity<?> reviewComplaint(@PathVariable Long complaintId){
        return ResponseEntity.ok(receptionistComplaintWorkflow.inReview(complaintId));
    }

    @PatchMapping("/complaint/update")
    public ResponseEntity<?> updateComplaint(@RequestBody UpdateComplaintDto dto, Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(apiService.updateComplaint(email,dto));
    }

    @PostMapping("/complaint/uploadImages")
    public ResponseEntity<?> uploadComplaintImages(@RequestBody UpdateComplaintDto dto, Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(employeeComplaintWorkFlow.updateComplaint(email,dto));
    }

    //@PreAuthorize("hasAnyRole('CITIZEN','ADMIN')")
    @DeleteMapping("/complaint/delete/{complaintId}")
    public ResponseEntity<?> deleteComplaint(@PathVariable Long complaintId, Authentication auth){
        String email = auth.getName();
        return ResponseEntity.ok(apiService.deleteComplaint(email,complaintId));
    }

//    we will not use this action/state at this version.
//    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    @PostMapping("/complaint/closeComplaint")
    public ResponseEntity<?> closeComplaint(@RequestBody ComplaintRejectDto dto){
        return ResponseEntity.ok(employeeComplaintWorkFlow.closeComplaint(dto));
    }

    @PostMapping("/complaint/finishSolve")
    public ResponseEntity<?> finishSolveComplaint(@RequestBody ComplaintSolveDto dto){
        return ResponseEntity.ok(employeeComplaintWorkFlow.solveComplaint(dto));
    }

    @PostMapping("/complaint/startSolve/{complaintId}")
    public ResponseEntity<?> startSolveComplaint(@PathVariable Long complaintId){
        return ResponseEntity.ok(employeeComplaintWorkFlow.startSolveComplaint(complaintId));
    }

    @GetMapping("/delayedComplaints")
    public ResponseEntity<?> getDelayedComplaints(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Boolean myComplaints
            ) {

        if (employeeId != null) {

            Optional<Employee> employee = employeeRepo.findByAccountId(employeeId);

            if(employee.isEmpty()){

                throw new ApiException("Employee not found",HttpStatus.NOT_FOUND);

            }
            return ResponseEntity.ok(statisticsService.getDelayedComplaints(employee.get()));

        } else {
            return ResponseEntity.ok(apiService.getDelayedComplaints(myComplaints));
        }
    }

    @PostMapping("/sendNotification/manual")
    public ResponseEntity<ApiResponseDto<Object>>
    sendManualNotification(
            @RequestBody SendManualNotificationDto dto
    ) {

        return ResponseEntity.ok(
                notificationService.sendManualNotification(dto)
        );
    }

    @PostMapping("/complaint/{complaintId}/rating/{starNumber}")
    public ResponseEntity<ApiResponseDto<?>> rating(@PathVariable Integer starNumber,@PathVariable Long complaintId){
        return ResponseEntity.ok(ratingService.rating(complaintId,starNumber));
    }

    @GetMapping("/complaint/{complaintId}/rate")
    public ResponseEntity<RatingDto> getRate(@PathVariable Long complaintId){
        return ResponseEntity.ok(ratingService.getRate(complaintId));
    }

}
