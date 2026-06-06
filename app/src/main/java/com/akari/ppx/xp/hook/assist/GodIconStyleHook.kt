@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.check
import com.akari.ppx.utils.setSettingKeyValue
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 神评图标旧版样式Hook。
 *
 * 将common_god_icon_style设置项的值设为0，
 * 强制使用旧版神评图标样式。
 */
class GodIconStyleHook : SwitchHook("enable_old_god_icon_style") {
    override fun onHook() {
        setSettingKeyValue {
            it.check("common_god_icon_style") { 0 }
        }
    }
}