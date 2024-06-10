package com.atmiao.wechatdemo.controller;

import com.atmiao.wechatdemo.commons.ResponseVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author miao
 * @version 1.0
 */
@RestController
@CrossOrigin
@Slf4j
@Tag(name = "hello",description = "这是一个测试接口")
@RequestMapping("test")
public class HelloConrtroller {
    @Operation(summary = "hello",description = "hello的方法")
    @GetMapping("hello")
    public String sayHello(){
        log.info("hh");
        return "hello world";
    }
    @GetMapping("response")
    public ResponseVo<Object> testResponse(){
        return ResponseVo.getSuccessResponseVo(null);
    }

}
