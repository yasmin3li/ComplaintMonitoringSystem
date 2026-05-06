package com.myapp.complaints;

import com.myapp.complaints.enums.ComplaintPriority;
import com.myapp.complaints.enums.ComplaintState;
import com.myapp.complaints.exceptionHandller.ApiException;
import org.springframework.http.HttpStatus;

public class CommonUtils {

    private CommonUtils() {
    }

    public static ComplaintState fromArabicState(String state) {

        return switch (state) {
            case "جديدة" -> ComplaintState.NEW;
            case "قيد التقدم" -> ComplaintState.IN_PROGRESS;
            case "قيد المراجعة" -> ComplaintState.IN_REVIEW;
            case "محلولة" -> ComplaintState.RESOLVED;
            case "مرفوضة" ->ComplaintState.REJECTED;
            case "مغلقة" -> ComplaintState.CLOSED;
            case "مسندة" -> ComplaintState.ASSIGNED;
            case "تم مقاطعة التقدم" -> ComplaintState.CANCELLED;
            case "محولة" -> ComplaintState.FORWARDED_TO_MANAGER;
            default -> throw new ApiException("Invalid Arabic state: " + state, HttpStatus.BAD_REQUEST);
        };

    }

    public static String toArabicState(ComplaintState state) {

        return switch (state) {
            case ComplaintState.NEW -> "جديدة";
            case ComplaintState.IN_PROGRESS -> "قيد التقدم";
            case ComplaintState.IN_REVIEW -> "قيد المراجعة";
            case ComplaintState.RESOLVED -> "محلولة";
            case ComplaintState.REJECTED ->"مرفوضة";
            case ComplaintState.CLOSED -> "مغلقة";
            case ComplaintState.ASSIGNED -> "مسندة";
            case ComplaintState.CANCELLED -> "تم مقاطعة التقدم";
            case ComplaintState.FORWARDED_TO_MANAGER -> "محولة";
            default -> throw new ApiException("Invalid English state: " + state, HttpStatus.BAD_REQUEST);
        };
    }

    public static boolean validateAndEncodePassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ApiException("Password cannot be empty",HttpStatus.BAD_REQUEST);
        }
        if (rawPassword.length() < 8) {
            throw new ApiException("Password must be at least 8 characters long",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[A-Z].*")) {
            throw new ApiException("Password must contain at least one uppercase letter",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[a-z].*")) {
            throw new ApiException("Password must contain at least one lowercase letter",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*\\d.*")) {
            throw new ApiException("Password must contain at least one digit",HttpStatus.BAD_REQUEST);
        }
        if (!rawPassword.matches(".*[!@#%^&*].*")) {
            throw new ApiException("Password must contain at least one special character",HttpStatus.BAD_REQUEST);
        }
        if (rawPassword.matches(".*[$].*")) {
            throw new ApiException("Password must not contain this special character: $",HttpStatus.BAD_REQUEST);
        }
        return true;
    }


    public static ComplaintPriority fromArabicPriority(String priority) {

        return switch (priority) {
            case "أولوية منخفضة" -> ComplaintPriority.LOW;
            case "أولوية متوسطة" -> ComplaintPriority.MEDIUM;
            case "أولوية عالية" ->  ComplaintPriority.HIGH;
            case "أولوية حرجة" ->   ComplaintPriority.CRITICAL;
            case "لم يتم اسناد أولوية بعد" -> ComplaintPriority.No_PRIORITY_Yet;
            default -> throw new ApiException("Invalid Arabic priority: " + priority, HttpStatus.BAD_REQUEST);
        };

    }

    public static String toArabicSPriority(ComplaintPriority priority) {

        return switch (priority) {
            case ComplaintPriority.LOW -> "أولوية منخفضة";
            case ComplaintPriority.MEDIUM-> "أولوية متوسطة";
            case ComplaintPriority.HIGH-> "أولوية عالية";
            case ComplaintPriority.CRITICAL -> "أولوية حرجة";
            case No_PRIORITY_Yet -> "لم يتم اسناد أولوية بعد";
            default -> throw new ApiException("Invalid English state: " + priority, HttpStatus.BAD_REQUEST);
        };
    }
}