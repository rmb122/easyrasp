package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.SecurityException;
import com.rmb122.easyrasp.annotation.HookHandler;

import java.net.URL;
import java.util.Arrays;

public class RMIHook {
    @HookHandler(hookClass = "sun.rmi.server.LoaderHandler", hookMethod = "lookupLoader")
    public static Object[] rmiHook(Object[] params) throws SecurityException {
        URL[] urls = (URL[]) params[0];
        if (urls.length != 0) {
            throw new SecurityException(String.format("Codebase %s not allowed.", Arrays.toString(urls)));
        }
        return params;
    }
}
