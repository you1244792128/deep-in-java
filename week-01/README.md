### MyClassLoader.java 源代码
 ```java
/**
 * fshows.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ObjectUtil;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * @author youmingming
 * @version MyClassLoader.java, v 0.1 2020-11-07 3:23 下午 youmingming
 */
public class MyClassLoader extends ClassLoader {

    private static final int MAX_LENGTH = 1024;

    /**
     * Finds the class with the specified <a href="#name">binary name</a>.
     * This method should be overridden by class loader implementations that
     * follow the delegation model for loading classes, and will be invoked by
     * the {@link #loadClass <tt>loadClass</tt>} method after checking the
     * parent class loader for the requested class.  The default implementation
     * throws a <tt>ClassNotFoundException</tt>.
     *
     * @param name The <a href="#name">binary name</a> of the class
     * @return The resulting <tt>Class</tt> object
     * @throws ClassNotFoundException If the class could not be found
     * @since 1.2
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String fileName = "/Users/youmingming/Project/java-project/deep-in-java/" + name + ".xlass";
        byte[] classByte = decodeFile(fileName);
        if (ObjectUtil.isNotNull(classByte)) {
            try {
                return defineClass(name, classByte, 0, classByte.length);
            } catch (ClassFormatError error) {
                error.printStackTrace();
            }
        }
        return super.findClass(name);
    }

    private byte[] decodeFile(String fileName) {
        try {
            //获取文件对象
            File file = new File(fileName);
            //字节数组
            byte[] read = new byte[MAX_LENGTH];
            //文件输入流
            BufferedInputStream bufferedInputStream = FileUtil.getInputStream(file);

            //字节数据读取
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) file.length());
            int length;
            while ((length = bufferedInputStream.read(read,0,MAX_LENGTH)) != -1) {
                outputStream.write(read,0,length);
            }
            byte[] outputByte = outputStream.toByteArray();
            bufferedInputStream.close();
            outputStream.close();
            //解码 255-outputByte
            for (int i = 0; i < outputByte.length; i++) {
                outputByte[i] = (byte) ((byte) 255 - outputByte[i]);
            }

            return outputByte;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
```

