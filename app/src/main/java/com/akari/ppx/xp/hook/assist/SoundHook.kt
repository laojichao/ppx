@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.media.SoundPool
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 点赞音效Hook。
 *
 * 修复/增强点赞音效的播放逻辑：替换ClickSoundManager的playSound方法，
 * 确保SoundPool已正确初始化后再从缓存中获取并播放指定音效。
 */
class SoundHook : SwitchHook("enable_digg_sound") {
    override fun onHook() {
        // 替换音效播放方法：确保SoundPool已初始化，然后从cacheMap中查找并播放指定音效
        "com.sup.android.manager.ClickSoundManager".findClass(cl).apply {
            replaceMethod(
                "playSound",
                String::class.java,
                Int::class.java
            ) { param ->
                val soundPool = getStaticObjectFieldOrNullAs<SoundPool>("soundPool") ?: run {
                    callMethod("initSoundPool")
                    getStaticObjectFieldAs("soundPool")
                }
                getStaticObjectFieldAs<HashMap<String, Int>>("cacheMap")[param.args[0]]?.let {
                    soundPool.play(it, 1f, 1f, 1, param.args[1] as Int, 1f)
                }
            }
        }
    }
}