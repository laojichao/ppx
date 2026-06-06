package com.akari.ppx.ui.widget

import androidx.compose.foundation.clickable
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Modifier
import com.akari.ppx.data.Preference
import com.akari.ppx.utils.checkUnless

/**
 * 文本偏好基础组件，显示标题和可选摘要，右侧可放置自定义 trailing 组件。
 *
 * 支持 disabled 状态，禁用时降低内容透明度并屏蔽点击事件。
 * 当 preference 为单行标题时，标题文本限制为一行。
 *
 * @param preference 偏好项配置
 * @param summary 可选摘要文本，为 null 时使用 preference 自带的 summary
 * @param onClick 点击回调，仅在 enabled 状态下生效
 * @param trailing 右侧尾部组件，如 Switch 等
 */
@Composable
fun TextPreferenceWidget(
    preference: Preference.PreferenceItem<*>,
    summary: String? = null,
    onClick: () -> Unit = { },
    trailing: @Composable (() -> Unit)? = null
) {
    val enabled = compositionLocalOf(structuralEqualityPolicy()) { true }.current && preference.enabled
    CompositionLocalProvider(LocalContentAlpha provides if (enabled) ContentAlpha.high else ContentAlpha.disabled) {
        ListItem(
            text = {
                Text(
                    text = preference.title,
                    maxLines = if (preference.singleLineTitle) 1 else Int.MAX_VALUE
                )
            },
            secondaryText = (summary ?: preference.summary)?.checkUnless("") {
                {
                    Text(text = this)
                }
            },
            icon = preference.icon,
            modifier = Modifier.clickable(onClick = { if (enabled) onClick() }),
            trailing = trailing,
        )
    }
}
