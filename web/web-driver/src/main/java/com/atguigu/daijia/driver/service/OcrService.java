package com.atguigu.daijia.driver.service;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.driver.DriverLicenseOcrVo;
import com.atguigu.daijia.model.vo.driver.IdCardOcrVo;

import io.swagger.v3.oas.annotations.Operation;

public interface OcrService {

    public IdCardOcrVo idCardOcr(MultipartFile file);

    public DriverLicenseOcrVo driverLicenseOcr(MultipartFile file);
}
