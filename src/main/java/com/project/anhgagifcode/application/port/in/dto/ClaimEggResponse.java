package com.project.anhgagifcode.application.port.in.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import java.util.List;

@Getter
@Builder
@Value
public class ClaimEggResponse {
    private List<ClaimedAccountDto> accounts;
    private int stuckCount;
    private String message;
}