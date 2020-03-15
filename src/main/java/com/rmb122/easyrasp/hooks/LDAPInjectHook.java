package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.SecurityException;
import com.rmb122.easyrasp.annotation.HookHandler;

import javax.naming.Reference;

public class LDAPInjectHook {
    @HookHandler(hookClass = "javax.naming.spi.NamingManager", hookMethod = "getObjectFactoryFromReference")
    public static Object[] ldapInjectHook(Object[] params) throws SecurityException {
        Reference reference = (Reference) params[0];
        String factoryClassLocation  = reference.getFactoryClassLocation();
        String className = (String) params[1];

        if (className.equals("org.apache.naming.factory.BeanFactory")) {
            throw new SecurityException("Can't set factory class to org.apache.naming.factory.BeanFactory");
        }
        if (factoryClassLocation != null && factoryClassLocation.startsWith("http")) {
            throw new SecurityException("Can't set factory class location to remote addr");
        }
        return params;
    }
}
