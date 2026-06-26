package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.ClaimEggUseCase;
import com.project.anhgagifcode.application.port.in.dto.ClaimEggResponse;
import com.project.anhgagifcode.application.port.out.EggOpeningLogPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.GiftAccountPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.EggOpeningLog;
import com.project.anhgagifcode.domain.model.GiftAccount;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@RequiredArgsConstructor
public class ClaimEggService implements ClaimEggUseCase {

    private final EggPersistencePort eggPort;
    private final GiftAccountPersistencePort accountPort;
    private final EggOpeningLogPersistencePort logPort;
    private final Random random = new Random();

    @Override
    @Transactional // Rất quan trọng để kích hoạt Pessimistic Lock
    public ClaimEggResponse claimEggReward(String eggId, String ipAddress) {
        
        // 1. Load và Khóa Trứng (Lock For Update)
        Egg egg = eggPort.loadEggForUpdate(eggId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng hợp lệ."));

        // 2. Validate trạng thái trứng (Phải check lại lần nữa để đề phòng bypass API)
        if ("CLAIMED".equals(egg.getStatus()) || "CANCELLED".equals(egg.getStatus())) {
            throw new BusinessRuleViolationException("Trứng này đã được mở hoặc bị hủy.");
        }
        if (egg.getEggType() == 2 && egg.getHatchAt() != null && LocalDateTime.now().isBefore(egg.getHatchAt())) {
            throw new BusinessRuleViolationException("Trứng đang ấp, chưa đến thời gian mở.");
        }

        // 3. Đếm số lượng quà trong Kho
        String poolId = egg.getGiftPool().getId();
        long availableCount = accountPort.countAvailableAccountsByPoolId(poolId);
        
        if (availableCount == 0) {
            throw new BusinessRuleViolationException("Kho quà hiện tại đã hết, vui lòng quay lại sau.");
        }

        // 4. Random Offset và Bắt Quà (Lock For Update GiftAccount)
        int randomOffset = random.nextInt((int) availableCount);
        GiftAccount assignedAccount = accountPort.pickRandomAvailableAccountForUpdate(poolId, randomOffset)
                .orElseThrow(() -> new BusinessRuleViolationException("Lỗi hệ thống khi bốc quà, vui lòng thử lại."));

        // 5. Cập nhật Account
        assignedAccount.setStatus("ASSIGNED");
        assignedAccount.setAssignedAt(LocalDateTime.now());
        accountPort.updateAccount(assignedAccount);

        // 6. Cập nhật Trứng
        egg.setStatus("CLAIMED");
        egg.setAccount(assignedAccount);
        eggPort.saveEgg(egg);

        // 7. Ghi Log
        EggOpeningLog log = EggOpeningLog.builder()
                .id(UUID.randomUUID().toString())
                .eggId(egg.getId())
                .accountId(assignedAccount.getId())
                .actionType("CLAIM_REWARD")
                .triggeredBy("USER_IP")
                .ipAddress(ipAddress)
                .createdAt(LocalDateTime.now())
                .build();
        logPort.saveLog(log);

        // 8. Trả kết quả
        return ClaimEggResponse.builder()
                .username(assignedAccount.getUsername())
                .password(assignedAccount.getPassword())
                .platform(assignedAccount.getPlatform())
                .message("Chúc mừng! Bạn đã mở trứng thành công.")
                .build();
    }
}