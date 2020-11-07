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
            while ((length = bufferedInputStream.read(read, 0, MAX_LENGTH)) != -1) {
                outputStream.write(read, 0, length);
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