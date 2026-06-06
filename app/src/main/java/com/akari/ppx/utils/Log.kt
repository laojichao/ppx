package com.akari.ppx.utils

import android.util.Log

/**
 * 统一日志工具对象，封装 [android.util.Log]。
 * 使用固定 TAG "PPXPPX"，自动处理 Throwable 类型的堆栈输出。
 */
object Log {
    private const val TAG = "PPXPPX"

    /**
     * 通用日志输出方法。
     * @param f 对应级别的 android.util.Log 方法引用
     * @param obj 日志内容，Throwable 类型自动转为堆栈字符串
     */
    private fun log(f: (String, String) -> Int, obj: Any?) {
        f(TAG, if (obj is Throwable) Log.getStackTraceString(obj) else obj.toString())
    }

    /** 输出 DEBUG 级别日志 */
    fun d(obj: Any?) {
        log(Log::d, obj)
    }

    /** 输出 INFO 级别日志 */
    fun i(obj: Any?) {
        log(Log::i, obj)
    }

    /** 输出 ERROR 级别日志 */
    fun e(obj: Any?) {
        log(Log::e, obj)
    }

    /** 输出 VERBOSE 级别日志 */
    fun v(obj: Any?) {
        log(Log::v, obj)
    }

    fun w(obj: Any?) {
        log(Log::w, obj)
    }
}