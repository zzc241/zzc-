package com.atguigu.daijia.customer.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.atguigu.daijia.common.result.Result;
import com.atguigu.daijia.model.form.customer.UpdateWxPhoneForm;
import com.atguigu.daijia.model.vo.customer.CustomerLoginVo;

@FeignClient(value = "service-customer")
public interface CustomerInfoFeignClient {

	@GetMapping("/customer/info/login/{code}")
	public Result<Long> login(@PathVariable String code);


	@GetMapping("/customer/info/getCustomerLoginInfo/{customerId}")
	public Result<CustomerLoginVo> getCustomerLoginInfo(@PathVariable("customerId") Long customerId);

	@PostMapping("/customer/info/updateWxPhoneNumber")
	Result<Boolean> updateWxPhoneNumber(@RequestBody UpdateWxPhoneForm updateWxPhoneForm);

	/**
	 * 获取客户OpenId
	 * @param customerId
	 * @return
	 */
	@GetMapping("/customer/info/getCustomerOpenId/{customerId}")
	Result<String> getCustomerOpenId(@PathVariable("customerId") Long customerId);
}