package com.atguigu.daijia.driver.controller;

import com.alibaba.nacos.plugin.auth.constant.Constants.Auth;
import com.atguigu.daijia.common.login.zzcLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.DriverService;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Slf4j
@Tag(name = "司机API接口管理")
@RestController
@RequestMapping(value="/driver")
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverController {
    @Autowired
    private DriverService driverInfoService;
	@Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> login(@PathVariable String code) {
        return Result.ok(driverInfoService.login(code));
    }

    @Operation(summary = "获取司机登录信息")
    @zzcLogin
    @GetMapping("/getDriverLoginInfo")
    public Result<DriverLoginVo> getDriverLoginInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverInfoService.getDriverLoginInfo(driverId));
    }


    @Operation(summary = "获取司机认证信息")
    @zzcLogin
    @GetMapping("/getDriverAuthInfo")
    public Result<DriverAuthInfoVo> getDriverAuthInfo() {
        Long driverId = AuthContextHolder.getUserId();
        return Result.ok(driverInfoService.getDriverAuthInfo(driverId));
    }


    // // UpdateDriverAuthInfoForm updateDriverAuthInfoForm
    // @Operation(summary = "更新司机认证信息")
    // // @zzcLogin
    // @PostMapping("/updateDriverAuthInfo")
    // public Result<Boolean> updateDriverAuthInfo(@RequestBody UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
    //     updateDriverAuthInfoForm.setDriverId(AuthContextHolder.getUserId());
    //     log.info("DriverController收到修改司机认证信息请求，driverId={}", updateDriverAuthInfoForm.getDriverId());
    //     log.info("DriverController收到修改司机认证信息请求，updateDriverAuthInfoForm={}", updateDriverAuthInfoForm);
    //     return Result.ok(driverInfoService.updateDriverAuthInfo(updateDriverAuthInfoForm));
    // }


    
    // private final ObjectMapper objectMapper;
    // public DriverController(DriverService driverInfoService, ObjectMapper objectMapper) {
    //     this.driverInfoService = driverInfoService;
    //     this.objectMapper = objectMapper;
    // }
    @Autowired
    private ObjectMapper objectMapper;
    @Operation(summary = "更新司机认证信息")
    @zzcLogin
    @PostMapping("/updateDriverAuthInfo")
    public Result<Boolean> updateDriverAuthInfo(HttpServletRequest request) {
        String rawBody;
        try {
            rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            // 步骤2：手动将JSON转为TestForm（排除@RequestBody的自动绑定问题）
            UpdateDriverAuthInfoForm updateDriverAuthInfoForm = objectMapper.readValue(rawBody, UpdateDriverAuthInfoForm.class);
            updateDriverAuthInfoForm.setDriverId(AuthContextHolder.getUserId());
            log.info("DriverController收到修改司机认证信息请求，updateDriverAuthInfoForm={}", updateDriverAuthInfoForm);
            String token = request.getHeader("token"); // 对应前端传的token: a48f88b40d17471db3c977a5c5dd0a9c
            log.info("请求头中的token：{}", token);
            Boolean isSuccess = driverInfoService.updateDriverAuthInfo(updateDriverAuthInfoForm);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return Result.ok();
    }
    
    @Operation(summary = "上传司机人脸模型")
    @zzcLogin
    @PostMapping("/creatDriverFaceModel")
    // public Result<Boolean> createDriverFaceModel(@RequestBody DriverFaceModelForm driverFaceModelForm){
    //     log.info("DriverController收到上传司机人脸模型请求，driverFaceModelForm={}", driverFaceModelForm);
    //     Boolean isSuccess = driverInfoService.createDriverFaceModel(driverFaceModelForm);
    //     return Result.ok(isSuccess);
    // }
    public Result<Boolean> createDriverFaceModel(HttpServletRequest request) {
        String rawBody;
        try {
            rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
            // 步骤2：手动将JSON转为TestForm（排除@RequestBody的自动绑定问题）
            DriverFaceModelForm driverFaceModelForm = objectMapper.readValue(rawBody, DriverFaceModelForm.class);
            driverFaceModelForm.setDriverId(AuthContextHolder.getUserId());
            log.info("DriverController收到修改司机认证信息请求，driverFaceModelForm={}", driverFaceModelForm);
            // String token = request.getHeader("token"); // 对应前端传的token: a48f88b40d17471db3c977a5c5dd0a9c
            // log.info("请求头中的token：{}", token);
            Boolean isSuccess = driverInfoService.createDriverFaceModel(driverFaceModelForm);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        return Result.ok();
    }

    // @RestController
    // @RequestMapping("/test")
    // public class TestController {

    //     // 注入Jackson的ObjectMapper（用Spring容器内的实例，避免自定义实例的配置差异）
    //     private final ObjectMapper objectMapper;
    //     public TestController(ObjectMapper objectMapper) {
    //         this.objectMapper = objectMapper;
    //     }

        // @PostMapping("/json")
        // public String testJson(HttpServletRequest request) throws IOException {
        //     // 步骤1：读取原始请求体（确认后端真的收到了JSON）
        //     String rawBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);
        //     System.out.println("=== 原始请求体 ===");
        //     System.out.println(rawBody); // 重点看这行输出！

        //     // 步骤2：手动将JSON转为TestForm（排除@RequestBody的自动绑定问题）
        //     TestForm form = objectMapper.readValue(rawBody, TestForm.class);
        //     System.out.println("=== 手动解析结果 ===");
        //     System.out.println("phone: " + form.getPhone() + ", name: " + form.getName());

        //     return "原始请求体：" + rawBody + " | 手动解析：" + form.getPhone() + "," + form.getName();
        // }

        // // 最简实体类（手动写getter/setter，彻底抛弃Lombok，排除Lombok问题）
        // static class TestForm {
        //     private String phone;
        //     private String name;

        //     // 手动写无参构造（必须）
        //     public TestForm() {}

        //     // 手动写getter/setter（确保public）
        //     public String getPhone() { return phone; }
        //     public void setPhone(String phone) { this.phone = phone; }
        //     public String getName() { return name; }
        //     public void setName(String name) { this.name = name; }
        // }
    // }

}

