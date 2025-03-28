package com.gasen.usercenterbackend.model.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("product")
public class Product {
    
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    private String name;
    
    private Integer price;
    
    private String image;
    
    private String type;
    
    private String description;
    
    private Integer status; // 0-下架, 1-上架
    
    private LocalDateTime createTime;
    
    private LocalDateTime updateTime;
} 