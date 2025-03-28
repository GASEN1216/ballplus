package com.gasen.usercenterbackend.model.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class ScoreHistoryDTO {

    private Long id;
    
    private Integer changeAmount;
    
    private String type;
    
    private String description;
    
    private LocalDateTime createdTime;
} 