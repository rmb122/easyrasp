package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.annotation.HookHandler;

public class RequestHook {
    private static ThreadLocal<Object> currRequest = new ThreadLocal<>();

    @HookHandler(hookClass = "org.springframework.web.servlet.FrameworkServlet", hookMethod = "service")
    public static Object[] requestHook(Object self, Object[] params) {
        currRequest.set(params[0]);
        return params;
    }

    public static Object getRequest() {
        return currRequest.get();
    }

    public static void releaseRequest() {
        currRequest.remove();
    }
}
