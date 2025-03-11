package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.*;
import com.gasen.usercenterbackend.model.respond.detailEvent;
import com.gasen.usercenterbackend.model.respond.eventTemplates;
import com.gasen.usercenterbackend.model.respond.indexEvent;
import com.gasen.usercenterbackend.model.respond.indexEventWithState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("event")
public class Event implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
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

    private Integer totalParticipants;

    private Integer participants;

    private String phoneNumber;

    private Byte type;

    private String remarks;

    private  String labels;

    private Byte limits;

    private Boolean visibility;

    private Byte level;

    private Byte feeMode;

    private Float fee;

    private Boolean penalty;

    private Boolean isTemplate;

    private Byte state;

    /**
     * 逻辑删除
     */
    @TableLogic(value = "0", delval = "1")
    private Integer isDelete;

    /**
     * 创建账号时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(update = "now()")
    private LocalDateTime updateTime;

    public indexEvent toIndexEvent() {
        return new indexEvent()
                .setId(id)
                .setAvatar(avatar)
                .setName(name)
                .setEventDate(eventDate)
                .setEventTime(eventTime)
                .setEventTimee(eventTimee)
                .setLocation(location)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setParticipants(participants)
                .setTotalParticipants(totalParticipants)
                .setRemarks(remarks);
    }

    public indexEventWithState toIndexEventWithState() {
        return new indexEventWithState()
                .setId(id)
                .setAvatar(avatar)
                .setName(name)
                .setEventDate(eventDate)
                .setEventTime(eventTime)
                .setEventTimee(eventTimee)
                .setLocation(location)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setParticipants(participants)
                .setTotalParticipants(totalParticipants)
                .setRemarks(remarks)
                .setState(state);
    }

    public eventTemplates toEventTemplates() {
        return new eventTemplates()
                .setId(id)
                .setName(name)
                .setEventTime(eventTime)
                .setEventTimee(eventTimee)
                .setLocation(location)
                .setTotalParticipants(totalParticipants)
                .setRemarks(remarks)
                .setLabels(labels)
                .setLocationDetail(locationDetail)
                .setPhoneNumber(phoneNumber)
                .setType(type)
                .setLimits(limits)
                .setVisibility(visibility)
                .setLevel(level)
                .setFeeMode(feeMode)
                .setFee(fee)
                .setPenalty(penalty)
                .setLatitude(latitude)
                .setLongitude(longitude);
    }

    public detailEvent toDetailEvent() {
        return new detailEvent()
                .setId(id)
                .setAppId(appId)
                .setAvatar(avatar)
                .setName(name)
                .setEventDate(eventDate)
                .setEventTime(eventTime)
                .setEventTimee(eventTimee)
                .setLocation(location)
                .setLocationDetail(locationDetail)
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setParticipants(participants)
                .setTotalParticipants(totalParticipants)
                .setPhoneNumber(phoneNumber)
                .setType(type)
                .setLimits(limits)
                .setLevel(level)
                .setFeeMode(feeMode)
                .setState(state)
                .setFee(fee)
                .setRemarks(remarks);
    }
}
