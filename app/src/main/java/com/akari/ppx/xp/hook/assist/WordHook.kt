@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 违禁词限制解除Hook。
 *
 * 绕过评论发送时的违禁词检测，包括：
 * 1. 替换tryPublish方法使其直接返回原始文本，跳过违禁词替换
 * 2. 将违规词替换表（getReplaceMap）返回null，使替换逻辑不生效
 */
class WordHook : SwitchHook("unlock_illegal_words") {
    override fun onHook() {
        // 替换评论发布方法：直接返回原始输入文本，跳过违禁词替换/拦截逻辑
        "com.sup.android.module.publish.view.NewInputCommentDialog\$tryPublish$2".replaceMethod(
            cl,
            "invoke",
            String::class.java
        ) { param ->
            param.args[0]
        }
        // 将违规词替换表设为null，使违规词替换逻辑失效
        "com.sup.android.m_illegalword.utils.RuleTable".replaceMethod(cl, "getReplaceMap") { null }
    }
}