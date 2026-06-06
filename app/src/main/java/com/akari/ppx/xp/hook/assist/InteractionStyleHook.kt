@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.callMethodAs
import com.akari.ppx.utils.check
import com.akari.ppx.utils.findClass
import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 互动样式自定义Hook。
 *
 * 自定义点赞（digg）和踩（diss）的动画样式类型。
 * 当用户未点赞/踩时，将返回的样式类型替换为用户自定义的样式值。
 */
class InteractionStyleHook : SwitchHook("modify_interaction_style") {
    override fun onHook() {
        // 从偏好设置读取点赞/踩的动画样式值，默认为10（原始样式）
        val (diggStyle, dissStyle) = listOf<String>(
            XPrefs("digg_style"),
            XPrefs("diss_style")
        ).map { if (it.isEmpty()) 10 else it.toInt() }
        // 当用户未点赞时，替换点赞样式；当用户未踩时，替换踩的样式
        "com.sup.android.mi.feed.repo.bean.cell.AbsFeedItem\$ItemRelation".findClass(cl).apply {
            hookAfterMethod("getDiggType") { param ->
                param.thisObject.callMethodAs<Boolean>("isLike").check(false) {
                    param.result = diggStyle
                }
            }
            hookAfterMethod("getDissType") { param ->
                param.thisObject.callMethodAs<Boolean>("isDiss").check(false) {
                    param.result = dissStyle
                }
            }
        }
    }
}