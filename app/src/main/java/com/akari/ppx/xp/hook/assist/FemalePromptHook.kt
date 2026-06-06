@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.absFeedCellClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 女性用户名字染色Hook。
 *
 * 在帖子详情页和评论区，将女性用户的用户名文字显示为粉色（#FF6800），
 * 便于快速识别女性用户发帖/评论。
 */
class FemalePromptHook : SwitchHook("enable_female_prompt") {
    override fun onHook() {
        var isFemale = false  // 标记当前帖子/评论的作者是否为女性
        // 通过getUserName钩子获取作者性别信息（gender==2表示女性）
        "com.sup.android.mi.feed.repo.utils.AbsFeedCellUtil\$Companion".hookAfterMethod(
            cl,
            "getUserName",
            absFeedCellClass
        ) { param ->
            isFemale = 2 == param.thisObject.callMethod("getAuthorInfo", param.args[0])
                ?.callMethodAs<Int>("getGender")
        }
        // 获取帖子详情页和评论区用户名TextView的资源ID，用于精确匹配目标控件
        val targetIds = "com.sup.android.detail.R\$id".findClass(cl).run {
            listOf<Int>(
                getStaticObjectFieldAs("detail_item_user_name_tv"),
                getStaticObjectFieldAs("detail_comment_user_name_tv")
            )
        }
        // 拦截TextView.setText：对目标用户名控件，如果作者为女性则应用粉色文字样式
        TextView::class.java.name.hookBeforeMethod(
            cl,
            "setText",
            CharSequence::class.java
        ) { param ->
            with(param.thisObject as TextView) {
                targetIds.find { it == id }?.run {
                    isFemale.check(true) {
                        val text = param.args[0] as String
                        param.args[0] = SpannableString(text).apply {
                            setSpan(ForegroundColorSpan(-38784), 0, text.length, 33)
                        }
                    }
                }
            }
        }
    }
}