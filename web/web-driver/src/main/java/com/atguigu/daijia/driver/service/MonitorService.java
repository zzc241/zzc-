package com.atguigu.daijia.driver.service;

import org.springframework.web.multipart.MultipartFile;

import com.atguigu.daijia.model.form.order.OrderMonitorForm;

public interface MonitorService {
    Boolean upload(MultipartFile file, OrderMonitorForm orderMonitorForm);

}
