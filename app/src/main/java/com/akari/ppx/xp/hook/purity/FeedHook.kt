@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.feedResponse
import com.akari.ppx.xp.Init.feedResponseClass
import com.akari.ppx.xp.hook.BaseHook
import java.util.regex.Pattern

/**
 * 信息流过滤Hook。
 *
 * 拦截信息流数据返回，根据多种条件过滤帖子：
 * - **屏蔽帖子**：通过关键词或用户名匹配(支持正则表达式)过滤指定内容
 * - **屏蔽官方账号帖子**：过滤认证描述中包含"官方账号"/"视频号"/"新媒体"的帖子
 * - **屏蔽带货帖子**：过滤包含推广信息(promotionInfo)的帖子
 * - **屏蔽直播帖子**：过滤LiveSaasFeedCell类型的直播帖子
 *
 * 使用倒序遍历确保移除元素时索引正确，各条件独立生效。
 */
class FeedHook : BaseHook {
    override fun onHook() {
        val conditions = listOf<Boolean>(
            XPrefs("remove_feeds"),
            XPrefs("remove_official_feeds"),
            XPrefs("remove_promotional_feeds"),
            XPrefs("remove_live_feeds")
        )
        conditions.find { it }?.run {
            val (keywords, users) = listOf<String>(
                XPrefs("remove_feeds_keywords"),
                XPrefs("remove_feeds_users")
            ).map { it.splitByOr() }
            feedResponseClass!!.hookBeforeMethod(
                feedResponse(),
                String::class.java,
                "com.sup.android.mi.feed.repo.bean.FeedResponse",
                Boolean::class.java,
                Int::class.java
            ) { param ->
                val feeds = param.args[1].callMethodAs<ArrayList<*>>("getData")
                feeds.indices.reversed().forEach { i ->
                    run e@{
                        val removeCurrent = { feeds.removeAt(i) }
                        val item = feeds[i].callMethodOrNull("getFeedItem")
                        val user = item?.callMethodOrNull("getAuthor")
                        conditions.forEachIndexed { index, condition ->
                            condition.check(false) { return@forEachIndexed }
                            when (index) {
                                /* 屏蔽帖子 */ 0 -> {
                                fun checkPattern(list: ArrayList<String>, input: String?): Boolean {
                                    input?.let { s ->
                                        list.find {
                                            Pattern.matches(it, s)
                                        }?.run {
                                            removeCurrent()
                                            return true
                                        }
                                    }
                                    return false
                                }
                                checkPattern(
                                    keywords,
                                    item?.callMethodOrNullAs("getContent")
                                ).check(true) {
                                    return@e
                                }
                                checkPattern(users, user?.callMethodOrNullAs("getName"))
                                    .check(true) {
                                        return@e
                                    }
                            }
                                /* 屏蔽官方账号帖子 */ 1 -> {
                                user?.callMethodOrNull("getCertifyInfo")
                                    ?.callMethodOrNullAs<String>("getDescription")?.checkIf({
                                    contains("官方账号") || contains("视频号") || contains("新媒体")
                                }) {
                                    removeCurrent()
                                    return@e
                                }
                            }
                                /* 屏蔽带货帖子 */ 2 -> {
                                item?.callMethodOrNull("getPromotionInfo")?.run {
                                    removeCurrent()
                                    return@e
                                }
                            }
                                /* 屏蔽直播帖子 */ 3 -> {
                                runCatching {
                                    if ("com.sup.android.m_live_saas.data.LiveSaasFeedCell".findClass(
                                            cl
                                        ).isInstance(feeds[i])
                                    ) {
                                        removeCurrent()
                                        return@e
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        }
    }
}
