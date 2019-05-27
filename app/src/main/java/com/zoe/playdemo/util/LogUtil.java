package com.zoe.playdemo.util;

import android.util.Log;

/**
 *@author: cjl
 *@date: 2018/6/28 15:45
 *@desc: 日志工具类  打印出的日志格式：方法名(类名:行号)
 */
public class LogUtil {
    private static String className;  //类名
    private static String methodName; //方法名
    private static int    lineNumber;   //行数

    /**
     * 获取日志信息
     *
     * @param traceElements
     */
    public static void getLogInfo(StackTraceElement[] traceElements) {
        className = traceElements[1].getFileName();
        methodName = traceElements[1].getMethodName();
        lineNumber = traceElements[1].getLineNumber();
    }

    /**
     * i
     *
     * @param message
     */
    public static void i(String message) {
        getLogInfo(new Throwable().getStackTrace());
        Log.i(className, createLog(message));
    }

    /**
     * v
     *
     * @param message
     */
    public static void v(String message) {
        getLogInfo(new Throwable().getStackTrace());
        Log.v(className, createLog(message));
    }

    /**
     * d
     *
     * @param message
     */
    public static void d(String message) {
        getLogInfo(new Throwable().getStackTrace());
        Log.d(className, createLog(message));
    }

    /**
     * w
     *
     * @param message
     */
    public static void w(String message) {
        getLogInfo(new Throwable().getStackTrace());
        Log.w(className, createLog(message));
    }

    /**
     * e
     *
     * @param message
     */
    public static void e(String message) {
        getLogInfo(new Throwable().getStackTrace());
        Log.e(className, createLog(message));
    }


    /**
     * 创建Log日志
     *
     * @param log
     * @return
     */
    private static String createLog(String log) {
        StringBuffer sb = new StringBuffer("");
        sb.append(methodName)
          .append("(")
          .append(className)
          .append(":")
          .append(lineNumber)
          .append(")");
        sb.append(log);
        return sb.toString();
    }

}
