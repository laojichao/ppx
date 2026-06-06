package com.akari.ppx.data.model

/**
 * 频道数据模型，用于设置界面的频道管理功能。
 * @param name 频道显示名称
 * @param type 频道类型标识，对应 API 中的分类 ID
 * @param checked 是否启用该频道
 */
data class ChannelItem(
    val name: String,
    val type: Int,
    var checked: Boolean = true
)