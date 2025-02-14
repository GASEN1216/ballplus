package com.gasen.usercenterbackend.model.respond;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class eventTemplates implements Serializable {
    private Long id;
    private String name;
    private LocalTime eventTime;
    private LocalTime eventTimee;
    private String location;
    private String locationDetail;
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private Integer totalParticipants;
    private String phoneNumber;
    private Byte type;
    private String remarks;
    private String labels;
    private Byte limits;
    private Boolean visibility;
    private Byte level;
    private Byte feeMode;
    private Float fee;
    private Boolean penalty;
}
