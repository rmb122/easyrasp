package com.rmb122.easyrasp;

import com.rmb122.easyrasp.annotation.HookHandler;
import javassist.*;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.*;

public class Transformer implements ClassFileTransformer {
    private static Logger logger = LoggerFactory.getLogger(Transformer.class);

    private static Transformer transformer = new Transformer();
    private static HashMap<String, List<HookDesc>> handlers = getAllHandler();

    public static Transformer getInstance() {
        return transformer;
    }

    public static HashMap<String, List<HookDesc>> getAllHandler() {
        HashMap<String, List<HookDesc>> handlers = new HashMap<>();
        Reflections reflections = new Reflections("com.rmb122.easyrasp", new MethodAnnotationsScanner());
        Set<Method> methodsWithAnnotation = reflections.getMethodsAnnotatedWith(HookHandler.class);

        for (Method method : methodsWithAnnotation) {
            if (Modifier.isStatic(method.getModifiers())) {
                HookHandler hookHandler = method.getAnnotation(HookHandler.class);
                HookDesc hookDesc = HookDesc.fromHookHandler(hookHandler, method);
                logger.info("Arm hook with " + hookDesc.handlerMethod.getName() + " in " + hookDesc.hookClassName + "." + hookDesc.hookMethodName);

                List<HookDesc> hookDescs = handlers.get(hookDesc.hookClassName);
                if (hookDescs == null) {
                    hookDescs = new ArrayList<>();
                    handlers.put(hookDesc.hookClassName, hookDescs);
                }
                hookDescs.add(hookDesc);
            } else {
                logger.info("Can't use " + method + " as it's not a static method");
            }
        }
        return handlers;
    }

    public static CtBehavior[] findMatchBehavior(CtBehavior[] ctBehaviors, HookDesc hookDesc) {
        if ("".equals(hookDesc.hookMethodDesc)) { // 空字符串表示匹配所有同名方法
            ArrayList<CtBehavior> result = new ArrayList<>();
            for (CtBehavior ctBehavior : ctBehaviors) {
                if (ctBehavior.getMethodInfo().getName().equals(hookDesc.hookMethodName)) {
                    result.add(ctBehavior);
                }
            }
            return result.toArray(new CtBehavior[0]);
        } else {
            for (CtBehavior ctBehavior : ctBehaviors) {
                if (ctBehavior.getMethodInfo().getName().equals(hookDesc.hookMethodName) &&
                        ctBehavior.getMethodInfo().getDescriptor().equals(hookDesc.hookMethodDesc)) {
                    return new CtBehavior[]{ctBehavior};
                }
            }
            logger.info(hookDesc.hookClassName + " with " + hookDesc.hookMethodName  + "." + hookDesc.hookMethodDesc + " don't find match function");
            return new CtBehavior[]{};
        }
    }

    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        className = className.replace('/', '.');
        List<HookDesc> hookDescs = handlers.get(className);

        if (hookDescs != null) {
            try {
                logger.info("Matched " + className);
                ClassPool classPool = ClassPool.getDefault();
                classPool.appendClassPath(new LoaderClassPath(loader));
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));

                for (HookDesc hookDesc : hookDescs) {
                    logger.info("Install hook to " + hookDesc.hookMethodName + "." + hookDesc.hookMethodDesc);
                    CtBehavior[] ctBehaviors;
                    if ("<init>".equals(hookDesc.hookMethodName)) {
                        ctBehaviors = findMatchBehavior(ctClass.getDeclaredConstructors(), hookDesc);
                    } else {
                        ctBehaviors = findMatchBehavior(ctClass.getDeclaredMethods(), hookDesc);
                    }

                    for (CtBehavior ctBehavior : ctBehaviors) {
                        String funcBody = hookDesc.handlerMethod.getDeclaringClass().getName() + "." + hookDesc.handlerMethod.getName();
                        switch (hookDesc.hookType) {
                            case BEFORE_RUN:
                                if (!javassist.Modifier.isStatic(ctBehavior.getModifiers())) {
                                    funcBody = "{$args=" + funcBody + "($0,$args);}";
                                } else {
                                    funcBody = "{$args="+funcBody + "($args);}";
                                }
                                ctBehavior.insertBefore(funcBody); // 替换参数
                                break;
                            case AFTER_RUN:
                                funcBody = "$_=" + funcBody + "($_);"; // 替换返回值
                                ctBehavior.insertAfter(funcBody);
                                break;
                        }
                    }
                }
                return ctClass.toBytecode();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
