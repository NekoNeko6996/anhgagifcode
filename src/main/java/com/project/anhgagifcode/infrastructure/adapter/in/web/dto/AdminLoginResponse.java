package com.project.anhgagifcode.infrastructure.adapter.in.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminLoginResponse {
    private String accessToken;
    private String tokenType;
    private String username;
    private String role;
}