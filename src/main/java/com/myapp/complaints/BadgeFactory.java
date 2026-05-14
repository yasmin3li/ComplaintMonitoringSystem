package com.myapp.complaints;

import com.myapp.complaints.dto.EmployeeBadgeDto;
import com.myapp.complaints.enums.BadgeLevel;
import com.myapp.complaints.enums.BadgeType;

public class BadgeFactory {

    private BadgeFactory() {
    }

    public static EmployeeBadgeDto buildPerformanceBadge(double score) {

        if (score >= 90) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "موظف متميز",
                    "أداء ممتاز خلال هذه الفترة",
                    BadgeLevel.GOLD,
                    "star"
            );
        }

        if (score >= 70) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "جيد جدًا",
                    "أداء قوي ومستقر",
                    BadgeLevel.SILVER,
                    "award"
            );
        }

        if (score >= 50) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "جيد",
                    "يحافظ على مستوى مقبول",
                    BadgeLevel.BRONZE,
                    "shield"
            );
        }

        if (score >= 30) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "بحاجة لتحسين",
                    "الأداء منخفض",
                    BadgeLevel.WARNING,
                    "alert"
            );
        }

        return new EmployeeBadgeDto(
                BadgeType.PERFORMANCE,
                "مقصر",
                "متابعة إدارية مطلوبة",
                BadgeLevel.DANGER,
                "x"
        );
    }

    public static EmployeeBadgeDto buildResponseBadge(double responseRate) {

        if (responseRate <= 1) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة سريعة",
                    "سرعة عالية في معالجة الشكاوى",
                    BadgeLevel.GOLD,
                    "zap"
            );
        }

        if (responseRate <= 2) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة جيدة",
                    "زمن استجابة ممتاز",
                    BadgeLevel.SILVER,
                    "clock"
            );
        }

        if (responseRate <= 3) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة مقبولة",
                    "الأداء مستقر",
                    BadgeLevel.BRONZE,
                    "timer"
            );
        }

        return new EmployeeBadgeDto(
                BadgeType.RESPONSE,
                "استجابة ضعيفة",
                "تحسين سرعة المعالجة مطلوب",
                BadgeLevel.WARNING,
                "alert"
        );
    }

    public static EmployeeBadgeDto buildMilestoneBadge(long handledCount,long threshold) {

            return new EmployeeBadgeDto(
                    BadgeType.MILESTONE,
                    String.format("شكوى معالجة %d", threshold),
                    String.format("لقد وصلت إلى %d شكوى معالجة", threshold),
                    BadgeLevel.BLUE,
                    "trophy"
            );
            
    }

}