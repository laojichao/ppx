@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.app.Activity
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 青少年模式弹窗移除Hook。
 *
 * 替换 `TeenagerService.tryShowTeenagerModeDialog()` 方法为空实现，
 * 阻止青少年模式确认弹窗的显示。
 */
class TeenHook : SwitchHook("remove_teenager_dialog") {
    override fun onHook() {
        "com.sup.superb.m_teenager.TeenagerService".replaceMethod(
            cl,
            "tryShowTeenagerModeDialog",
            Activity::class.java
        ) {}
    }
}