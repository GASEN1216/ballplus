package com.gasen.usercenterbackend.model.respond;

import com.gasen.usercenterbackend.model.userIdAndAvatar;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class detailEvent implements Serializable {
    private Long id;
    private Integer appId;
    private String avatar;
    private String name;
    private LocalDate eventDate;
    private LocalTime eventTime;
    private LocalTime eventTimee;
    private String location;
    private String locationDetail;
    private BigDecimal latitude; // 纬度
    private BigDecimal longitude; // 经度
    private Integer participants;
    private Integer totalParticipants;
    private String phoneNumber;
    private Byte type;
    private Byte limits;
    private Byte level;
    private Byte feeMode;
    private Float fee;
    private String remarks;
    private List<userIdAndAvatar> persons;
}
