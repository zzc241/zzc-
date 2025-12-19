package com.atguigu.daijia.driver.service.impl;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.driver.client.DriverInfoFeignClient;
import com.atguigu.daijia.driver.service.LocationService;
import com.atguigu.daijia.map.client.LocationFeignClient;
import com.atguigu.daijia.model.entity.driver.DriverSet;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import com.atguigu.daijia.model.form.map.UpdateOrderLocationForm;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
// @SuppressWarnings({"unchecked", "rawtypes"})
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationFeignClient locationFeignClient;
    @Autowired
    private DriverInfoFeignClient driverFeignClient;
    
    //更新司机位置
    // @Override
    // public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
    //     Result<Boolean> booleanResult = locationFeignClient.updateDriverLocation(updateDriverLocationForm);
    //     return booleanResult.getData();
    // }
    @Override
    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm) {
        Long driverId = updateDriverLocationForm.getDriverId();
        Result<DriverSet> booleanResult = driverFeignClient.getDriverSet(driverId);
        DriverSet driverSet = booleanResult.getData();
        if(driverSet.getServiceStatus() == 1){
            Result<Boolean> result = locationFeignClient.updateDriverLocation(updateDriverLocationForm);
            return result.getData();
        }
        else{
            throw new RuntimeException("司机未开启接单服务");
        }
    }

    @Override
    public Boolean updateOrderLocationToCache(UpdateOrderLocationForm updateOrderLocationForm) {
        return locationFeignClient.updateOrderLocationToCache(updateOrderLocationForm).getData();
    }


}
