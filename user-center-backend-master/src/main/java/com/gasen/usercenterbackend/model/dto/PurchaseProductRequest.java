package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

@Data
public class PurchaseProductRequest {
    private Integer userId;
    private Integer productId;
} 