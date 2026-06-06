@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.hookAfterMethod
import com.akari.ppx.utils.hookBeforeMethod
import com.akari.ppx.xp.Init.profileCond
import com.akari.ppx.xp.Init.profileCondClass
import com.akari.ppx.xp.Init.searchHint
import com.akari.ppx.xp.Init.searchHintClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 搜索用户限制解除Hook。
 *
 * 在个人主页点击搜索时，解除搜索次数限制（profileCondition强制返回true），
 * 并将搜索提示中的"我"替换为"他"以适配他人主页场景。
 */
class SearchHook : SwitchHook("unlock_search_user_limits") {
    override fun onHook() {
        // scope过滤器：仅在个人主页的onClick调用栈中生效（onClick出现1次且profile出现2次）
        val scope = { block: () -> Unit ->
            with(Thread.currentThread().stackTrace) {
                find {
                    count { it.methodName == "onClick" } == 1
                            && count { it.className.contains("profile") } == 2
                }?.run { block() }
            }
        }
        // 强制搜索条件检查返回true，绕过搜索次数限制
        profileCondClass!!.hookAfterMethod(profileCond(), Long::class.java) { param ->
            scope {
                param.result = true
            }
        }
        // 将搜索提示文本中的"我"替换为"他"，适配查看他人主页时的搜索场景
        searchHintClass!!.hookBeforeMethod(searchHint(), String::class.java) { param ->
            scope {
                param.args[0] = (param.args[0] as String).replace("我", "他")
            }
        }
    }
}