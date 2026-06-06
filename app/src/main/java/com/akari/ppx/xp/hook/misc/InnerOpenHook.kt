@file:Suppress("unused")

package com.akari.ppx.xp.hook.misc

import android.view.View
import com.akari.ppx.data.Const.APP_NAME
import com.akari.ppx.data.Const.TAB_SCHEMA
import com.akari.ppx.ui.MainActivity
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.myTabList
import com.akari.ppx.xp.Init.myTabListClass
import com.akari.ppx.xp.Init.myTabView
import com.akari.ppx.xp.Init.myTabViewClass
import com.akari.ppx.xp.hook.BaseHook

/**
 * 应用内打开模块页面Hook。
 *
 * 在底部Tab列表中添加一个指向模块设置页面的自定义Tab项，并拦截该Tab的点击事件：
 * - 修改Tab列表第2个位置的Tab，将其schema替换为模块页面的schema
 * - 拦截Tab点击，若为目标schema则打开模块的MainActivity，否则执行原始逻辑
 */
class InnerOpenHook : BaseHook {
    override fun onHook() {
        // 在Tab列表中注入自定义入口
        myTabListClass!!.hookAfterMethod(myTabList()) { param ->
            (param.result as ArrayList<*>).checkIf({ isNotEmpty() }) {
                with(get(1)) {
                    callMethod("setSchemaNeedLogin", false)
                    callMethod("setTabName", APP_NAME)
                    callMethod("setTabSchema", TAB_SCHEMA)
                    callMethod("setExtra", "{\"icon_list\":null,\"alert\":false}")
                    callMethod("setType", 4)
                }
            }
        }
        // 拦截Tab点击，匹配目标schema时打开模块页面
        "${myTabViewClass!!.name}\$1".replaceMethod(cl, "doClick", View::class.java) { param ->
            val tabView = param.thisObject.getObjectField("b")
            when (myTabViewClass!!.callStaticMethod(myTabView(), tabView)
                ?.callMethodAs<String>("getTabSchema")) {
                TAB_SCHEMA -> {
                    myTabViewClass!!.declaredFields.find { f -> f.type == View::class.java }?.name.let { f ->
                        tabView?.getObjectFieldAs<View>(f)?.context?.let { MainActivity(it) }
                    }
                }
                else -> param.invokeOriginalMethod()
            }
        }
    }
}
