package com.akari.ppx

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

/**
 * 应用程序入口类。
 * 在 [attachBaseContext] 阶段保存全局 [Context]，供整个模块（包括 Xposed 环境）使用。
 */
class App : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        context = base
    }

    companion object {
        /** 全局应用上下文，在 [attachBaseContext] 时初始化 */
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
    }
}