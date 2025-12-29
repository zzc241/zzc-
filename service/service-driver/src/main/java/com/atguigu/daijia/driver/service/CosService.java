package com.atguigu.daijia.driver.service;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;

public interface CosService {

    public CosUploadVo upload(MultipartFile file, String path);

    String getImageURL(String path);


}
