@file:Suppress("unused")

package com.akari.ppx.xp.hook.auto

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init
import com.akari.ppx.xp.Init.cellDiggerClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.detailPagerFragClass
import com.akari.ppx.xp.hook.BaseHook
import com.akari.ppx.xp.hook.auto.BrowseHook.Companion.isAuto
import com.akari.ppx.xp.hook.auto.BrowseHook.Companion.viewPager

/**
 * 通用自动化Hook，是自动功能的核心调度器。
 *
 * 在详情页翻页(OnPageSelected)时根据开关配置依次执行：
 * - **自动浏览**：延迟后自动翻到下一页，频率可配置
 * - **自动点赞**：对当前内容自动点赞，支持自定义点赞样式
 * - **自动踩**：对当前内容自动踩，支持频繁操作后暂停
 * - **自动评论**：在翻页时或点赞时发送预设评论文本
 *
 * 此外还支持：
 * - 点赞频繁失败时暂停自动浏览
 * - 条件模式：仅在为帖子点赞时才发送评论
 *
 * @see BrowseHook 提供ViewPager引用
 * @see WardHook 类似的条件触发收藏逻辑
 */
class CommonHook : BaseHook {
    override fun onHook() {
        val (isAutoBrowse, isDelayHandoff, isAutoDigg, isAutoDiss, isAutoDiggPause, isAutoDissPause, isAutoComment) = listOf<Boolean>(
            XPrefs("auto_browse"),
            XPrefs("video_delay_handoff"),
            XPrefs("auto_digg"),
            XPrefs("auto_diss"),
            XPrefs("digg_pause_after_frequent"),
            XPrefs("diss_pause_after_frequent"),
            XPrefs("auto_comment")
        )
        val isModifyStyle = XPrefs<Boolean>("modify_interaction_style")
        val (condition, browseFreq, diggStyle, dissStyle) = listOf(
            XPrefs("auto_comment_condition"),
            XPrefs("auto_browse_frequency"),
            if (isModifyStyle) XPrefs("digg_style") else "10",
            if (isModifyStyle) XPrefs("diss_style") else "10"
        ).map { if (it.isEmpty()) 0 else it.toInt() }
        val commentText = XPrefs<String>("comment_text")
        if (isAutoBrowse || isAutoDigg || isAutoDiss || isAutoComment) {
            detailPagerFragClass!!.hookAfterMethod("onPageSelected", Int::class.java) { param ->
                detailPagerFragClass!!.declaredFields.find { f ->
                    f.type == ArrayList::class.java
                }!!.name.let { f ->
                    val feedCell =
                        param.thisObject.getObjectFieldAs<ArrayList<*>>(f)[param.args[0] as Int]
                    isAutoBrowse.check(true) {
                        if (isAuto) {
                            if (!isDelayHandoff
                                || "com.sup.android.mi.feed.repo.bean.cell.NoteFeedItem".findClass(
                                    cl
                                )
                                    .isInstance(feedCell.callMethod("getFeedItem"))
                            ) {
                                viewPager?.callMethod("postDelayed", object : Runnable {
                                    override fun run() {
                                        viewPager?.apply {
                                            callMethod(
                                                "setCurrentItem",
                                                callMethodAs<Int>("getCurrentItem") + 1
                                            )
                                        }
                                    }
                                }, if (1 == param.args[0] as Int) 1000 else browseFreq)
                            }
                        } else isAuto = true
                    }
                    isAutoDigg.check(true) {
                        diggCell(
                            cellType = feedCell.callMethodAs("getCellType"),
                            cellId = feedCell.callMethodAs("getCellId"),
                            diggStyle = diggStyle,
                            reserved = 2
                        )
                    }
                    isAutoDiss.check(true) {
                        dissCell(
                            cellType = feedCell.callMethodAs("getCellType"),
                            cellId = feedCell.callMethodAs("getCellId"),
                            dissStyle = dissStyle
                        ) { _, _, args ->
                            if (isAutoDissPause && 0 != args[0].callMethodAs("getStatusCode")) isAuto =
                                false
                        }
                    }
                    isAutoComment.check(true) {
                        /* 无条件 */ condition.check(0) {
                        commentText.checkUnless("") {
                            commentCell(
                                cellType = feedCell.callMethodAs("getCellType"),
                                cellId = feedCell.callMethodAs("getCellId"),
                                commentText = commentText
                            )
                        }
                    }
                    }
                }
            }
        }
        isAutoDiggPause.check(true) {
            "com.sup.android.module.feed.repo.FeedCellService".hookAfterMethod(
                cl,
                "diggCell",
                Long::class.java,
                Int::class.java,
                Boolean::class.java,
                Int::class.java,
                Int::class.java
            ) { param ->
                if (0 != param.result.callMethodAs("getStatusCode"))
                    isAuto = false
            }
        }
        /* 为帖子点赞时 */ condition.check(1) {
            cellDiggerClass!!.hookBeforeMethod(
                Init.cellDigger(),
                Int::class.java,
                Long::class.java,
                Boolean::class.java,
                Int::class.java,
                Int::class.java
            ) { param ->
                param.args[2].check(true) {
                    commentText.checkUnless("") {
                        commentCell(
                            cellId = param.args[1] as Long,
                            commentText = commentText
                        )
                    }
                }
            }
        }
    }
}