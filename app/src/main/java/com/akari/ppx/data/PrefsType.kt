package com.akari.ppx.data

/**
 * 偏好类型枚举，定义 ContentProvider 跨进程通信时使用的方法标识和 Bundle 键名。
 * @param method ContentProvider call 方法的 method 参数值
 * @param key Bundle 中存储值对应的键名
 */
enum class PrefsType(val method: String, val key: String) {
    STRING("s", "s"),
    BOOLEAN("b", "b"),
}