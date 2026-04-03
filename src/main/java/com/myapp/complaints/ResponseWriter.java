package com.myapp.complaints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.myapp.complaints.dto.ApiResponseDto;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ResponseWriter {

    private final ObjectMapper objectMapper;

    public  void sendError(HttpServletResponse response, int status, String message) throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");

        ApiResponseDto<?> dto =
                new ApiResponseDto<>(false, message, Optional.empty());

        objectMapper.writeValue(response.getOutputStream(), dto);
    }

}
