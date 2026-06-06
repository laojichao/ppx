@file:Suppress("DEPRECATION")

package com.akari.ppx.xp

import android.content.Context
import android.os.Bundle
import com.akari.ppx.BuildConfig.APPLICATION_ID
import com.akari.ppx.data.Const.TARGET_APP_ID
import com.akari.ppx.data.XPrefs
import com.akari.ppx.data.alive.AliveActivity
import com.akari.ppx.ui.MainActivity
import com.akari.ppx.utils.check
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.utils.new
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.mainActivityClass
import com.akari.ppx.xp.Init.safeModeApplicationClass
import com.akari.ppx.xp.hook.BaseHook
import com.akari.ppx.xp.hook.SwitchHook
import dalvik.system.DexFile
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * Xposed 模块入口类，实现 [IXposedHookLoadPackage] 接口。
 *
 * 处理两个目标包的加载：
 * 1. 模块自身包名：Hook [MainActivity.isModuleActive] 使其返回 true，用于 UI 中显示激活状态。
 * 2. 皮皮虾目标包名：在 [com.sup.android.safemode.SafeModeApplication.attachBaseContext] 中初始化模块，
 *    通过 DexFile 扫描自动发现并实例化所有 [BaseHook] 子类，
 *    在目标 Activity 的 onCreate 中逐一执行 Hook。
 *    对于 [SwitchHook] 类型，会先检查对应的开关状态再决定是否执行。
 */
class Entry : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        when (lpparam.packageName) {
            APPLICATION_ID -> MainActivity.Companion::class.java.name.replaceMethod(
                lpparam.classLoader,
                "isModuleActive",
                Context::class.java
            ) { true }
            TARGET_APP_ID -> {
                cl = lpparam.classLoader
                arrayListOf<BaseHook>().let { hooks ->
                    safeModeApplicationClass?.hookBeforeMethod(
                        "attachBaseContext",
                        Context::class.java
                    ) { param ->
                        with(param.args[0] as Context) {
                            AliveActivity(this)
                            Init(this)
                            packageManager.getApplicationInfo(APPLICATION_ID, 0).run {
                                DexFile(sourceDir).entries()
                            }.asSequence().filter {
                                it.startsWith(BaseHook::class.java.`package`!!.name)
                            }.map {
                                Class.forName(it)
                            }.filter {
                                !it.isInterface && BaseHook::class.java.isAssignableFrom(it) && it != SwitchHook::class.java
                            }.forEach {
                                hooks += it.new() as BaseHook
                            }
                        }
                    }
                    mainActivityClass?.hookBeforeMethod("onCreate", Bundle::class.java) {
                        hooks.forEach {
                            when (it) {
                                is SwitchHook -> {
                                    XPrefs<Boolean>(it.key).check(true) {
                                        it.onHook()
                                    }
                                }
                                else -> it.onHook()
                            }
                        }
                    }
                }
            }
        }
    }
}