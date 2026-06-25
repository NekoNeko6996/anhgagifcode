package com.project.anhgagifcode.application.service;

import com.project.anhgagifcode.application.port.in.SyncSapoOrderUseCase;
import com.project.anhgagifcode.application.port.out.EggPersistencePort;
import com.project.anhgagifcode.application.port.out.ProductEggMappingPersistencePort;
import com.project.anhgagifcode.application.port.out.SapoOrderPersistencePort;
import com.project.anhgagifcode.domain.model.Egg;
import com.project.anhgagifcode.domain.model.ProductEggMapping;
import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.domain.model.SapoOrderItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class SyncSapoOrderService implements SyncSapoOrderUseCase {

    private final SapoOrderPersistencePort orderPersistencePort;
    private final EggPersistencePort eggPersistencePort;
    private final ProductEggMappingPersistencePort mappingPersistencePort; // Bổ sung Port này

    @Override
    @Transactional
    public void syncOrder(SapoOrder incomingOrder) {
        Optional<SapoOrder> existingOrderOpt = orderPersistencePort.loadOrderByCode(incomingOrder.getOrderCode());

        if (existingOrderOpt.isPresent()) {
            SapoOrder existingOrder = existingOrderOpt.get();
            if (!incomingOrder.getUpdatedAt().isAfter(existingOrder.getUpdatedAt())) {
                log.info("Bỏ qua webhook cũ cho đơn hàng: {}", incomingOrder.getOrderCode());
                return;
            }
            incomingOrder.setId(existingOrder.getId());
            if (incomingOrder.getOrderItems() != null) {
                incomingOrder.getOrderItems().forEach(item -> item.setOrderId(existingOrder.getId()));
            }
        }

        // 1. Lưu lại đơn hàng
        SapoOrder savedOrder = orderPersistencePort.saveOrder(incomingOrder);
        log.info("Đồng bộ thành công đơn hàng: {}", savedOrder.getOrderCode());

        // 2. Logic Hủy Đơn -> Hủy Trứng
        if ("cancelled".equalsIgnoreCase(incomingOrder.getStatus())) {
            eggPersistencePort.cancelEggsByOrderId(savedOrder.getId());
            log.info("Đã huỷ toàn bộ trứng thuộc đơn hàng: {}", savedOrder.getOrderCode());
            return; // Dừng luồng nếu đã huỷ
        }

        // 3. Logic Đã Thanh Toán -> Sinh Trứng
        if ("paid".equalsIgnoreCase(incomingOrder.getFinancialStatus())) {
            generateEggIfEligible(savedOrder);
        }
    }

    /**
     * Private method phụ trách việc sinh trứng
     */
    private void generateEggIfEligible(SapoOrder order) {
        // Lấy toàn bộ ID sản phẩm mà khách đã mua
        List<String> productIds = order.getOrderItems().stream()
                .map(SapoOrderItem::getSapoProductId)
                .collect(Collectors.toList());

        // Tìm luật đổi quà có Tier cao nhất (đã viết hàm trong Repository)
        Optional<ProductEggMapping> mappingOpt = mappingPersistencePort.loadHighestTierMapping(productIds);

        if (mappingOpt.isPresent()) {
            ProductEggMapping mapping = mappingOpt.get();

            // Check xem đơn này đã từng được phát trứng loại này chưa (chống duplicate webhook)
            if (!eggPersistencePort.existsByOrderIdAndEggType(order.getId(), mapping.getEggType())) {
                
                Egg newEgg = Egg.builder()
                        .eggType(mapping.getEggType())
                        .status("READY_TO_CLAIM") // Trứng đã sẵn sàng để khách mở
                        .hatchAt(LocalDateTime.now()) // Có thể setup cộng thêm thời gian delay nếu muốn
                        .order(order)
                        .giftPool(mapping.getGiftPool())
                        .build();

                eggPersistencePort.saveEgg(newEgg);
                log.info("Đã sinh trứng loại [{}] (Tier {}) cho đơn hàng {}", 
                        mapping.getEggType(), mapping.getEggTier(), order.getOrderCode());
            }
        } else {
            log.info("Đơn hàng {} không chứa sản phẩm nào nằm trong chương trình nhận trứng.", order.getOrderCode());
        }
    }
}