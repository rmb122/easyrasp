package com.rmb122.easyrasp;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class Agent {
    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        inst.appendToBootstrapClassLoaderSearch(new JarFile(Agent.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()));
        // 将自己加入 BootstrapClassLoader 的 classpath 里面. 否则 Bootstrap 加载的类中无法调用 rasp 内的 hook 函数
        Transformer transformer = Transformer.getInstance();
        inst.addTransformer(transformer);
    }
}
