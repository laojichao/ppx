@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.utils.check
import com.akari.ppx.utils.getIntField
import com.akari.ppx.utils.getObjectFieldAs
import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 头像装饰移除Hook。
 *
 * Hook用户信息的 `getDecorationList` 方法，遍历返回的装饰列表，
 * 移除 `decorationType==2`(头像框类型)的装饰项，使头像恢复原始外观。
 */
class AvatarHook : SwitchHook("remove_avatar_decoration") {
    override fun onHook() {
        "com.sup.android.mi.usercenter.model.UserInfo".hookAfterMethod(
            cl,
            "getDecorationList"
        ) { param ->
            val list = param.result as ArrayList<*>? ?: return@hookAfterMethod
            // 倒序遍历避免移除元素导致索引错乱
            list.indices.reversed().forEach { i ->
                list[i].getObjectFieldAs<ArrayList<*>>("decorationInfos")[0]
                    .getIntField("decorationType").check(2) {
                        list.removeAt(i)
                    }
            }
        }
    }
}