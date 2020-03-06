package com.rmb122.easyrasp;

import com.rmb122.easyrasp.annotation.HookHandler;
import com.rmb122.easyrasp.enums.HookType;

import java.lang.reflect.Method;

public class HookDesc {
    String hookClassName;
    String hookMethodName;
    String hookMethodDesc;
    HookType hookType;
    Method handlerMethod;

    public static HookDesc fromHookHandler(HookHandler hookHandler, Method method) {
        HookDesc hookDesc = new HookDesc();
        hookDesc.hookClassName = hookHandler.hookClass();
        hookDesc.hookMethodName = hookHandler.hookMethod();
        hookDesc.hookMethodDesc = hookHandler.methodDesc();
        hookDesc.hookType = hookHandler.hookType();
        hookDesc.handlerMethod = method;
        return hookDesc;
    }
}
