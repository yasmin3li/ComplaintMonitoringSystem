package com.myapp.complaints;

import com.myapp.complaints.enums.ComplaintState;

public class CommonUtils {

    private CommonUtils() {
    }

    public static ComplaintState fromArabicState(String state) {

        return switch (state) {
            case "جديدة" -> ComplaintState.NEW;
            case "قيد التقدم" -> ComplaintState.IN_PROGRESS;
            case "قيد المراجعة" -> ComplaintState.IN_VERIFY;
            case "محلولة" -> ComplaintState.RESOLVED;
            case "مرفوضة" ->ComplaintState.REJECTED;
            case "مغلقة" -> ComplaintState.CLOSED;
            case "مسندة" -> ComplaintState.ASSIGNED;
            case "تم مقاطعة التقدم" -> ComplaintState.CANCELLED;
            default -> throw new IllegalArgumentException("Invalid Arabic state: " + state);
        };

    }

    public static String toArabicState(ComplaintState state) {

        return switch (state) {
            case ComplaintState.NEW -> "جديدة";
            case ComplaintState.IN_PROGRESS -> "قيد التقدم";
            case ComplaintState.IN_VERIFY -> "قيد المراجعة";
            case ComplaintState.RESOLVED -> "محلولة";
            case ComplaintState.REJECTED ->"مرفوضة";
            case ComplaintState.CLOSED -> "مغلقة";
            case ComplaintState.ASSIGNED -> "مسندة";
            case ComplaintState.CANCELLED -> "تم مقاطعة التقدم";
            default -> throw new IllegalArgumentException("Invalid English state: " + state);
        };

    }
}