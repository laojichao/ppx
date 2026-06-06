@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.historyPoster
import com.akari.ppx.xp.Init.historyPosterClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 历史记录禁用Hook。
 *
 * 替换历史记录上报方法(historyPoster)返回true，阻止应用记录用户的浏览历史。
 */
class HistoryHook : SwitchHook("disable_history_items") {
    override fun onHook() {
        historyPosterClass!!.replaceMethod(historyPoster(), List::class.java) { true }
    }
}