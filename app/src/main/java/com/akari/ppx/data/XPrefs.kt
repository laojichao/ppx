package com.akari.ppx.data

import android.net.Uri
import com.akari.ppx.data.Const.ALLOW_STARTUP_HINT
import com.akari.ppx.data.Const.CP_URI
import com.akari.ppx.utils.checkUnless
import com.akari.ppx.utils.showToast
import com.akari.ppx.xp.Init.ctx

/**
 * Xposed 模块侧的偏好读取工具（运行在目标应用进程中）。
 * 通过 ContentProvider ([PrefsProvider]) 跨进程读取主进程中存储的偏好值。
 * 包含错误状态管理，避免重复弹出提示。
 */
object XPrefs {
    /**
     * ContentProvider 访问状态枚举，用于控制错误提示的显示策略。
     */
    enum class Status {
        /** 初始状态，尚未发生错误 */
        PRIMITIVE,
        /** 发生过错误，等待下次触发时提示用户 */
        ERROR,
        /** 已弹出提示，不再重复提示 */
        DISMISSED,
    }

    /** 当前 ContentProvider 访问状态 */
    var status = Status.PRIMITIVE

    /**
     * 通过 ContentProvider 跨进程读取偏好值。
     * @param T 值类型（String 或 Boolean）
     * @param key 偏好键名
     * @param defValue 默认值，读取失败或无值时返回
     * @return 读取到的偏好值，类型不支持时返回 null
     */
    inline operator fun <reified T> invoke(key: String, defValue: T? = null): T =
        Uri.parse(CP_URI).let {
            ctx.contentResolver.run {
                // 错误处理策略：首次错误标记为 ERROR，再次触发时弹出提示，之后不再提示
                val dealError = { t: Throwable ->
                    when (status) {
                        Status.PRIMITIVE -> {
                            if (t is IllegalArgumentException)
                                status = Status.ERROR
                        }
                        Status.ERROR -> {
                            showToast(ALLOW_STARTUP_HINT)
                            status = Status.DISMISSED
                        }
                        Status.DISMISSED -> {
                            /* Do Nothing */
                        }
                    }
                }
                when (T::class.java) {
                    String::class.java -> runCatching {
                        call(it, PrefsType.STRING.method, key, null)
                            ?.getString(PrefsType.STRING.key, (defValue ?: "") as String)
                    }.getOrElse {
                        dealError(it)
                        ""
                    }
                    java.lang.Boolean::class.java -> runCatching {
                        call(it, PrefsType.BOOLEAN.method, key, null)
                            ?.getBoolean(PrefsType.BOOLEAN.key, (defValue ?: false) as Boolean)
                    }.getOrElse {
                        dealError(it)
                        false
                    }
                    else -> null
                } as T
            }
        }

    /**
     * 检查指定布尔偏好项是否为 true，不满足时执行回调。
     * @param key 偏好键名
     * @param unsatisfiedAction 偏好值为 false 时执行的操作
     */
    inline fun checkUnless(key: String, unsatisfiedAction: () -> Unit) =
        XPrefs<Boolean>(key).checkUnless(true) { unsatisfiedAction() }

}