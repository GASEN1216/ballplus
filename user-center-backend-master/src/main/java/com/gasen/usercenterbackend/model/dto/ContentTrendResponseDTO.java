package com.gasen.usercenterbackend.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class ContentTrendResponseDTO {
    private List<TrendDataDTO> postData;
    private List<TrendDataDTO> commentData;
} 