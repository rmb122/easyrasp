# easyrasp

利用 instrument API 简单实现的 Java RASP.  

特性:  
* Hook 大部分的方法
* 检查, 替换方法的参数
* 检查, 替换方法的返回值

## 已实现的功能

1. Hook `org.springframework.web.servlet.FrameworkServlet.service` 获得请求的参数.  
2. Hook `java.lang.ProcessImpl.<init>` 得到实际执行的命令, 只是一个 demo, 仅能防御 Runtime.exec() 直接跑一个参数的情况 (逃  
3. Hook `java.io.ObjectInputStream.readClassDesc` 的返回值, 阻止恶意类反序列化.  
4. Hook `javax.naming.spi.NamingManager.getObjectFactoryFromReference` 阻止 LDAP 注入.  

## 怎样 Hook?

hook 声明基于注解, 示例:  
```java
@HookHandler(hookClass = "org.springframework.web.servlet.FrameworkServlet", hookMethod = "service")
public static Object[] requestHook(Object self, Object[] params) {
    currRequest.set(params[0]);
    return params;
}
```

默认的 hook 类型是在真正的代码运行前. 如果 hook 的目标方法是非 `static` 传入的参数为 `Object self, Object[] params`,  
如果是 `static` 则为 `Object[] params`, params 为函数的实参, self 为被调用方法所属的对象. 其中返回值会被用于替换原来的参数.
  
还有两种为在代码运行后, 传入的参数为 `Object ret`, 为函数的返回值, 对于 AFTER_RUN, 返回值会被用于替换原返回值.  
而对 AFTER_RUN_FINALLY, 即使 hook 的方法运行中抛出异常, 也会被运行, 因此无法替换原返回值.  

更多示例可以看 hooks 文件夹下的代码.  

## 使用方法

```sh
java -javaagent:easyrasp-1.0-SNAPSHOT-jar-with-dependencies.jar target_class
```