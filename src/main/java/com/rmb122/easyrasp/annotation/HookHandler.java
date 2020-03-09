package com.rmb122.easyrasp.annotation;

import com.rmb122.easyrasp.enums.HookType;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Repeatable(HookHandlers.class)
public @interface HookHandler {
    String hookClass();
    String hookMethod();
    String methodDesc() default "" ;
    HookType hookType() default HookType.BEFORE_RUN;
}
