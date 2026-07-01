package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Getter
@Builder
@Value
public class ClaimEggResponse {
    private String username;
    private String password;
    private String platform;
    private String tier;
    private String message;
}