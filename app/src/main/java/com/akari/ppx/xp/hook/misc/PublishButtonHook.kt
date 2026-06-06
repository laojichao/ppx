@file:Suppress("unused")

package com.akari.ppx.xp.hook.misc

import com.akari.ppx.utils.callMethod
import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.xp.Init.mainActivityClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 移除发布按钮Hook。
 *
 * 在主Activity窗口获得焦点时，将底部自动播放弹窗视图(AutoPlayPopupBottomView)
 * 的可见性设为GONE(4)，从而隐藏发布按钮。
 */
class PublishButtonHook : SwitchHook("remove_publish_button") {
    override fun onHook() {
        mainActivityClass?.hookAfterMethod("onWindowFocusChanged", Boolean::class.java) { param ->
            param.thisObject.callMethod("getAutoPlayPopupBottomView")
                ?.callMethod("setVisibility", 4)
        }
    }
}