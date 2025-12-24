package com.atguigu.daijia.driver.controller;

import com.atguigu.daijia.common.login.zzcLogin;
import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.driver.service.FileService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "上传管理接口")
@RestController
@RequestMapping("/file")
public class FileController {
    // @Autowired
    // private CosService cosService;
    @Autowired
    private FileService fileService;
	// @Operation(summary = "上传")
    // // @zzcLogin
    // @PostMapping(value="/upload")
    // public Result<String> upload(@RequestPart ("file") MultipartFile file , 
    // @RequestParam (name = "path" , defaultValue = "auth")String path) {
    //     CosUploadVo cosUploadVo =  cosService.upload(file, path);
    //     String showURL = cosUploadVo.getShowUrl();
    //     return Result.ok(showURL);
    // }

    @Operation(summary = "上传")
    @PostMapping("/upload")
    public Result<String> upload(@RequestPart("file") MultipartFile file) {
        String url = fileService.upload(file);
        return Result.ok(url);
    }

}
