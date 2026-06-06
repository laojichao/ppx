@file:Suppress("unused", "unchecked_cast", "type_mismatch_warning")

package com.akari.ppx.xp.hook.assist

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import de.robv.android.xposed.XC_MethodHook
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.absFeedCellClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.enterPi1
import com.akari.ppx.xp.Init.enterPi1Class
import com.akari.ppx.xp.Init.enterPi2
import com.akari.ppx.xp.Init.enterPi2Class
import com.akari.ppx.xp.hook.SwitchHook
import java.lang.Enum.valueOf

/**
 * 复制文字内容Hook。
 *
 * 将帖子/评论的"发布皮皮"分享选项替换为"复制文字"功能，
 * 点击后将帖子正文或评论文本复制到系统剪贴板。
 * 同时强制启用"可重新创建"标志，并捕获未初始化属性异常以防止崩溃。
 */
class CopyHook : SwitchHook("copy_item") {
    override fun onHook() {
        // 获取ACTION_PI枚举值，复用"发布皮皮"的分享选项位来注入"复制文字"
        val actionType = valueOf(
            "com.sup.android.i_sharecontroller.model.OptionAction\$OptionActionType".findClass(cl) as Class<Enum<*>>,
            "ACTION_PI"
        )
        "com.sup.android.mi.feed.repo.utils.AbsFeedCellUtil\$Companion".replaceMethod(
            cl,
            "canBeRecreated",
            absFeedCellClass
        ) { true }
        // 拦截分享选项按钮的setTag，将"发布皮皮"选项的显示文字改为"复制文字"
        View::class.java.name.hookBeforeMethod(cl, "setTag", Object::class.java) { param ->
            runCatching {
                param.args[0].check(actionType) {
                    param.thisObject.callMethod("getChildAt", 1)?.callMethod("setText", "复制文字")
                }
            }
        }

        // 扩展MethodHookParam：尝试获取帖子内容，失败则获取评论文本，复制到剪贴板
        fun XC_MethodHook.MethodHookParam.copyText() {
            val text: String? = runCatching {
                args[1].callMethod("getFeedItem")?.callMethodAs<String>("getContent")
            }.getOrElse {
                args[1].callMethod("getComment")?.callMethodAs<String>("getText")
            }
            ((args[0] as Activity).getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(
                ClipData.newPlainText(text, text)
            )
            showStickyToast("复制成功")
        }

        // 替换enterPi方法的两种重载，拦截"发布皮皮"操作为复制文字
        enterPi1Class!!.replaceMethod(
            enterPi1(),
            Activity::class.java,
            absFeedCellClass,
            String::class.java,
            String::class.java,
            String::class.java,
            Boolean::class.java
        ) { param ->
            param.copyText()
        }
        enterPi2Class!!.replaceMethod(
            enterPi2(),
            Activity::class.java,
            absFeedCellClass,
            String::class.java,
            String::class.java,
            String::class.java,
            HashMap::class.java,
            Boolean::class.java
        ) { param ->
            param.copyText()
        }
        // 捕获Kotlin属性未初始化异常，防止模块运行时崩溃
        "kotlin.jvm.internal.Intrinsics".replaceMethod(
            cl,
            "throwUninitializedPropertyAccessException",
            String::class.java
        ) {}
    }
}