### MyWork.java 源代码
```java
/**
 * fshows.com
 * Copyright (C) 2013-2020 All Rights Reserved.
 */

import cn.hutool.core.util.ReflectUtil;

/**
 * @author youmingming
 * @version MyWork.java, v 0.1 2020-11-07 2:30 下午 youmingming
 */
public class MyWork {
    public static void main(String[] args) {
        try {
            Object object = new MyClassLoader().findClass("Hello").newInstance();
            ReflectUtil.invoke(object,"hello");
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```
### 分析以下GC日志，尽可能详细的标注出GC发生时相关的信息。
```text
# 初始化标记阶段：其中 106000K 为老年代已使用内存，2097152K(2g) 为给老年代分配的内存，其中 1084619K 为当前堆已使用内存，3984640K 为给堆分配的总内存。用户态消耗的CPU时间是0.86秒，内核态消耗的CUP时间为0秒，从开始到结束消耗的总时间为0.28秒，因为多核的原故
2020-10-29T21:19:19.488+0800: 114.015: [GC (CMS Initial Mark) [1 CMS-initial-mark: 106000K(2097152K)] 1084619K(3984640K), 0.2824583 secs] [Times: user=0.86 sys=0.00, real=0.28 secs]

# 并发标记阶段：整个阶段总耗时0.16秒
2020-10-29T21:19:19.771+0800: 114.298: [CMS-concurrent-mark-start]
# 并发标记，总共花费0.160秒，cpu时间/0.160秒时钟时间 
2020-10-29T21:19:19.931+0800: 114.458: [CMS-concurrent-mark: 0.160/0.160 secs] [Times: user=0.32 sys=0.03, real=0.16 secs]

# 并发预清理阶段：为标记为 Dirty Card 的对象以及可达的对象重新遍历标记，并尝试做部分 Final Remark 的工作，总耗时 5.08 秒
2020-10-29T21:19:19.931+0800: 114.459: [CMS-concurrent-preclean-start]
# CMS预清理，总共花费0.065秒，cpu时间/0.066秒时钟时间
2020-10-29T21:19:19.998+0800: 114.525: [CMS-concurrent-preclean: 0.065/0.066 secs] [Times: user=0.05 sys=0.01, real=0.06 secs]
# CMS可终止的并发预清理开始 
2020-10-29T21:19:19.998+0800: 114.525: [CMS-concurrent-abortable-preclean-start]CMS: abort preclean due to time 
# CMS预清理，总共花费0.065秒，cpu时间/0.066秒时钟时间 
2020-10-29T21:19:25.072+0800: 119.599: [CMS-concurrent-abortable-preclean: 5.038/5.073 secs] [Times: user=7.72 sys=0.50, real=5.08 secs]

# 最终标记阶段，年轻代已使用 1279357 K，整个年轻代分配了 1887488 K
2020-10-29T21:19:25.076+0800: 119.603: [GC (CMS Final Remark) [YG occupancy: 1279357 K (1887488 K)]
## 重新扫描CMS堆中对象
2020-10-29T21:19:25.076+0800: 119.603: [Rescan (parallel) , 0.3120602 secs]
## 处理弱引用
2020-10-29T21:19:25.388+0800: 119.915: [weak refs processing, 0.0015920 secs]
## 卸载无用的类
2020-10-29T21:19:25.390+0800: 119.917: [class unloading, 0.0517863 secs]
## 清理分别包含类级元数据和内部化字符串的符号和字符串表
2020-10-29T21:19:25.441+0800: 119.969: [scrub symbol table, 0.0212825 secs]
## 106000K(2097152K) 为老年代内存使用情况，1385358K(3984640K)为堆使用情况
2020-10-29T21:19:25.463+0800: 119.990: [scrub string table, 0.0022435 secs][1 CMS-remark: 106000K(2097152K)] 1385358K(3984640K), 0.3959182 secs] [Times: user=1.33 sys=0.00, real=0.40 secs]

# 并发清除阶段，清除上面标记的无用的对象，回收内存
2020-10-29T21:19:25.473+0800: 120.000: [CMS-concurrent-sweep-start]
# 并发清理总共耗时0.067秒，cpu时间/0.067秒时钟时间 
2020-10-29T21:19:25.540+0800: 120.067: [CMS-concurrent-sweep: 0.067/0.067 secs] [Times: user=0.18 sys=0.02, real=0.06 secs]

# 并发重置阶段，重置CMS，为下一次准备
2020-10-29T21:19:25.540+0800: 120.068: [CMS-concurrent-reset-start]
#并发重置总共耗时0.003秒，cpu时间/0.003秒时钟时间 
2020-10-29T21:19:25.544+0800: 120.071: [CMS-concurrent-reset: 0.003/0.003 secs] [Times: user=0.00 sys=0.00, real=0.01 secs]
```
### 标注以下启动参数每个参数的含义
```text
java
-Denv=PRO 指定 Spring Boot profile 环境 PRO
-server 服务端模式
-Xms4g 设置jvm堆内存最小为4g
-Xmx4g 设置jvm堆内存最大为4g
-Xmn2g 设置年轻代 (新生代) jvm堆内存为2g
-XX:MaxDirectMemorySize=512m 设置jvm堆外内存大小为512M
-XX:MetaspaceSize=128m 设置jvm元数据区空间128M,到Java8时, Metaspace(元数据区) 取代 PermGen space(永久代), 用来存放class信息
-XX:MaxMetaspaceSize=512m 设置jvm元数据区最大空间512M
-XX:-UseBiasedLocking 关闭偏向锁
-XX:-UseCounterDecay 禁止JIT调用计数器衰减
-XX:AutoBoxCacheMax=10240 设置jvm自动装箱的最大范围10240
-XX:+UseConcMarkSweepGC 设置jvm老年代使用CMS收集器
-XX:CMSInitiatingOccupancyFraction=75 计算老年代最大使用率，使用cms作为垃圾回收，使用75％后开始CMS收集
-XX:+UseCMSInitiatingOccupancyOnly 设置CMS收集仅在内存占用率达到时再触发
-XX:MaxTenuringThreshold=6 设置对象在新生代中最大的存活次数为6就晋升到老生代
-XX:+ExplicitGCInvokesConcurrent 使用System.gc()时触发CMS GC，而不是Full GC
-XX:+ParallelRefProcEnabled 默认为false，并行的处理Reference对象，如WeakReference，除非在GC log里出现Reference处理时间较长的日志，否则效果不会很明显。
-XX:+PerfDisableSharedMem 禁止写统计文件
-XX:+AlwaysPreTouch JAVA进程启动的时候,虽然我们可以为JVM指定合适的内存大小,但是这些内存操作系统并没有真正的分配给JVM,而是等JVM访问这些内存的时候,才真正分配；通过配置这个参数JVM就会先访问所有分配给它的内存,让操作系统把内存真正的分配给JVM.从而提高运行时的性能，后续JVM就可以更好的访问内存了；
-XX:-OmitStackTraceInFastThrow 强制要求JVM始终抛出含堆栈的异常
-XX:+ExplicitGCInvokesConcurrent 命令JVM无论什么时候调用系统GC，都执行CMS GC，而不是Full GC
-XX:+HeapDumpOnOutOfMemoryError 当堆内存空间溢出时输出堆的内存快照。
-XX:HeapDumpPath=/home/devjava/logs/ 设置堆内存空间溢出是内存快照保存路径
-Xloggc:/home/devjava/logs/lifecircle-tradecore-gc.log 将gc垃圾回收信息输出到指定文件
-XX:+PrintGCApplicationStoppedTime 打印gc垃圾回收期间程序暂停的时间
-XX:+PrintGCDateStamps 打印GC执行的时间戳
-XX:+PrintGCDetails 打印GC日志详情
-javaagent:/home/devjava/ArmsAgent/arms-bootstrap-1.7.0-SNAPSHOT.jar 加载arms的包
-jar /home/devjava/lifecircle-tradecore/app/lifecircle-tradecore.jar 指定要启动的项目jar包
```