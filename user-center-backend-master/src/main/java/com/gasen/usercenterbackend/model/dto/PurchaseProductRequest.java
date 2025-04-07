package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

@Data
public class PurchaseProductRequest {
    private Long userId;
    private Integer productId;
} 