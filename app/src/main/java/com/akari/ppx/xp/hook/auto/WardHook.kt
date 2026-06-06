@file:Suppress("unused")

package com.akari.ppx.xp.hook.auto

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cellDigger
import com.akari.ppx.xp.Init.cellDiggerClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.godCommentDiggerClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 自动收藏Hook。
 *
 * 根据配置的触发条件自动收藏帖子，支持三种触发模式：
 * - **条件0 - 为帖子点赞时**：拦截点赞方法，点赞成功后自动收藏该帖子
 * - **条件1 - 为帖子评论时**：拦截评论发送方法，评论时自动收藏帖子
 * - **条件2 - 为评论送神时**：拦截送神评论方法，送神时自动收藏对应帖子
 *
 * 通过 `auto_ward` 开关控制启用，`auto_ward_condition` 控制触发条件。
 */
class WardHook : SwitchHook("auto_ward") {
    override fun onHook() {
        val condition = XPrefs<String>("auto_ward_condition").let {
            if (it.isEmpty()) 0 else it.toInt()
        }
        when (condition) {
            /* 为帖子点赞时 */ 0 -> {
            cellDiggerClass!!.hookBeforeMethod(
                cellDigger(),
                Int::class.java,
                Long::class.java,
                Boolean::class.java,
                Int::class.java,
                Int::class.java
            ) { param ->
                // args[2]为Boolean类型，表示是否为点赞操作(true=点赞)
                param.args[2].check(true) {
                    wardCell(param.args[1] as Long)
                }
            }
        }
            /* 为帖子评论时 */ 1 -> {
            "com.sup.android.module.publish.publish.PublishLooper\$enqueue$1".hookBeforeMethod(
                cl,
                "invoke",
                "com.sup.android.mi.publish.bean.PublishBean"
            ) { param ->
                with(param.args[0]) {
                    // 使用fakeId字段作为去重标记，避免同一评论重复触发收藏
                    if (getObjectFieldAs<Long>("fakeId") != 666L) {
                        setObjectField("fakeId", 666L)
                        wardCell(getLongField("cellId"))
                    }
                }
            }
        }
            /* 为评论送神时 */ 2 -> {
            godCommentDiggerClass!!.hookBeforeMethod(
                "a",
                Long::class.java,
                Long::class.java,
                "com.sup.android.m_comment.util.helper.g"
            ) { param ->
                wardCell(param.args[0] as Long)
            }
        }
        }
    }
}