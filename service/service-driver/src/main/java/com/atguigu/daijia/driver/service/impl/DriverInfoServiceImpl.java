package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.constant.SystemConstant;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.config.TencentCloudProperties;
import com.atguigu.daijia.driver.mapper.DriverAccountMapper;
import com.atguigu.daijia.driver.mapper.DriverInfoMapper;
import com.atguigu.daijia.driver.mapper.DriverLoginLogMapper;
import com.atguigu.daijia.driver.mapper.DriverSetMapper;
import com.atguigu.daijia.driver.service.DriverInfoService;
import com.atguigu.daijia.model.entity.driver.DriverAccount;
import com.atguigu.daijia.model.entity.driver.DriverInfo;
import com.atguigu.daijia.model.entity.driver.DriverLoginLog;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.driver.DriverFaceModelForm;
import com.atguigu.daijia.model.form.driver.UpdateDriverAuthInfoForm;
import com.atguigu.daijia.model.vo.driver.DriverAuthInfoVo;
import com.atguigu.daijia.model.vo.driver.DriverLoginVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tencentcloudapi.iai.v20180301.models.CreatePersonResponse;
import com.tencentcloudapi.common.AbstractModel;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.iai.v20180301.IaiClient;
import com.tencentcloudapi.iai.v20180301.models.CreatePersonRequest;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class DriverInfoServiceImpl extends ServiceImpl<DriverInfoMapper, DriverInfo> implements DriverInfoService {

    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private DriverInfoMapper driverInfoMapper;
    @Autowired
    private DriverSetMapper driverSetMapper;
    @Autowired
    private DriverAccountMapper driverAccountMapper;
    @Autowired
    private DriverLoginLogMapper driverLoginLogMapper;
    @Autowired
    private CosServiceImpl cosService;
    @Autowired
    private TencentCloudProperties tencentCloudProperties;
    @Override
    @Transactional
    public Long login(String code) {
        String openId = null;
        try {
            //获取openId
            WxMaJscode2SessionResult sessionInfo = wxMaService.getUserService().getSessionInfo(code);
            openId = sessionInfo.getOpenid();
            log.info("【小程序授权】openId={}", openId);
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        LambdaQueryWrapper<DriverInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DriverInfo::getWxOpenId, openId);
        DriverInfo driverInfo = driverInfoMapper.selectOne(wrapper);
        if(driverInfo == null) {
            driverInfo = new DriverInfo();
            driverInfo.setNickname(String.valueOf(System.currentTimeMillis()));
            driverInfo.setAvatarUrl("https://oss.aliyuncs.com/aliyun_id_photo_bucket/default_handsome.jpg");
            driverInfo.setWxOpenId(openId);
            driverInfoMapper.insert(driverInfo);

            DriverSet driverSet = new DriverSet();
            driverSet.setDriverId(driverInfo.getId());
            driverSet.setOrderDistance(new BigDecimal(0));//0：无限制
            driverSet.setAcceptDistance(new BigDecimal(SystemConstant.ACCEPT_DISTANCE));//默认接单范围：5公里
            driverSet.setIsAutoAccept(0);//0：否 1：是
            driverSetMapper.insert(driverSet);

            DriverAccount driverAccount = new DriverAccount();
            driverAccount.setDriverId(driverInfo.getId());
            driverAccountMapper.insert(driverAccount);

            
        }
        DriverLoginLog driverLoginLog = new DriverLoginLog();
        driverLoginLog.setDriverId(driverInfo.getId());
        driverLoginLog.setMsg("小程序登录");
        driverLoginLogMapper.insert(driverLoginLog);
        return driverInfo.getId();
        
    }


    @Override
    @Transactional
    public DriverLoginVo getDriverLoginInfo(Long driverId){
        DriverLoginVo driverLoginVo = new DriverLoginVo();

        DriverInfo driverInfo = driverInfoMapper.selectById(driverId);
        BeanUtils.copyProperties(driverInfo, driverLoginVo);

        String FaceModelId = driverInfo.getFaceModelId();
        Boolean isArchiveFace = StringUtils.hasText(FaceModelId);
        driverLoginVo.setIsArchiveFace(isArchiveFace);
        return driverLoginVo;
    }

    

    @Override
    @Transactional
    public DriverAuthInfoVo getDriverAuthInfo(Long driverId){
        DriverAuthInfoVo driverAuthInfoVo = new DriverAuthInfoVo();
        log.info("在getDriverAuthInfo获取司机认证信息，driverId={}", driverId);

        // DriverInfo driverInfo = this.getById(driverId);
        DriverInfo driverInfo = driverInfoMapper.selectById(driverId);
        
        BeanUtils.copyProperties(driverInfo, driverAuthInfoVo);

        driverAuthInfoVo.setIdcardBackUrl(cosService.getImageURL(driverAuthInfoVo.getIdcardBackUrl()));
        driverAuthInfoVo.setIdcardFrontShowUrl(cosService.getImageURL(driverAuthInfoVo.getIdcardFrontUrl()));
        driverAuthInfoVo.setIdcardHandShowUrl(cosService.getImageURL(driverAuthInfoVo.getIdcardHandUrl()));
        driverAuthInfoVo.setDriverLicenseFrontShowUrl(cosService.getImageURL(driverAuthInfoVo.getDriverLicenseFrontUrl()));
        driverAuthInfoVo.setDriverLicenseBackShowUrl(cosService.getImageURL(driverAuthInfoVo.getDriverLicenseBackUrl()));
        driverAuthInfoVo.setDriverLicenseHandShowUrl(cosService.getImageURL(driverAuthInfoVo.getDriverLicenseHandUrl()));
        return driverAuthInfoVo;
    }


    // public Boolean updateDriverAuthInfo( UpdateDriverAuthInfoForm updateDriverAuthInfoForm){
    //     Long driverId = updateDriverAuthInfoForm.getDriverId();
        
    //     DriverInfo driverInfo = new DriverInfo();
    //     BeanUtils.copyProperties(updateDriverAuthInfoForm, driverInfo);
    //     driverInfo.setId(driverId);
    //     // int success = driverInfoMapper.updateById(driverInfo);
    //     Boolean isUpdate = this.updateById(driverInfo);
    //     return isUpdate;
    // }


    @Override
    @Transactional
    public Boolean updateDriverAuthInfo(UpdateDriverAuthInfoForm updateDriverAuthInfoForm) {
        //获取司机id
        Long driverId = updateDriverAuthInfoForm.getDriverId();

        //修改操作
        DriverInfo driverInfo = new DriverInfo();
        driverInfo.setId(driverId);
        BeanUtils.copyProperties(updateDriverAuthInfoForm,driverInfo);

//        int i = driverInfoMapper.updateById(driverInfo);
        boolean update = this.updateById(driverInfo);
        return update;
    }

    @Override
    @Transactional
    public Boolean createDriverFaceModel(DriverFaceModelForm driverFaceModelForm) { 
        Long driverId = driverFaceModelForm.getDriverId();
        DriverInfo driverInfo = driverInfoMapper.selectById(driverId);

        try{
            Credential cred = new Credential(tencentCloudProperties.getSecretId(), tencentCloudProperties.getSecretKey());
            
            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint("iai.tencentcloudapi.com");
            // 实例化一个client选项，可选的，没有特殊需求可以跳过
            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);
            // 实例化要请求产品的client对象,clientProfile是可选的
            IaiClient client = new IaiClient(cred, tencentCloudProperties.getRegion(), clientProfile);
            // 实例化一个请求对象,每个接口都会对应一个request对象
            CreatePersonRequest req = new CreatePersonRequest();
            //设置相关值
            req.setGroupId(tencentCloudProperties.getPersonGroupId());
            //基本信息
            req.setPersonId(String.valueOf(driverInfo.getId()));
            req.setGender(Long.parseLong(driverInfo.getGender()));
            req.setQualityControl(4L);
            req.setUniquePersonControl(4L);
            req.setPersonName(driverInfo.getName());
            req.setImage(driverFaceModelForm.getImageBase64());
            
            // 返回的resp是一个CreatePersonResponse的实例，与请求对象对应
            CreatePersonResponse resp = client.CreatePerson(req);
            String faceModelId = resp.getFaceId();
            if(StringUtils.hasText(faceModelId)){
                driverInfo.setFaceModelId(faceModelId);
                driverInfoMapper.updateById(driverInfo);
            }

        } catch (TencentCloudSDKException e) {
            System.out.println(e.toString());
        }
        return true;

    }


}