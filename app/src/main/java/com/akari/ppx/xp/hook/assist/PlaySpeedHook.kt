@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.check
import com.akari.ppx.utils.findClass
import com.akari.ppx.utils.getStaticObjectFieldAs
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.BaseHook
import java.math.BigDecimal

/**
 * 播放速度自定义Hook。
 *
 * 自定义视频播放的正常速度和长按倍速速度，替代默认的1.0x和2.0x。
 * 同时修改长按倍速时的提示文字，显示为粉色高亮的自定义速度值。
 */
class PlaySpeedHook : BaseHook {
    override fun onHook() {
        // 从偏好设置读取自定义播放速度，空值时默认1.0x（正常）和2.0x（长按）
        val (normalPlaySpeed, pressedPlaySpeed) = listOf<String>(
            XPrefs("normal_play_speed"),
            XPrefs("pressed_play_speed")
        ).mapIndexed { i, v -> if (v.isEmpty()) 1.0f * (i + 1) else v.toFloat() }
        // 拦截播放速度设置：将默认的1.0x替换为自定义正常速度，2.0x替换为自定义长按速度
        "com.sup.android.ttvideoplayer.TTVideoEnginePlayer".hookBeforeMethod(
            cl,
            "setPlaySpeed",
            Float::class.java
        ) { param ->
            when (param.args[0] as Float) {
                1.0f -> param.args[0] = normalPlaySpeed
                2.0f -> param.args[0] = pressedPlaySpeed
            }
        }
        // 获取倍速提示TextView的资源ID，用于匹配目标控件
        val targetId = "com.sup.superb.video.R\$id".findClass(cl)
            .getStaticObjectFieldAs<Int>("video_speed_tip_tv")
        // 拦截倍速提示文字的setText，将速度数字部分高亮为粉色（#FF6800）
        TextView::class.java.name.hookBeforeMethod(
            cl,
            "setText",
            CharSequence::class.java
        ) { param ->
            with(param.thisObject as TextView) {
                id.check(targetId) {
                    val speed =
                        BigDecimal(pressedPlaySpeed.toString()).stripTrailingZeros().toPlainString()
                    param.args[0] = SpannableString("${speed}X快进中").apply {
                        setSpan(ForegroundColorSpan(-38784), 0, speed.length + 1, 33)
                    }
                }
            }
        }
    }
}