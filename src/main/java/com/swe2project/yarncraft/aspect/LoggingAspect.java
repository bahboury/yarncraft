package com.swe2project.yarncraft.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class LoggingAspect {
    // "Around" advice wraps the method execution
    // This pattern matches ANY method inside ANY class in the 'modules' package
    @Around("execution(* com.swe2project.yarncraft.modules..*(..))")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();

        try {
            // Proceed means: "Go ahead and run the actual function now"
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - start;

            // Log success
            log.info("Executed {}.{}() in {} ms", className, methodName, executionTime);
            return result;

        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - start;

            // Log failure
            log.error("Failed {}.{}() after {} ms with error: {}", className, methodName, executionTime, ex.getMessage());
            throw ex; // Re-throw the error so the ExceptionHandler can catch it
        }
    }
}
