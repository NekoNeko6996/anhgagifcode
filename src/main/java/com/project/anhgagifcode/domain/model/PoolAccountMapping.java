package com.project.anhgagifcode.domain.model;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoolAccountMapping {
    private String id;
    private String accountId;
    private String poolId;
}