@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * Feed底栏新样式强制启用Hook。
 *
 * 将useFeedFooterNewStyle的返回值强制设为true，
 * 强制使用新版Feed帖子底栏样式。
 */
class FooterHook : SwitchHook("use_feed_footer_new_style") {
    override fun onHook() {
        "com.sup.superb.m_feedui_common.util.FeedCommonSettingsHelper\$useFeedFooterNewStyle\$2".hookAfterMethod(
            cl,
            "invoke"
        ) { param ->
            param.result = true
        }
    }
}