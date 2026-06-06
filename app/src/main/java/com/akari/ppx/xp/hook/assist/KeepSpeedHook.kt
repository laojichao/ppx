@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.view.MotionEvent
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.vMotionEventHandler
import com.akari.ppx.xp.Init.vMotionEventHandlerClass
import com.akari.ppx.xp.hook.SwitchHook
import java.util.*

/**
 * 长按倍速后保持播放速度Hook。
 *
 * 实现"按一次长按加速，松手不恢复；再按一次才恢复"的交互逻辑。
 * 通过两组状态标记（isFirstPress/isLongPressing）分别管理普通长按和评论区长按场景。
 */
class KeepSpeedHook : SwitchHook("keep_video_play_speed") {
    override fun onHook() {
        // isFirstPress[0]/[1]: 分别标记普通/评论区长按是否为第一次按下
        // isLongPressing[0]/[1]: 分别标记普通/评论区是否处于长按状态
        val (isFirstPress, isLongPressing) = listOf(BooleanArray(2), BooleanArray(2))
        // 视频播放状态归零（0=暂停，5=结束）时重置长按状态
        "com.sup.android.supvideoview.videoview.SupVideoView".hookAfterMethod(
            cl,
            "getPlayState"
        ) { param ->
            val state = param.result as Int
            if (state == 0 || state == 5)
                Arrays.fill(isFirstPress, false)
        }
        // 普通视频长按处理：第一次长按保持速度，第二次恢复原始速度
        vMotionEventHandlerClass!!.apply {
            hookBeforeMethod("onLongPress", MotionEvent::class.java) {
                isLongPressing[0] = true
                isFirstPress[0] = !isFirstPress[0]
            }
            replaceMethod(vMotionEventHandler(), MotionEvent::class.java) { param ->
                isLongPressing[0].check(true) {
                    isFirstPress[0].check(false) { param.invokeOriginalMethod() }
                    isLongPressing[0] = false
                }
            }
        }
        // 评论区视频长按处理：通过调用栈判断是否来自viewholder层，避免干扰其他场景
        "com.sup.superb.video.controllerlayer.j".findClass(cl).apply {
            val scope = { block: () -> Unit ->
                Thread.currentThread().stackTrace.find { it.className.startsWith("com.sup.superb.video.viewholder") }
                    ?.run { block() }
            }
            hookBeforeMethod("z") {
                scope {
                    isLongPressing[1] = true
                    isFirstPress[1] = !isFirstPress[1]
                }
            }
            replaceMethod("A") { param ->
                scope {
                    isLongPressing[1].check(true) {
                        isFirstPress[1].check(false) { param.invokeOriginalMethod() }
                        isLongPressing[1] = false
                    }
                }
                true
            }
        }
    }
}