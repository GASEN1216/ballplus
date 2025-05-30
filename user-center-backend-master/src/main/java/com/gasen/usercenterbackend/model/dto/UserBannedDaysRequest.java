package com.gasen.usercenterbackend.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * 用户封禁
 * */
@Data
public class UserBannedDaysRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = 1533989921684895724L;
    /**
     * 封禁用户id
     * */
    private long id;

    /**
     * 封禁天数days
     * */
    private int days;


}
