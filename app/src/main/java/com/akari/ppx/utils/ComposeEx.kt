package com.akari.ppx.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

/**
 * 创建一个可记忆的 [MutableState]，用于 Jetpack Compose 中的状态管理。
 * @param T 状态值类型
 * @param value 初始值
 * @return 记忆化的 MutableState 实例
 */
@Composable
fun <T> rememberState(value: T) = remember {
    mutableStateOf(value)
}