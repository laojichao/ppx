@file:Suppress("unused")

package com.akari.ppx.xp.hook.auto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.detailPagerFragClass
import com.akari.ppx.xp.hook.BaseHook

/**
 * 自动浏览Hook。
 *
 * 提供两项功能：
 * 1. **自动浏览**：在详情页创建时获取ViewPager引用，供 [CommonHook] 自动翻页使用。
 * 2. **视频延迟切换**：当视频播放完成后自动切换到下一条内容，
 *    仅对图文类型(CellType=1)且播放状态为5(播放结束)时生效。
 *
 * @see CommonHook 配合实现自动翻页逻辑
 */
class BrowseHook : BaseHook {
    override fun onHook() {
        val (isAutoBrowse, isDelayHandoff) = listOf<Boolean>(
            XPrefs("auto_browse"),
            XPrefs("video_delay_handoff")
        )
        isAutoBrowse.check(true) {
            detailPagerFragClass?.hookAfterMethod(
                "onCreateView",
                LayoutInflater::class.java,
                ViewGroup::class.java,
                Bundle::class.java
            ) { param ->
                detailPagerFragClass?.declaredFields?.find { f ->
                    f.type.name == "com.bytedance.ies.uikit.viewpager.SwipeControlledViewPager"
                }?.name.let { f ->
                    viewPager = param.thisObject.getObjectField(f)
                }
            }
        }
        isDelayHandoff.check(true) {
            val videoHolderClass = "com.sup.superb.video.viewholder.a".findClass(cl)
            videoHolderClass.hookAfterMethod("a_", Int::class.java) { param ->
                val feedCell = param.thisObject.callMethod("P") ?: return@hookAfterMethod
                // CellType==1为图文，args[0]==5表示播放结束
                if (feedCell.callMethodAs<Int>("getCellType") == 1 && 5 == param.args[0] as Int)
                    viewPager?.callMethod("postDelayed", object : Runnable {
                        override fun run() {
                            viewPager?.apply {
                                callMethod(
                                    "setCurrentItem",
                                    callMethodAs<Int>("getCurrentItem") + 1
                                )
                            }
                        }
                    }, 0)
            }
        }
    }

    companion object {
        /** 当前详情页的ViewPager实例引用，供自动翻页使用 */
        var viewPager: Any? = null
        /** 标记是否处于自动浏览状态，为false时暂停自动翻页 */
        var isAuto = true
    }
}