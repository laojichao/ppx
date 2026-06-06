@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.content.Context
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 自动更新检查禁用Hook。
 *
 * 替换 `UpdateService.checkUpdateByAutomatic()` 方法为空实现，
 * 阻止应用在启动时自动检查更新。
 */
class UpdateHook : SwitchHook("disable_update") {
    override fun onHook() {
        "com.sup.android.m_update.UpdateService".replaceMethod(
            cl,
            "checkUpdateByAutomatic",
            Context::class.java
        ) {}
    }
}