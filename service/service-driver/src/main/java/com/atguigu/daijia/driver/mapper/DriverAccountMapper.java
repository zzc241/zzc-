package com.atguigu.daijia.driver.mapper;

import com.atguigu.daijia.model.entity.driver.DriverAccount;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import feign.Param;

import java.math.BigDecimal;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DriverAccountMapper extends BaseMapper<DriverAccount> {

    void add(@Param("driverId") Long driverId,@Param("amount") BigDecimal amount);

}
