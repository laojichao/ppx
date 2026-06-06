@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.utils.callMethodAs
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.splashAdClass
import com.akari.ppx.xp.Init.tabItems
import com.akari.ppx.xp.Init.tabItemsClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 广告移除Hook。
 *
 * 移除应用中的多种广告形式：
 * - **开屏广告**：替换 `splashAdClass.b()` 方法返回false，跳过开屏广告展示
 * - **信息流广告**：替换 `AdFeedCell.getAdInfo()` 返回null，移除信息流中的广告卡片
 * - **Banner广告**：替换 `BannerModel.getBannerData()` 返回null，移除Banner广告
 * - **Tab栏广告项**：过滤Tab列表中包含 `comment_identify`/`novel`/`option` 标记的广告项
 */
class AdHook : SwitchHook("remove_ads") {
    override fun onHook() {
        // 跳过开屏广告
        splashAdClass!!.replaceMethod("b") { false }
        // 移除信息流广告
        "com.sup.android.mi.feed.repo.bean.ad.AdFeedCell".replaceMethod(cl, "getAdInfo") { null }
        // 移除Banner广告
        "com.sup.android.base.model.BannerModel".replaceMethod(cl, "getBannerData") { null }
        // 过滤Tab栏中的广告项
        tabItemsClass!!.hookBeforeMethod(tabItems(), ArrayList::class.java) { param ->
            arrayListOf<Any>().let { items ->
                (param.args[0] as ArrayList<*>).filter {
                    it.callMethodAs<String>("getEventParams").run {
                        contains("comment_identify") || contains("novel") || contains("option")
                    }
                }.forEach {
                    items += it
                }
                param.args[0] = items
            }
        }
    }
}