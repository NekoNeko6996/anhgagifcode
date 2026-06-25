package com.project.anhgagifcode.domain.model;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiftAccount {
    private String id;
    private String username;
    private String password;
    private String status;
    private String platform; // Đã sửa lỗi chính tả "platfrom" từ Entity
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime assignedAt;
}