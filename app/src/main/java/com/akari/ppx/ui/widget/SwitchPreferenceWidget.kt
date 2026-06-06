package com.akari.ppx.ui.widget

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Switch
import androidx.compose.runtime.Composable
import com.akari.ppx.data.Preference
import com.akari.ppx.data.Prefs
import com.akari.ppx.utils.check

/**
 * 开关偏好组件，在文本偏好项右侧显示 [Switch] 开关。
 *
 * 切换为 true 时自动设置 dependency 为 true，联动其他依赖此 key 的偏好项。
 *
 * @param preference 偏好项配置
 * @param value 当前开关状态
 * @param onValueChange 开关状态变更回调
 */
@Composable
fun SwitchPreferenceWidget(
    preference: Preference.PreferenceItem<*>,
    value: Boolean,
    onValueChange: (Boolean) -> Unit
) {
    val onClick = {
        val newValue = !value
        newValue.check(true) {
            preference.dependency?.let { Prefs.set(it, true) }
        }
        onValueChange(newValue)
    }
    TextPreferenceWidget(
        preference = preference,
        onClick = { onClick() }
    ) {
        Switch(
            checked = value,
            onCheckedChange = { onClick() },
            enabled = preference.enabled
        )
    }
}