package com.gasen.usercenterbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserTrendResponseDTO {
    private List<TrendDataDTO> data;
} 