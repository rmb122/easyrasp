package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.annotation.HookHandler;

import java.util.Arrays;
import java.util.Map;

public class ProcessHook {
    @HookHandler(hookClass = "java.lang.ProcessImpl", hookMethod = "<init>")
    public static Object[] processHook(Object self, Object[] objects) throws Exception {
        ClassLoader tomcatClassLoader = Thread.currentThread().getContextClassLoader();
        Object request = RequestHook.getRequest();
        if (request == null) {
            RequestHook.releaseRequest();
            return objects; // 不是来自 Springboot
        }
        String prog = new String((byte[]) objects[0]);
        prog = prog.substring(0, prog.length() - 1); // 干掉最后一个 \0
        String args = (new String((byte[]) objects[1])).replace('\0', ' ');
        if (args.length() >= 1) {
            args = args.substring(0, args.length() - 1); // 干掉最后一个 ' '
        }

        String cmd = prog + " " + args;
        System.out.println(cmd);
        System.out.println(Arrays.toString(cmd.getBytes()));

        Map<String, String[]> params = (Map<String, String[]>) tomcatClassLoader.loadClass("javax.servlet.http.HttpServletRequest").getMethod("getParameterMap").invoke(request);
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            for (String value : values) {
                value = value.replaceAll("[ ]+", " "); // 将多个空格换成一个
                value = value.replace("\n", "");
                value = value.replace("\t", "");
                value = value.trim();

                System.out.println(key + " : " + value);
                System.out.println(Arrays.toString(value.getBytes()));
                if (value.equals(cmd)) { // 执行请求中参数一样都指令, 用无害指令替换掉这个指令
                    objects[0] = "echo\0".getBytes();
                    objects[1] = "RASP banned\0".getBytes();
                    objects[2] = 1;
                }
            }
        }

        RequestHook.releaseRequest();
        return objects;
    }
}
