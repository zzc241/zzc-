package com.atguigu.daijia.driver.service.impl;

import org.springframework.util.StringUtils;

import com.atguigu.daijia.common.execption.GuiguException;
import com.atguigu.daijia.common.result.ResultCodeEnum;
import com.atguigu.daijia.driver.config.TencentCloudProperties;
import com.atguigu.daijia.driver.service.CiService;
import com.atguigu.daijia.driver.service.CosService;
import com.atguigu.daijia.model.vo.driver.CosUploadVo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.StorageClass;
import com.qcloud.cos.region.Region;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@SuppressWarnings({"unchecked", "rawtypes"})
public class CosServiceImpl implements CosService {
    @Autowired
    private TencentCloudProperties tencentCloudProperties;
    @Autowired
    private CiService ciService;

    @Override
    public CosUploadVo upload(MultipartFile file, String path) {
        // // 1 初始化用户身份信息（secretId, secretKey）。
        // String secretId = tencentCloudProperties.getSecretId();
        // String secretKey = tencentCloudProperties.getSecretKey();
        // COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // // 2 设置 bucket 的地域
        // // clientConfig 中包含了设置 region, https(默认 http), 超时, 代理等 set 方法, 使用可参见源码或者常见问题 Java SDK 部分。
        // Region region = new Region(tencentCloudProperties.getRegion());
        // ClientConfig clientConfig = new ClientConfig(region);
        // // 这里建议设置使用 https 协议
        // // 从 5.6.54 版本开始，默认使用了 https
        // clientConfig.setHttpProtocol(HttpProtocol.https);
        // // 3 生成 cos 客户端。
        COSClient cosClient = getCosClient();

        //文件上传
        //元数据信息
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        meta.setContentEncoding("UTF-8");
        meta.setContentType(file.getContentType());

        //向存储桶中保存文件
        String fileType = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")); //文件后缀名
        String uploadPath = "/driver/" + path + "/" + UUID.randomUUID().toString().replaceAll("-", "") + fileType;
        // 01.jpg
        PutObjectRequest putObjectRequest = null;
        try {
            //1 bucket名称
            putObjectRequest = new PutObjectRequest(tencentCloudProperties.getBucketPrivate(),
                    uploadPath,
                    file.getInputStream(),
                    meta);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        putObjectRequest.setStorageClass(StorageClass.Standard);
        // PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest); //上传文件
        cosClient.shutdown();

        //图片审核
        Boolean imageAuditing = ciService.imageAuditing(uploadPath);
        if(!imageAuditing) {
            //删除违规图片
            cosClient.deleteObject(tencentCloudProperties.getBucketPrivate(),uploadPath);
            throw new GuiguException(ResultCodeEnum.IMAGE_AUDITION_FAIL);
        }

        CosUploadVo cosUploadVo = new CosUploadVo();
        cosUploadVo.setUrl(uploadPath);
        cosUploadVo.setShowUrl(getImageURL(uploadPath));

        return cosUploadVo;
    }

    public COSClient getCosClient() {
        // 1 获取用户身份信息
        String secretId = tencentCloudProperties.getSecretId();
        String secretKey = tencentCloudProperties.getSecretKey();
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // 2 设置 bucket 的地域
        Region region = new Region(tencentCloudProperties.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        return new COSClient(cred, clientConfig);
    }

    @Override
    public String getImageURL(String path) {
        if(!StringUtils.hasText(path)) {
            return "";
        }

        COSClient cosClient = getCosClient();

        GeneratePresignedUrlRequest request =
            new GeneratePresignedUrlRequest(tencentCloudProperties.getBucketPrivate(),
                    path, HttpMethodName.GET);

        Date date = new DateTime().plusDays(1).toDate();
        request.setExpiration(date);
        URL url = cosClient.generatePresignedUrl(request);

        cosClient.shutdown();
        return url.toString();
    }
        


}
