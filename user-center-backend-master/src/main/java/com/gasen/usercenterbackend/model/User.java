package com.gasen.usercenterbackend.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.io.Serializable;
import java.util.Random;

import com.gasen.usercenterbackend.model.respond.wxUser;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户信息
 * </p>
 *
 * @author gasen
 * @since 2024-02-22
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("user")
public class User implements Serializable {

    @Serial
    @TableField(exist=false)
    private static final long serialVersionUID = 1L;

    /**
     * 用户id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 微信开放平台id
     */
    private String openId;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 账户名
     */
    private String userAccount;

    /**
     * 密码
     */
    private String password;

    /**
     * 头像url
     */
    private String avatarUrl;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 手机号码
     */
    private String phone;

    /**
     * 等级
     */
    private Integer grade;

    /**
     * 经验
     */
    private Integer exp;

    /**
     * 上一次签到时间
     */
    private LocalDateTime signIn;

    /**
     * 用户状态（-1冻结，0正常，1管理员）
     */
    private Integer state;

    /**
     * 解封时间
     */
    private LocalDateTime unblockingTime;

    /**
     * 逻辑删除
     */
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

    public User(String userAccount, String password) {
        this.userAccount = userAccount;
        this.password = password;
    }

    // 将wxUser的内容填充到User中
    public static User wxUserToUser(wxUser wxUser) {
        return new User()
                .setId(wxUser.getId())
                .setUserAccount(wxUser.getUserAccount())
                .setAvatarUrl(wxUser.getAvatarUrl())
                .setGender(wxUser.getGender())
                .setBirthday(wxUser.getBirthday())
                .setDescription(wxUser.getDescription())
                .setLabel(wxUser.getLabel());
    }
}
