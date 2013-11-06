package com.aplana.timesheet.system.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class ResponceBodyExceptionAspect {

    private static final Logger logger = LoggerFactory.getLogger(ResponceBodyExceptionAspect.class);

    @Pointcut("@annotation(org.springframework.web.bind.annotation.ResponseBody)")
    public void aroundResponseBody() {
    }

    @Around("aroundResponseBody()")
    public Object profile(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (Exception e) {
            logger.error("Error in @ResponceBody method", e);
            return null;
        }
    }


}
