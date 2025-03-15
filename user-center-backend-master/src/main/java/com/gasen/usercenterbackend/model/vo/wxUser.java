package com.gasen.usercenterbackend.model.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Accessors(chain = true)
public class wxUser implements Serializable {
    /**
     * 用户id
     */
    private Integer id;

    /**
     * 球号
     */
    private String ballNumber;

    /**
     * 昵称
     */
    private String userAccount;

    /**
     * Token
     */
    private String token;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 等级
     */
    private Integer grade;

    /**
     * 经验
     */
    private Integer exp;

    /**
     * 用户状态（-1冻结，0正常，1管理员）
     */
    private Integer state;

    /**
     * 解封时间
     */
    private LocalDateTime unblockingTime;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 信誉值
     */
    private Integer credit;

    /**
     * 积分
     */
    private Integer score;

    /**
     * 个性签名
     */
    private String description;

    /**
     * 标签
     */
    private String label;

    private String phone;

}
