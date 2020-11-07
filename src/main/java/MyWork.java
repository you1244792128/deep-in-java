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
            ReflectUtil.invoke(object, "hello");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}