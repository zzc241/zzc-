package com.atguigu.daijia.customer.service;

import org.springframework.web.bind.annotation.RequestBody;

import com.atguigu.daijia.model.form.customer.ExpectOrderForm;
import com.atguigu.daijia.model.vo.customer.ExpectOrderVo;

public interface OrderService {

    public ExpectOrderVo expectOrder(ExpectOrderForm expectOrderForm);

}
