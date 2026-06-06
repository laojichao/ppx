@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 神评发送功能增强Hook。
 *
 * 解锁神评发送限制，支持自动神评功能：
 * - 当评论的aliasItemId为1时自动点赞并发送神评
 * - 当sendGodStatus为3时根据是否开启自动神评决定返回状态
 * - 自动神评受时间限制约束（auto_send_god_time_limit，默认86400秒）
 */
class SendGodHook : SwitchHook("unlock_send_god_limit") {
    override fun onHook() {
        val isAuto = XPrefs<Boolean>("auto_send_god")
        val autoTimeLimit = XPrefs<String>("auto_send_god_time_limit").let {
            if (it.isEmpty()) 86400 else it.toInt()
        }

        // 判断是否应自动发送神评：需要开启auto开关且评论发布时间在限制时间内
        fun Any.shouldSendGod() =
            isAuto && System.currentTimeMillis() / 1000 - getLongField("createTime") <= autoTimeLimit

        // 拦截神评状态查询：
        // 1. aliasItemId==1且未点赞 -> 自动点赞并发送神评，标记为已处理（aliasItemId=2）
        // 2. sendGodStatus==3 -> 根据auto设置决定返回神评状态
        "com.sup.android.mi.feed.repo.bean.comment.Comment".hookAfterMethod(
            cl,
            "getSendGodStatus"
        ) { param ->
            with(param.thisObject) {
                checkIf({ getLongField("aliasItemId") == 1L && !getBooleanField("hasLiked") }) {
                    setLongField("aliasItemId", 2)
                    diggCell(cellId = getLongField("commentId"))
                    if (shouldSendGod())
                        diggGodComment(getLongField("itemId"), getLongField("commentId"))
                    param.result = 1
                    return@hookAfterMethod
                }
                checkIf({ getIntField("sendGodStatus") == 3 }) {
                    if (getLongField("aliasItemId") == 2L) {
                        param.result = if (shouldSendGod()) 2 else 1
                        return@hookAfterMethod
                    }
                    setLongField("aliasItemId", 1)
                    param.result = 1
                }
            }

        }
    }
}