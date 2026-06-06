@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.utils.findClass
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.mainActivityClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 红点/未读标记移除Hook。
 *
 * 移除应用中的各类红点提醒和未读标记：
 * - **Tab红点**：Hook FeedTabFragmentV2的三参数方法，将showDot设为false、badgeCount设为0
 * - **主页角标**：Hook MainActivity的 `handleBadgeAndPopup` 方法，将ID参数设为0
 * - **个人主页红点**：Hook MyProfileHeaderLayout的方法，将showDot设为false
 */
class RedDotHook : SwitchHook("remove_red_dots") {
    override fun onHook() {
        // 移除Tab栏红点
        "com.sup.superb.feedui.view.tabv2.FeedTabFragmentV2".findClass(cl).apply {
            hookBeforeMethod(declaredMethods.find { m ->
                m.parameterTypes.size == 3 && m.parameterTypes[0] == Boolean::class.java
                        && m.parameterTypes[1] == Int::class.java && m.parameterTypes[2] == Int::class.java
            }?.name, Boolean::class.java, Int::class.java, Int::class.java) { param ->
                param.args[0] = false
                param.args[1] = 0
            }
        }
        // 移除主页弹窗角标
        mainActivityClass?.hookBeforeMethod("handleBadgeAndPopup", Long::class.java) { param ->
            param.args[0] = 0L
        }
        // 移除个人主页红点
        "com.sup.android.m_mine.view.subview.MyProfileHeaderLayout".hookBeforeMethod(
            cl,
            "a",
            Boolean::class.java
        ) { param ->
            param.args[0] = false
        }

    }
}