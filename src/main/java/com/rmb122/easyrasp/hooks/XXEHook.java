package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.SecurityException;
import com.rmb122.easyrasp.annotation.HookHandler;
import com.rmb122.easyrasp.enums.HookType;

public class XXEHook {
    @HookHandler(hookClass = "com.sun.org.apache.xerces.internal.impl.XMLEntityManager", hookMethod = "expandSystemId", hookType = HookType.AFTER_RUN)
    public static String xxeHook(String ret) throws SecurityException {
        if (ret != null) {
            throw new SecurityException(String.format("External object %s not allowed.", ret));
        }
        return ret;
    }
}
