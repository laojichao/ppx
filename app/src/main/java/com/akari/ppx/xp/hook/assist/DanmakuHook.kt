@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.asyncCallbackClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.BaseHook
import java.lang.reflect.Proxy

/**
 * 弹幕增强Hook。
 *
 * 提供两项弹幕功能：
 * 1. 解锁高级弹幕发送权限（unlock_danmaku）
 * 2. 点击弹幕查询发送者信息并跳转其主页（query_danmaku_sender）
 */
class DanmakuHook : BaseHook {
    override fun onHook() {
        val (isUnlockDanmaku, isQuerySender) = listOf<Boolean>(
            XPrefs("unlock_danmaku"),
            XPrefs("query_danmaku_sender")
        )
        // 解锁高级弹幕：将用户特权中的canSendAdvanceDanmaku强制设为true
        isUnlockDanmaku.check(true) {
            "com.sup.android.mi.usercenter.model.UserInfo".hookAfterMethod(
                cl,
                "getUserPrivilege"
            ) { param ->
                param.result.setBooleanField("canSendAdvanceDanmaku", true)
            }
        }
        // 弹幕发送者查询：替换弹幕点击处理方法，通过异步回调获取用户信息后跳转个人主页
        isQuerySender.check(true) {
            "com.sup.android.m_danmaku.widget.j".findClass(cl)
                .replaceMethod(
                    "b",
                    "com.sup.android.m_danmaku.danmaku.model.d",
                    Float::class.java,
                    Float::class.java
                ) { param ->
                    "com.sup.android.module.usercenter.b.e".findClass(cl).new().callMethod(
                        "a",
                        param.args[0].getLongField("V"),
                        Proxy.newProxyInstance(cl, arrayOf(asyncCallbackClass)) { _, _, args ->
                            "com.bytedance.router.SmartRouter".findClass(cl).callStaticMethod(
                                "buildRoute",
                                param.thisObject.getObjectField("b")!!.callMethod("getContext"),
                                args[0].callMethod("getData")!!.callMethod("getProfileSchema")
                            )!!.callMethod("open")
                        }
                    )
                }
        }
    }
}