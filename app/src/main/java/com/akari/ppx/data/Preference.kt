package com.akari.ppx.data

import android.content.Context
import androidx.compose.runtime.Composable
import com.akari.ppx.data.model.CheckBoxItem

/**
 * 设置项的密封类层次结构，用于定义设置界面中各类偏好项的数据模型。
 * 包含纯文本项和多种可交互项（开关、编辑框、列表、复选框列表、频道列表）。
 */
sealed class Preference {
    /** 设置项标题 */
    abstract val title: String
    /** 设置项是否可用 */
    abstract val enabled: Boolean

    /**
     * 可交互的偏好项基类。
     * @param T 该偏好项存储的值类型
     */
    sealed class PreferenceItem<T> : Preference() {
        /** 摘要文本，显示在标题下方 */
        abstract val summary: String?
        /** 标题是否单行显示 */
        abstract val singleLineTitle: Boolean
        /** 前置图标 Composable */
        abstract val icon: @Composable (() -> Unit)?
        /** 依赖的偏好项 key，该依赖未满足时当前项不可用 */
        abstract val dependency: String?

        /**
         * 纯文本偏好项，点击后执行 [onClick] 回调。
         */
        data class TextPreference(
            val onClick: (Context) -> Unit = {},

            override val title: String,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<String>()

        /**
         * 可编辑文本偏好项，用户可输入字符串值。
         * @param key 存储键名
         * @param default 默认值
         * @param multi 是否多行输入
         */
        data class EditPreference(
            val key: String,
            val default: String,
            val multi: Boolean,

            override val title: String,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<String>()

        /**
         * 开关偏好项，存储布尔值。
         * @param key 存储键名
         * @param onClick 开关状态变更回调，参数为 Context 和新布尔值
         */
        data class SwitchPreference(
            val key: String,
            val onClick: (Context, Boolean) -> Unit = { _, _ -> },

            override val title: String,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<Boolean>()

        /**
         * 列表选择偏好项，用户从预定义选项中选择一个值。
         * @param key 存储键名
         * @param entries 选项映射，键为显示文本，值为实际存储值
         * @param default 默认选中值
         */
        data class ListPreference(
            val key: String,
            val entries: Map<String, String>,
            val default: String,

            override val title: String,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<String>()

        /**
         * 复选框列表偏好项，支持多个选项同时选中。
         * @param key 存储键名
         * @param items 复选框选项列表
         */
        data class CheckboxListPreference(
            val key: String,
            val items: List<CheckBoxItem>,

            override val title: String,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<String>()

        /**
         * 频道列表偏好项，用于管理可排序的频道配置。
         * @param key 存储键名
         * @param dialogTitle 弹窗标题，默认与 [title] 相同
         */
        data class ChannelListPreference(
            val key: String,

            override val title: String,
            val dialogTitle: String = title,
            override val summary: String? = null,
            override val singleLineTitle: Boolean = false,
            override val icon: @Composable (() -> Unit)? = null,
            override val enabled: Boolean = true,
            override val dependency: String? = null
        ) : PreferenceItem<String>()
    }
}