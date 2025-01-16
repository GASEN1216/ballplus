package com.gasen.usercenterbackend.controller;

import com.gasen.usercenterbackend.common.BaseResponse;
import com.gasen.usercenterbackend.common.ResultUtils;
import com.gasen.usercenterbackend.config.GdMapConfig;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/user/wx")
public class MapController {

    @Resource
    private GdMapConfig gdMapConfig;

    @Operation(summary = "获取高德地图key" )
    @GetMapping("/getKey")
    public BaseResponse<String> getKey() {
        return ResultUtils.success(gdMapConfig.getKey());
    }
}
