@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.view.MotionEvent
import com.akari.ppx.utils.callMethod
import com.akari.ppx.utils.getObjectField
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.vControllerHandlerClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 防误触Hook。
 *
 * 在视频控制器层禁用手势操作（如左右滑动切换视频），
 * 防止在浏览详情页或评论区时因误触而切换视频。
 * 通过拦截dispatchTouchEvent和强制setGestureEnable为false实现。
 */
class TouchHook : SwitchHook("prevent_mistouch") {
    override fun onHook() {
        vControllerHandlerClass!!.apply {
            // 拦截触摸事件分发：在每次触摸事件到达前禁用手势
            hookBeforeMethod("dispatchTouchEvent", MotionEvent::class.java) { param ->
                runCatching {
                    with(param.thisObject) {
                        callMethod("getActivity")?.getObjectField("mAccountService")
                        callMethod("setGestureEnable", false)
                    }
                }
            }
            // 拦截手势开关设置：始终强制设为false，防止应用重新启用手势
            hookBeforeMethod("setGestureEnable", Boolean::class.java) { param ->
                param.args[0] = false
            }
        }
    }
}