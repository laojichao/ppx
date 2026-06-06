@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * Story动态移除Hook。
 *
 * 替换 `StoryInfo.getStoryList()` 方法返回null，移除信息流中的Story动态入口。
 */
class StoryHook : SwitchHook("remove_stories") {
    override fun onHook() {
        "com.sup.android.mi.feed.repo.bean.cell.StoryInfo".replaceMethod(
            cl,
            "getStoryList"
        ) { null }
    }
}