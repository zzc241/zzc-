package com.atguigu.daijia.common.login;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.atguigu.daijia.common.constant.RedisConstant;
import com.atguigu.daijia.common.util.AuthContextHolder;

import jakarta.servlet.http.HttpServletRequest;

@Component
@Aspect
public class zzcLoginAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @Around("execution(* com.atguigu.daijia.*.controller.*.*(..)) && @annotation(zzclogin)")
    public Object login(ProceedingJoinPoint proceedingJoinPoint , zzcLogin zzclogin) throws Throwable {
        System.out.println("登录切面");

        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();

        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;

        if(sra != null){
            HttpServletRequest request = sra.getRequest();
            String token = request.getHeader("token");
            if(token == null) {
                return "请登录";
            }
            System.out.println("token:" + token);
            String customerId =(String) redisTemplate.opsForValue().get(RedisConstant.USER_LOGIN_KEY_PREFIX+token);

            if(StringUtils.hasText(customerId)){
                AuthContextHolder.setUserId(Long.parseLong(customerId));
            }
        }
        return proceedingJoinPoint.proceed();
    }
    
}
