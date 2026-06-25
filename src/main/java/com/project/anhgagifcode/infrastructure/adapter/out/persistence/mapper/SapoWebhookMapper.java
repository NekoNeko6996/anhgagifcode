package com.project.anhgagifcode.infrastructure.adapter.in.web.mapper;

import com.project.anhgagifcode.domain.model.SapoOrder;
import com.project.anhgagifcode.domain.model.SapoOrderItem;
import com.project.anhgagifcode.infrastructure.adapter.in.web.dto.SapoWebhookRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SapoWebhookMapper {

    @Mapping(source = "lineItems", target = "orderItems")
    // Bỏ qua ID vì đây là dữ liệu mới từ Sapo, hệ thống sẽ tự cấp UUID hoặc map ID cũ sau
    @Mapping(target = "id", ignore = true) 
    SapoOrder toDomain(SapoWebhookRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orderId", ignore = true)
    SapoOrderItem itemToDomain(SapoWebhookRequest.SapoWebhookItemRequest request);
}