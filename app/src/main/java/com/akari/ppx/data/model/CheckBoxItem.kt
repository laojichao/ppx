package com.akari.ppx.data.model

/**
 * 复选框数据模型，用于复选框列表偏好项。
 * @param title 选项显示文本
 * @param key 选项对应的存储键名
 * @param checked 是否选中
 */
data class CheckBoxItem(
    val title: String,
    val key: String,
    var checked: Boolean = false
)