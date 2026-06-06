@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.widget.ImageView
import com.akari.ppx.utils.check
import com.akari.ppx.utils.findClass
import com.akari.ppx.utils.getStaticObjectFieldAs
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 视频评论区上传按钮解锁Hook。
 *
 * 拦截评论区视频上传按钮（iv_comment_video）的setVisibility调用，
 * 强制将其可见性设为VISIBLE（值为0），确保评论区可以上传视频。
 */
class VCommentHook : SwitchHook("unlock_video_comment_limit") {
    override fun onHook() {
        // 获取评论区视频上传按钮的资源ID
        val targetId = "com.sup.android.module.publish.R\$id".findClass(cl)
            .getStaticObjectFieldAs<Int>("iv_comment_video")
        // 拦截ImageView.setVisibility：匹配目标按钮后强制设为VISIBLE
        ImageView::class.java.name.hookBeforeMethod(cl, "setVisibility", Int::class.java) { param ->
            with(param.thisObject as ImageView) {
                id.check(targetId) {
                    param.args[0] = 0
                }
            }
        }
    }
}