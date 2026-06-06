@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import com.akari.ppx.data.XPrefs
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.commentResponseClass
import com.akari.ppx.xp.hook.SwitchHook
import java.util.regex.Pattern

/**
 * 评论过滤Hook。
 *
 * 根据用户配置的关键词和用户名列表过滤评论，支持正则表达式匹配：
 * - **关键词过滤**：匹配评论文本内容，命中则移除该评论
 * - **用户名过滤**：匹配评论者的用户名，命中则移除该评论
 *
 * 同时支持对回复评论(getReply)和普通评论(getComment)进行过滤。
 * 使用倒序遍历确保移除元素时索引正确。
 */
class CommentHook : SwitchHook("remove_comments") {
    override fun onHook() {
        val (keywords, users) = listOf<String>(
            XPrefs("remove_comments_keywords"),
            XPrefs("remove_comments_users")
        ).map { it.splitByOr() }
        commentResponseClass!!.hookBeforeMethod("a", ArrayList::class.java) { param ->
            runCatching {
                val comments = param.args[0] as ArrayList<*>? ?: return@hookBeforeMethod
                // 倒序遍历，避免移除元素导致索引越界
                comments.indices.reversed().forEach { i ->
                    run e@{
                        val comment = runCatching {
                            comments[i].callMethod("getReply")
                        }.getOrElse { comments[i].callMethod("getComment") }!!

                        /**
                         * 检查输入文本是否匹配规则列表中的任一模式。
                         *
                         * @param list 规则列表(支持正则表达式)
                         * @param input 待检查的文本
                         * @return 匹配到则返回true(同时移除对应评论)
                         */
                        fun checkPattern(list: ArrayList<String>, input: String?): Boolean {
                            input?.let {
                                list.forEach { s ->
                                    s.checkIf({ Pattern.matches(this, it) }) {
                                        comments.removeAt(i)
                                        return true
                                    }
                                }
                            }
                            return false
                        }
                        // 优先匹配评论文本
                        checkPattern(keywords, comment.getObjectFieldAs("text")).check(true) {
                            return@e
                        }
                        // 再匹配评论者用户名
                        checkPattern(
                            users,
                            comment.getObjectField("userInfo")?.getObjectFieldAs("name")!!
                        ).check(true) {
                            return@e
                        }
                    }
                }
            }
        }
    }
}