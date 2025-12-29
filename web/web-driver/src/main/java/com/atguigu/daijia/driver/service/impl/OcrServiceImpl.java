package com.atguigu.daijia.driver.service.impl;


import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.client.OcrFeignClient;
import com.atguigu.daijia.driver.service.OcrService;
import com.atguigu.daijia.model.vo.driver.DriverLicenseOcrVo;
import com.atguigu.daijia.model.vo.driver.IdCardOcrVo;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class OcrServiceImpl implements OcrService {
    @Autowired
    private OcrFeignClient client;

    @Override
    public IdCardOcrVo idCardOcr(MultipartFile file) {

        Result<IdCardOcrVo> idCardOcrVo =  client.idCardOcr(file);
        IdCardOcrVo data = idCardOcrVo.getData();
        return data;
    }

    @Override
    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file){

        Result<DriverLicenseOcrVo> driverLicenseOcrVo = client.driverLicenseOcr(file);
        DriverLicenseOcrVo data = driverLicenseOcrVo.getData();
        return data;
    }


}
