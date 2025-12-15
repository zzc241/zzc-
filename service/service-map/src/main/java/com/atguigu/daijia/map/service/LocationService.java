package com.atguigu.daijia.map.service;

import java.util.List;

import com.atguigu.daijia.model.form.map.SearchNearByDriverForm;
import com.atguigu.daijia.model.form.map.UpdateDriverLocationForm;
import com.atguigu.daijia.model.vo.map.NearByDriverVo;

public interface LocationService {

    public Boolean updateDriverLocation(UpdateDriverLocationForm updateDriverLocationForm);

    public Boolean removeDriverLocation(Long driverId);

    List<NearByDriverVo> searchNearByDriver(SearchNearByDriverForm searchNearByDriverForm);

}
