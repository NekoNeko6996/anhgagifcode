package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClaimedAccountDto {
    private String username;
    private String password;
    private String platform;
    private String tier;
}
