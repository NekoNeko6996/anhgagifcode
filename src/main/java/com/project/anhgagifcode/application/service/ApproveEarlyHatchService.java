package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.ApproveEarlyHatchUseCase;
import com.project.anhgagifcode.application.port.out.CustomerPersistencePort;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.domain.exception.BusinessRuleViolationException;
import com.project.anhgagifcode.domain.exception.ResourceNotFoundException;
import com.project.anhgagifcode.domain.model.Customer;
import com.project.anhgagifcode.domain.model.Egg;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;

public class ApproveEarlyHatchService implements ApproveEarlyHatchUseCase {

    private final EggPersistencePort eggPort;
    private final CustomerPersistencePort customerPort;
    private final TransactionTemplate transactionTemplate;

    public ApproveEarlyHatchService(
            EggPersistencePort eggPort,
            CustomerPersistencePort customerPort,
            PlatformTransactionManager transactionManager) {
        this.eggPort = eggPort;
        this.customerPort = customerPort;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public void approveEarlyHatch(String eggId) {
        transactionTemplate.executeWithoutResult(status -> {
            // 1. Tải thông tin trứng và khóa trứng (Lock For Update)
            Egg egg = eggPort.loadEggForUpdate(eggId)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy trứng cần duyệt."));

            if (egg.getHatchAt() == null || !LocalDateTime.now().isBefore(egg.getHatchAt())) {
                throw new BusinessRuleViolationException("Trứng đã nở hoặc không ở trạng thái ấp.");
            }

            if ("CLAIMED".equals(egg.getStatus()) || "CANCELLED".equals(egg.getStatus())) {
                throw new BusinessRuleViolationException("Trứng đã được nhận hoặc đã bị hủy.");
            }

            // 2. Khóa thông tin khách hàng (Lock For Update) để chống Race Condition trừ điểm
            if (egg.getOrder() == null || egg.getOrder().getCustomerCode() == null) {
                throw new BusinessRuleViolationException("Thiếu thông tin đơn hàng hoặc khách hàng.");
            }
            String customerCode = egg.getOrder().getCustomerCode();
            Customer customer = customerPort.loadByCustomerCodeForUpdate(customerCode)
                    .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin khách hàng."));

            if (customer.getEarlyHatchCredits() <= 0) {
                throw new BusinessRuleViolationException("Khách hàng không đủ lượt duyệt sớm.");
            }

            // 3. Tiêu hao tín dụng duyệt sớm
            customer.setEarlyHatchCredits(customer.getEarlyHatchCredits() - 1);
            customerPort.saveCustomer(customer);

            // 4. Giảm thời gian ấp trứng đi 3 ngày
            LocalDateTime newHatchAt = egg.getHatchAt().minusDays(3);
            egg.setHatchAt(newHatchAt);

            // 5. Nếu thời gian nở mới đã qua, chuyển trạng thái sang READY_TO_CLAIM
            if (LocalDateTime.now().isAfter(newHatchAt) || LocalDateTime.now().isEqual(newHatchAt)) {
                egg.setStatus("READY_TO_CLAIM");
            }

            eggPort.saveEgg(egg);
        });
    }
}
