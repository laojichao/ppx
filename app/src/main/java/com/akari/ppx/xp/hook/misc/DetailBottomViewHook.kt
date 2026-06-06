@file:Suppress("unused")

package com.akari.ppx.xp.hook.misc

import de.robv.android.xposed.XposedHelpers.callMethod
import com.akari.ppx.utils.hookAfterConstructor
import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.detailViewControllerClass
import com.akari.ppx.xp.Init.removeDetailBottom
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 移除详情页底部操作栏Hook。
 *
 * 在详情页控制器构造完成后，将底部操作栏(DetailBottomView)的可见性设为GONE(4)，
 * 同时替换底部视图更新方法为空实现，阻止其重新显示。
 */
class DetailBottomViewHook : SwitchHook("remove_detail_bottom_view") {
    override fun onHook() {
        detailViewControllerClass?.apply {
            hookAfterConstructor(
                "com.sup.superb.dockerbase.misc.DockerContext",
                "com.sup.android.detail.view.DetailBottomView",
                String::class.java
            ) { param ->
                // 将底部视图可见性设为GONE(4)
                callMethod(param.args[1], "setVisibility", 4)
            }
            // 替换底部视图更新方法为空实现
            replaceMethod(
                removeDetailBottom(),
                Int::class.java,
                Boolean::class.java
            ) {
            }
        }
    }
}