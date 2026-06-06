package com.akari.ppx.data.model

/**
 * 单个版本信息。
 * @param version 版本号字符串
 */
data class Version(val version: String)

/**
 * 当前模块的版本兼容性信息。
 * @param version 当前模块版本号
 * @param matches 支持的目标应用版本列表
 */
data class Current(
    val version: String,
    val matches: List<Version>
) {
    /** 所有兼容版本号用"、"连接的字符串 */
    val matchesVersion: String
        get() = matches.fold("") { acc, s -> "$acc${s.version}、" }.dropLast(1)

    /**
     * 检查目标应用版本是否在兼容列表中。
     * @param targetVersion 目标应用版本号
     * @return 如果版本匹配则返回 true
     */
    fun hasMatch(targetVersion: String) = matches.find { c ->
        c.version == targetVersion
    }?.run { true } ?: false
}

/**
 * 最新版本信息。
 * @param version 最新版本号
 * @param url 下载链接
 * @param msg 更新说明
 */
data class Latest(
    val version: String,
    val url: String,
    val msg: String
)

/**
 * 版本信息包装类，聚合当前版本和最新版本数据。
 * @param current 当前模块版本兼容性信息
 * @param latest 最新版本信息
 */
data class VersionWrapper(
    val current: Current? = null,
    val latest: Latest? = null
)
