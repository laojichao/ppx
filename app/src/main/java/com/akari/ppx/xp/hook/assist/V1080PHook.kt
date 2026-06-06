@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 1080P画质解锁Hook。
 *
 * 替换画质选择器的验证方法（chooser.impl.c.b），使其始终返回true，
 * 从而解锁1080P等高画质选项的播放限制。
 */
class V1080PHook : SwitchHook("unlock_1080p_limit") {
    override fun onHook() {
        "com.sup.android.m_chooser.impl.c".replaceMethod(
            cl,
            "b",
            Int::class.java,
            Int::class.java
        ) { true }
    }
}