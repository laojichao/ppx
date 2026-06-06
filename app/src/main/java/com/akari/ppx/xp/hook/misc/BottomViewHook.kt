@file:Suppress("unused")

package com.akari.ppx.xp.hook.misc

import com.akari.ppx.utils.callMethod
import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.mainActivityClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 移除底部导航栏Hook。
 *
 * 通过强制将频道选中状态设为true并重新触发 `channelFragmentIsSelected(false)` 来隐藏底部视图。
 * 在窗口获得焦点时也会重新执行一次隐藏操作，确保状态一致。
 */
class BottomViewHook : SwitchHook("remove_bottom_view") {
    override fun onHook() {
        mainActivityClass?.apply {
            hookBeforeMethod("channelFragmentIsSelected", Boolean::class.java) { param ->
                param.args[0] = true
            }
            hookAfterMethod("onWindowFocusChanged", Boolean::class.java) { param ->
                param.thisObject.callMethod("channelFragmentIsSelected", false)
            }
        }
    }
}