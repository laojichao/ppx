@file:Suppress("unused")

package com.akari.ppx.xp.hook.auto

import com.akari.ppx.utils.invokeOriginalMethod
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 修改分享计数Hook。
 *
 * 替换 `FeedCellService.shareCell` 方法，将原始分享调用重复执行100次(99次循环+1次原始调用)，
 * 从而将帖子的分享数累加100。通过开关 `modify_share_counts` 控制启用。
 */
class ShareHook : SwitchHook("modify_share_counts") {
    override fun onHook() {
        "com.sup.android.module.feed.repo.FeedCellService".replaceMethod(
            cl,
            "shareCell",
            Long::class.java,
            Int::class.java
        ) { param ->
            // 重复调用原始分享方法99次，加上下面1次共计100次
            repeat(99) {
                param.invokeOriginalMethod()
            }
            param.invokeOriginalMethod()
        }
    }
}