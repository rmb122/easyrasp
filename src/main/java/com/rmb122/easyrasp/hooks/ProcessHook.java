package com.rmb122.easyrasp.hooks;

import com.rmb122.easyrasp.annotation.HookHandler;

import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

public class ProcessHook {
    @HookHandler(hookClass = "java.lang.ProcessImpl", hookMethod = "<init>")
    public static Object[] processHook(Object self, Object[] objects) throws Exception {
        ClassLoader tomcatClassLoader = Thread.currentThread().getContextClassLoader();
        Object request = RequestHook.getRequest();
        if (request == null) {
            return objects; // 不是来自 Springboot
        }

        String prog = new String((byte[]) objects[0]);
        String cmd = null;
        prog = prog.substring(0, prog.length() - 1); // 干掉最后一个 \0
        String args = (new String((byte[]) objects[1])).replace('\0', ' ');
        if (args.length() >= 1) {
            args = args.substring(0, args.length() - 1); // 干掉最后一个 ' '
            cmd = prog + " " + args;
        } else {
            cmd = prog;
        }

        System.out.println(cmd);
        System.out.println(Arrays.toString(cmd.getBytes()));

        Map<String, String[]> params = (Map<String, String[]>) tomcatClassLoader.loadClass("javax.servlet.http.HttpServletRequest").getMethod("getParameterMap").invoke(request);
        for (String key : params.keySet()) {
            String[] values = params.get(key);
            for (String value : values) {
                StringTokenizer stringTokenizer = new StringTokenizer(value);
                StringBuilder stringBuilder = new StringBuilder();

                while (stringTokenizer.hasMoreTokens()) {
                    stringBuilder.append(stringTokenizer.nextToken());
                    stringBuilder.append(" ");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);

                value = stringBuilder.toString();
                System.out.println(key + " : " + value);
                System.out.println(Arrays.toString(value.getBytes()));
                if (value.equals(cmd)) { // 执行请求中参数一样都指令, 用无害指令替换掉这个指令
                    objects[0] = "echo\0".getBytes();
                    objects[1] = "RASP banned\0".getBytes();
                    objects[2] = 1;
                }
            }
        }
        return objects;
    }
}
