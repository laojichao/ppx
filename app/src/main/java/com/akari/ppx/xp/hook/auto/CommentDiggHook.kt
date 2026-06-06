@file:Suppress("unused")

package com.akari.ppx.xp.hook.auto

import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.commentHolder
import com.akari.ppx.xp.Init.commentHolderClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 自动评论点赞Hook。
 *
 * 当收到评论详情时自动对评论执行点赞操作，具体逻辑：
 * 1. 拦截 `setRealCommentId` 方法，在设置评论ID前调用 [diggCell] 进行点赞。
 * 2. Hook评论Holder绑定方法，对负数ID的评论(即刚刚发送的自定义评论)
 *    强制设置为已点赞状态并设置点赞数为1。
 */
class CommentDiggHook : SwitchHook("auto_comment_digg") {
    override fun onHook() {
        // 拦截评论ID设置，对每条评论自动点赞
        "com.sup.android.mi.publish.bean.CommentBean".hookBeforeMethod(
            cl,
            "setRealCommentId",
            Long::class.java
        ) { param ->
            diggCell(cellId = param.args[0] as Long)
        }
        // 对自生成的评论(ID<0)强制标记为已点赞
        commentHolderClass!!.hookAfterMethod(commentHolder(), commentHolderClass) { param ->
            runCatching {
                param.result.callMethod("getComment")?.apply {
                    if (callMethodAs<Long>("getCommentId") < 0L) {
                        callMethod("setHasLiked", true)
                        callMethod("setLikeCount", 1L)
                    }
                }
            }
        }
    }
}