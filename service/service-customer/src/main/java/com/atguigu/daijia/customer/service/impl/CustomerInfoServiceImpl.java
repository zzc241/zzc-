package com.atguigu.daijia.customer.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.daijia.customer.mapper.CustomerInfoMapper;
import com.atguigu.daijia.customer.mapper.CustomerLoginLogMapper;
import com.atguigu.daijia.customer.service.CustomerInfoService;
import com.atguigu.daijia.model.entity.customer.CustomerInfo;
import com.atguigu.daijia.model.entity.customer.CustomerLoginLog;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CustomerInfoServiceImpl extends ServiceImpl<CustomerInfoMapper, CustomerInfo> implements CustomerInfoService {

    @Autowired
    private CustomerInfoMapper customerInfoMapper;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private CustomerLoginLogMapper customerLoginLogMapper;

    @Override
    public Long login(String code){
        String openId = null;

        //获取code值获取微信openId
        WxMaJscode2SessionResult sessionInfo;
        try {
            sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
        } catch (WxErrorException e) {
            e.printStackTrace();
        }
        
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfo::getWxOpenId, openId);
        CustomerInfo customerInfo = customerInfoMapper.selectOne(wrapper);

        if(customerInfo == null) {
            customerInfo = new CustomerInfo();
            customerInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            customerInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            customerInfo.setWxOpenId(openId);
            customerInfoMapper.insert(customerInfo);
        }

        CustomerLoginLog customerLoginLog = new CustomerLoginLog();
        customerLoginLog.setCustomerId(customerInfo.getId());
        customerLoginLog.setMsg("小程序登录");
        customerLoginLogMapper.insert(customerLoginLog);

        return customerInfo.getId();
    }


    @Override
    public CustomerLoginVo getCustomerLoginInfo(Long customerId) {
        CustomerInfo customerinfo = new CustomerInfo();
        customerinfo = customerInfoMapper.selectById(customerId);
        CustomerLoginVo customerLoginVo = new CustomerLoginVo();
        // customerLoginVo.setWxOpenId(customerinfo.getWxOpenId());
        // customerLoginVo.setNickname(customerinfo.getNickname());
        // customerLoginVo.setGender(customerinfo.getGender());
        // customerLoginVo.setAvatarUrl(customerinfo.getAvatarUrl());
        // customerLoginVo.setIsBindPhone(customerinfo.getPhone() != null);

        
        BeanUtils.copyProperties(customerinfo, customerLoginVo);

        // if(customerinfo.getPhone() != null){
        //     customerLoginVo.setIsBindPhone(true);
        // }
        boolean hastext = StringUtils.hasText(customerinfo.getPhone());
        customerLoginVo.setIsBindPhone(hastext);
        return customerLoginVo;
    }


    @SneakyThrows
    @Transactional(rollbackFor = {Exception.class})
    @Override
    public Boolean updateWxPhoneNumber(UpdateWxPhoneForm updateWxPhoneForm) {
    // 调用微信 API 获取用户的手机号
    WxMaPhoneNumberInfo phoneInfo = wxMaService.getUserService().getPhoneNoInfo(updateWxPhoneForm.getCode());
    String phoneNumber = phoneInfo.getPhoneNumber();
    log.info("phoneInfo:{}", JSON.toJSONString(phoneInfo));

    CustomerInfo customerInfo = new CustomerInfo();
    customerInfo.setId(updateWxPhoneForm.getCustomerId());
    customerInfo.setPhone(phoneNumber);
    return this.updateById(customerInfo);

    }


    @Override
    public String getCustomerOpenId(Long customerId) {
        LambdaQueryWrapper<CustomerInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CustomerInfo::getId, customerId);
        
    CustomerInfo customerInfo = this.getOne(wrapper.select(CustomerInfo::getWxOpenId));
    return customerInfo.getWxOpenId();
    }

}
