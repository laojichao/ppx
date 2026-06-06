@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.app.Activity
import android.content.Context
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.absFeedCellClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.shareViewClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 简化分享面板Hook。
 *
 * 优化分享流程，移除冗余选项并改善下载体验：
 * - **移除分享信息**：将ShareInfo数组参数设为null，简化分享面板
 * - **清空分享渠道**：替换 `getAllShareletTypes` 返回空列表，移除所有第三方分享选项
 * - **静默下载**：替换 `getShowSuccessToast` 返回false，禁用下载成功提示
 * - **下载完成后提示并关闭**：在VideoDownloadProgressActivity中，
 *   下载完成后显示保存路径的Toast提示并自动关闭页面
 */
class ShareHook : SwitchHook("simplify_share") {
    override fun onHook() {
        // 简化分享面板，移除ShareInfo
        shareViewClass!!.hookBeforeConstructor(
            Context::class.java,
            "[Lcom.sup.android.i_sharecontroller.model.ShareInfo;",
            "[Lcom.sup.android.i_sharecontroller.model.OptionAction\$OptionActionType;",
            "com.sup.android.i_sharecontroller.model.OptionAction\$OptionActionListener",
            absFeedCellClass
        ) { param ->
            param.args[1] = null
        }
        // 清空第三方分享渠道列表
        "com.sup.android.m_sharecontroller.service.BaseShareService".replaceMethod(
            cl,
            "getAllShareletTypes",
            Context::class.java,
            Int::class.java
        ) { emptyList<Any>() }
        // 禁用默认的下载成功Toast
        "com.sup.android.video.VideoDownLoadConfig".replaceMethod(
            cl,
            "getShowSuccessToast"
        ) { false }
        // 下载完成后显示自定义Toast并关闭Activity
        "com.sup.android.uikit.VideoDownloadProgressActivity".findClass(cl).apply {
            hookAfterMethod(
                declaredMethods.find { m ->
                    m.parameterTypes.size == 3 && m.parameterTypes[0] == Boolean::class.java
                            && m.parameterTypes[1] == Boolean::class.java && m.parameterTypes[2] == Boolean::class.java
                }?.name,
                Boolean::class.java,
                Boolean::class.java,
                Boolean::class.java
            ) { param ->
                param.args[1].check(true) {
                    showStickyToast(
                        "已保存至DCIM/${
                            "com.sup.android.business_utils.config.AppConfig".findClass(cl)
                                .callStaticMethodAs<String>("getDownloadDir")
                        }文件夹"
                    )
                    (param.thisObject as Activity).finish()
                }
            }
        }
    }
}