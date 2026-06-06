@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import android.view.View
import android.widget.ImageView
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 发送链接功能解锁Hook。
 *
 * 解锁发布页面的链接按钮（默认隐藏），并替换链接解析逻辑，
 * 绕过服务端校验直接构造Link对象用于发布含链接的内容。
 *
 * 注意：此Hook有风控风险，已被标记为Deprecated。
 */
@Deprecated("Under risk control")
class LinkHook : SwitchHook("unlock_send_link") {
    override fun onHook() {
        // 解锁发布页的链接按钮：将其可见性从GONE改为VISIBLE
        "com.sup.android.module.publish.view.PublishActivity".findClass(cl).apply {
            val ivLink = "com.sup.android.module.publish.R\$id".findClass(cl)
                .getStaticObjectField("iv_link") as Int
            hookAfterMethod("_\$_findCachedViewById", Int::class.java) { param ->
                param.args[0].check(ivLink) {
                    with(param.result as ImageView) {
                        visibility = View.VISIBLE
                    }
                }
            }
        }
        // 替换链接验证逻辑：跳过服务端校验，直接构造包含URL的Link对象
        "com.sup.android.module.publish.viewmodel.LinkViewModel\$b".replaceMethod(
            cl,
            "run"
        ) { param ->
            with(param.thisObject) {
                val url = getObjectFieldAs<String>("c")
                getObjectField("b")?.callMethod("a")?.callMethod(
                    "postValue", "kotlin.Triple".findClass(cl).new(
                        true,
                        "com.sup.android.module.publish.bean.Link".findClass(cl).new().apply {
                            callMethod("setMTitle", "测试测试测试")
                            callMethod(
                                "setMCover",
                                "com.sup.android.base.model.ImageModel".findClass(cl).new(
                                    "https://p3-ppx.byteimg.com/obj/tos-cn-i-8gu37r9deh/1b5e368e5ad64fd68d830ae1a44d43d9",
                                    200,
                                    200
                                )
                            )
                            callMethod("setMDescription", "")
                            callMethod("setMOriginUrl", url)
                        },
                        url
                    )
                )
            }
        }
    }
}