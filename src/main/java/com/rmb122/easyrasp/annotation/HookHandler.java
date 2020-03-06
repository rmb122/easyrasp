package com.rmb122.easyrasp.annotation;

import com.rmb122.easyrasp.enums.HookType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface HookHandler {
    String hookClass();
    String hookMethod();
    String methodDesc() default "" ;
    HookType hookType() default HookType.BEFORE_RUN;
}
