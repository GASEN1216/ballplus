package com.gasen.usercenterbackend.model.respond;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class indexEvent implements Serializable {
    private Long id;
    private String avatar;
    private String name;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private LocalTime eventTimee;
    private String location;
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private Integer participants;
    private Integer totalParticipants;
    private String remarks;
}
