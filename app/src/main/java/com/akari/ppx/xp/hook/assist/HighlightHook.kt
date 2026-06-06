@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.app.Activity
import com.akari.ppx.utils.callMethod
import com.akari.ppx.utils.check
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.utils.setSettingKeyValue
import com.akari.ppx.xp.Init.photoEditorCallbackClass
import com.akari.ppx.xp.Init.photoEditorLauncher
import com.akari.ppx.xp.Init.photoEditorLauncherClass
import com.akari.ppx.xp.Init.photoEditorParamsClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 文字高亮功能解锁Hook。
 *
 * 将bds_enable_highlight设置项强制设为true以启用高亮功能，
 * 同时替换图片编辑器的启动方法，跳过付费限制直接回调成功。
 */
class HighlightHook : SwitchHook("unlock_highlight") {
    override fun onHook() {
        setSettingKeyValue {
            it.check("bds_enable_highlight") { true }
        }
        // 替换图片编辑器启动方法，绕过付费验证直接调用回调方法返回结果
        photoEditorLauncherClass!!.replaceMethod(
            photoEditorLauncher(),
            Activity::class.java,
            String::class.java,
            photoEditorCallbackClass!!.name,
            photoEditorParamsClass!!.name
        ) { param ->
            param.args[2].callMethod("a", param.args[1], false)
        }
    }
}