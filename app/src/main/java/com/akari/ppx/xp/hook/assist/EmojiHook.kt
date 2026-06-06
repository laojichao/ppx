@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.utils.setIntField
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 表情包收藏上限解锁Hook。
 *
 * 将表情包收藏数量上限(EMOTICON_MAX_COUNT)设置为Int.MAX_VALUE，
 * 从而突破原应用的表情包收藏数量限制。
 */
class EmojiHook : SwitchHook("unlock_emoji_limit") {
    override fun onHook() {
        // 拦截表情包收藏方法，在执行前将最大收藏数设为Integer最大值
        "com.sup.android.emoji.EmojiService".hookBeforeMethod(
            cl,
            "collectEmoticon",
            "com.sup.android.base.model.ImageModel",
            Long::class.java,
            Long::class.java,
            Long::class.java,
            "com.sup.android.superb.i_emoji.IEmojiService\$SingleCallBack"
        ) { param ->
            param.thisObject.setIntField("EMOTICON_MAX_COUNT", Int.MAX_VALUE)
        }
    }
}