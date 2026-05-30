package com.myapp.complaints.enums;

import lombok.Getter;

@Getter
public enum ComplaintPriority {
        LOW(7),
        MEDIUM(5),
        HIGH(3),
        CRITICAL(1);

    ComplaintPriority(int slaDays) {
        this.slaDays = slaDays;
    }

    private final int slaDays;
    }
