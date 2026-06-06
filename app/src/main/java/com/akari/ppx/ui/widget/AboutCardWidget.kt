package com.akari.ppx.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 关于页面的卡片组件，带圆角、阴影和点击事件。
 *
 * 内容以水平 [Row] 布局，左侧留 25dp 间距后渲染 [content]。
 *
 * @param modifier 修饰符
 * @param backgroundColor 卡片背景色，默认为 Material surface 色
 * @param onClick 卡片点击回调
 * @param content 卡片内容
 */
@Composable
fun AboutCardWidget(
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.surface,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth(1.0f)
            .clickable {
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        backgroundColor = backgroundColor,
        elevation = 6.dp,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Spacer(modifier = Modifier.width(25.dp))
            content()
        }
    }
}