package com.akari.ppx.ui.theme

import androidx.compose.material.MaterialTheme
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable

/**
 * 应用基础主题，使用浅色配色方案。
 *
 * 主色调和副色调均为 [LIGHT_PINK]，搭配 [Typography] 和 [Shapes] 定义全局样式。
 *
 * @param content 主题包裹的内容
 */
@Composable
fun BaseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colors = lightColors(
            primary = LIGHT_PINK,
            secondary = LIGHT_PINK,
            secondaryVariant = LIGHT_PINK,
        ),
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}