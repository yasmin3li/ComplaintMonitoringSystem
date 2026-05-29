package com.myapp.complaints;

import com.myapp.complaints.dto.EmployeeBadgeDto;
import com.myapp.complaints.dto.LoadTagDto;
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
                    "محترف _ أداء ممتاز خلال هذه الفترة",
                    BadgeLevel.GOLD,
                    "star"
            );
        }

        if (score >= 70) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "جيد جدًا",
                    "أداء قوي",
                    BadgeLevel.SILVER,
                    "award"
            );
        }

        if (score >= 50) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "جيد",
                    "يحافظ على مستوى مقبول في الأداء",
                    BadgeLevel.BRONZE,
                    "thumbs-up"
            );
        }

        if (score >= 30) {
            return new EmployeeBadgeDto(
                    BadgeType.PERFORMANCE,
                    "بحاجة لتحسين",
                    "الأداء منخفض",
                    BadgeLevel.ORANGE,
                    "trending-down"
            );
        }

        return new EmployeeBadgeDto(
                BadgeType.PERFORMANCE,
                "مقصر",
                "أداء سيئ _ متابعة إدارية مطلوبة",
                BadgeLevel.RED,
                "user-x"
        );
    }

    public static EmployeeBadgeDto buildResponseBadge(double responseRate) {

        if (responseRate <= 1) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة سريعة",
                    "سرعة عالية في البدء بمعالجة الشكاوى",
                    BadgeLevel.BLUE,
                    "zap"
            );
        }

        if (responseRate <= 2) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة جيدة",
                    "زمن استجابة ممتاز",
                    BadgeLevel.GREEN,
                    "timer"
            );
        }

        if (responseRate <= 3) {
            return new EmployeeBadgeDto(
                    BadgeType.RESPONSE,
                    "استجابة مقبولة",
                    "الأداء مستقر",
                    BadgeLevel.GRAY,
                    "clock"
            );
        }

        return new EmployeeBadgeDto(
                BadgeType.RESPONSE,
                "استجابة ضعيفة",
                "تحسين سرعة المعالجة مطلوب",
                BadgeLevel.YELLOW,
                "alert-circle"
        );
    }

    public static EmployeeBadgeDto buildMilestoneBadge(long threshold) {

            return new EmployeeBadgeDto(
                    BadgeType.MILESTONE,
                    String.format("%d شكوى معالجة", threshold),
                    String.format("لقد وصلت إلى %d شكوى معالجة", threshold),
                    BadgeLevel.ROYAL_BLUE,
                    "check-circle"
            );
            
    }

    public static LoadTagDto buildRecommendationBadge(double avgLoad) {

        if (avgLoad >= 80) {
            return new LoadTagDto(
                    BadgeType.LOAD,
                    "حمل مرتفع",
                    "الأكثر ازدحاما خلال هذه الفترة",
                    BadgeLevel.RED,
                    null,
                    Math.round(avgLoad*100)/100.0
            );
        }

        if (avgLoad >= 50) {
            return new LoadTagDto(
                    BadgeType.LOAD,
                    "حمل متوسط",
                    null,
                    BadgeLevel.ORANGE,
                    null,
                    Math.round(avgLoad*100)/100.0
            );
        }

        if (avgLoad >= 30) {
            return new LoadTagDto(
                    BadgeType.LOAD,
                    "حمل قليل",
                    "يحتمل اسناد المزيد من الشكاوى له",
                    BadgeLevel.YELLOW,
                    null,
                    Math.round(avgLoad*100)/100.0
            );
        }

        return new LoadTagDto(
                BadgeType.LOAD,
                "حمل قليل جدا",
                "الأقل حمل عمل - موصى به بشدة",
                BadgeLevel.GREEN,
                null,
                Math.round(avgLoad*100)/100.0
        );
    }

}