package com.myapp.complaints.service;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class Formatter {

// TODO: later maybe add publicId at Complaint entity
    public String complaintIdFormatter(long number){

        int year = LocalDateTime.now().getYear();
        return  String.format("%d%d",year, number) + "#";
    }
}
