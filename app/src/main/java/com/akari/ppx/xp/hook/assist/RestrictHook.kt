@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.getObjectField
import com.akari.ppx.utils.hookBeforeAllMethods
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.webSharerClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 下载限制移除Hook。
 *
 * 解除帖子和评论的下载限制，包括：
 * - 强制帖子和评论的isCanDownload/getCanDownload返回true
 * - 分享面板强制显示下载选项
 * - 视频下载返回原始画质（originDownloadVideoModel），绕过水印/压缩限制
 */
class RestrictHook : SwitchHook("remove_download_restrictions") {
    override fun onHook() {
        // 强制帖子允许下载
        "com.sup.android.mi.feed.repo.bean.cell.AbsFeedItem".replaceMethod(
            cl,
            "isCanDownload"
        ) { true }
        // 强制评论允许下载
        "com.sup.android.mi.feed.repo.bean.comment.Comment".replaceMethod(
            cl,
            "getCanDownload"
        ) { true }
        // 强制分享面板的第11个参数为true（下载按钮可见）
        webSharerClass!!.hookBeforeAllMethods("showSharePanel") { param ->
            param.args[10] = true
        }
        // 视频下载模型替换：返回原始画质的视频模型，绕过压缩/水印限制
        "com.sup.android.mi.feed.repo.bean.cell.VideoFeedItem".replaceMethod(
            cl,
            "getVideoDownload"
        ) { param ->
            param.thisObject.getObjectField("originDownloadVideoModel")
        }
    }
}