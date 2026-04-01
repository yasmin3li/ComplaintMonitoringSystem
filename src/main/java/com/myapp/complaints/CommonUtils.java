package com.myapp.complaints;

import com.myapp.complaints.enums.ComplaintState;

public class CommonUtils {

    private CommonUtils() {
    }

    public static ComplaintState fromArabicState(String state) {

        return switch (state) {
            case "جديدة" -> ComplaintState.NEW;
            case "قيد التقدم" -> ComplaintState.IN_PROGRESS;
            case "محلولة" -> ComplaintState.RESOLVED;
            case "مرفوضة" ->ComplaintState.REJECTED;
            case "مغلقة" -> ComplaintState.CLOSED;
            case "مسندة" -> ComplaintState.ASSIGNED;
            case "تم مقاطعة التقدم" -> ComplaintState.CANCELLED;
            default -> throw new IllegalArgumentException("Invalid Arabic state: " + state);
        };

    }
}