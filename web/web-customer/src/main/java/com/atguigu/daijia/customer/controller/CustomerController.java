package com.atguigu.daijia.customer.controller;


import com.atguigu.daijia.common.login.zzcLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.common.util.AuthContextHolder;
import com.atguigu.daijia.customer.service.CustomerService;
import com.atguigu.daijia.customer.service.OrderService;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import com.atguigu.daijia.model.vo.driver.DriverInfoVo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "客户API接口管理")
@RestController
@RequestMapping("/customer")
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerController {

    @Autowired
    private CustomerService customerInfoService;
    @Autowired
    private OrderService orderService;

    @Operation(summary = "小程序授权登录")
    @GetMapping("/login/{code}")
    public Result<String> wxLogin(@PathVariable String code) {
    return Result.ok(customerInfoService.login(code));
    }

    // @Operation(summary = "获取客户登录信息")
    // @GetMapping("/getCustomerLoginInfo")
    // public Result<CustomerLoginVo> getCustomerLoginInfo(HttpServletRequest request) {
    //     String token = request.getHeader("token");

    //     CustomerLoginVo customerLoginVo = customerInfoService.getCustomerLoginInfo(token);

    //     return Result.ok(customerLoginVo);
    // }
    @Operation(summary = "获取客户登录信息")
    @zzcLogin
    @GetMapping("/getCustomerLoginInfo")
    public Result<CustomerLoginVo> getCustomerLoginInfo() {
        Long customerId = AuthContextHolder.getUserId();

        CustomerLoginVo customerLoginVo = customerInfoService.getCustomerLoginInfo(customerId);

        return Result.ok(customerLoginVo);
    }

    @Operation(summary = "更新用户微信手机号")
    @zzcLogin
    @PostMapping("/updateWxPhone")
    public Result updateWxPhone(@RequestBody UpdateWxPhoneForm updateWxPhoneForm) {
    updateWxPhoneForm.setCustomerId(AuthContextHolder.getUserId());
    //customerInfoService.updateWxPhoneNumber(updateWxPhoneForm);
    return Result.ok(true);
    }

    @Operation(summary = "根据订单id获取司机基本信息")
    @zzcLogin
    @GetMapping("/getDriverInfo/{orderId}")
    public Result<DriverInfoVo> getDriverInfo(@PathVariable Long orderId) {
        Long customerId = AuthContextHolder.getUserId();
        return Result.ok(orderService.getDriverInfo(orderId, customerId));
    }
}

