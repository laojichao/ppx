@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.getDiffDays
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.utils.splitByOr
import com.akari.ppx.utils.ts2Date
import com.akari.ppx.xp.Init.inexactDate
import com.akari.ppx.xp.Init.inexactDateClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 评论精确时间显示Hook。
 *
 * 将评论区的模糊时间描述（如"3小时前"）替换为精确的日期时间格式。
 * 支持自定义近期时间格式（按天数分段）和完整时间格式。
 */
class CommentTimeHook : SwitchHook("show_exact_comment_time") {
    override fun onHook() {
        // recentFormat: 按天数分段的近期时间格式列表（以"|"分隔），exactFormat: 超出近期范围的完整时间格式
        val recentFormat = XPrefs<String>("recent_time_format").splitByOr()
        val exactFormat = XPrefs<String>("exact_time_format")
        // 替换模糊日期方法，将时间戳转换为自定义格式的精确时间
        inexactDateClass!!.replaceMethod(
            inexactDate(),
            Long::class.java,
            "kotlin.jvm.functions.Function0"
        ) { param ->
            val ts = param.args[0] as Long
            runCatching {
                ts.ts2Date(ts.getDiffDays().let { recentFormat[it] })
            }.getOrElse {
                ts.ts2Date(exactFormat)
            }
        }
    }
}


