package com.gasen.usercenterbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gasen.usercenterbackend.common.ErrorCode;
import com.gasen.usercenterbackend.exception.BusinessExcetion;
import com.gasen.usercenterbackend.mapper.ItemsMapper;
import com.gasen.usercenterbackend.model.dao.Items;
import com.gasen.usercenterbackend.service.IItemsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaQuery;
import static com.baomidou.mybatisplus.core.toolkit.Wrappers.lambdaUpdate;

@Service
@Slf4j
public class ItemServiceImpl implements IItemsService {

    @Resource
    private ItemsMapper itemsMapper;


    @Override
    public void addItem(Long userId, Integer itemId) {
        if(userId == null || itemId == null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "userId或itemId参数为空");

        LambdaQueryWrapper<Items> queryWrapper = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<Items> eq = queryWrapper.eq(Items::getUserId, userId).eq(Items::getItemId, itemId);
        if(itemsMapper.selectOne(eq) != null)
            throw new BusinessExcetion(ErrorCode.PARAMETER_ERROR, "该用户已添加该商品");

        itemsMapper.insert(new Items(userId, itemId));
    }

    /**
     * 获取用户物品列表
     * @param userId
     * @return
     */
    @Override
    public List<Integer> getItems(Long userId) {
        List<Items> items = itemsMapper.selectList(new LambdaQueryWrapper<Items>().eq(Items::getUserId, userId));
        return items.stream().map(Items::getItemId).toList();
    }
}
