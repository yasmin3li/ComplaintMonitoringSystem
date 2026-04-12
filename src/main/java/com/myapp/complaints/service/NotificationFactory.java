package com.myapp.complaints.service;

import com.myapp.complaints.enums.ComplaintState;

public class NotificationFactory {

    public static String getTitle(ComplaintState state) {
        return switch (state) {
            case NEW -> "تم ارسال شكوى جديدة";
            case IN_PROGRESS -> "Complaint In Progress";
            case RESOLVED -> "تم حل شكواك";
            case REJECTED -> "تم رفض شكواك";
            case CLOSED -> "Complaint Closed";
            case ASSIGNED -> "Complaint Assigned";
            case CANCELLED -> "Complaint Cancelled";
            case IN_REVIEW -> "شكواك قيد المراجعة";
        };
    }

    public static String getMessage(ComplaintState state, String complaintTitle,String reason) {
        return switch (state) {
            case NEW -> "تم ارسال شكواك  \"" + complaintTitle + "\" بنجاح";
            case IN_PROGRESS -> "شكواك \"" + complaintTitle + "\" قيد التقدم ";
            case RESOLVED -> "تم حل شكواك  \"" + complaintTitle + "\" بنجاح";
            case REJECTED -> "تم رفض شكواك  \"" + complaintTitle + " بسبب: " +reason;
            case CLOSED -> "Your complaint \"" + complaintTitle + "\" has been closed.";
            case ASSIGNED -> "Your complaint \"" + complaintTitle + "\" has been assigned to an employee.";
            case CANCELLED -> "Your complaint \"" + complaintTitle + "\" has been cancelled.";
            case IN_REVIEW -> "شكواك \"" + complaintTitle + "\" قيد المراجعة ";
        };
    }
}
