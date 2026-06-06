package com.akari.ppx.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akari.ppx.data.Preference
import com.akari.ppx.data.Prefs
import com.akari.ppx.data.model.CheckBoxItem
import com.akari.ppx.utils.rememberState

/**
 * 复选框列表偏好组件，点击文本项弹出对话框，展示可勾选的选项列表。
 *
 * 关闭对话框时通过 [onDismiss] 持久化勾选状态。
 *
 * @param preference 复选框列表偏好配置
 * @param items 选项列表
 * @param onDismiss 对话框关闭时的回调，用于持久化勾选数据
 */
@Composable
fun CheckBoxListPreferenceWidget(
    preference: Preference.PreferenceItem.CheckboxListPreference,
    items: List<CheckBoxItem>,
    onDismiss: () -> Unit
) {
    var isDialogShown by rememberState(value = false)
    TextPreferenceWidget(
        preference = preference,
        onClick = {
            preference.dependency?.let { Prefs.set(it, true) }
            isDialogShown = !isDialogShown
        }
    )
    if (isDialogShown) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                isDialogShown = !isDialogShown
            },
            title = {
                Text(
                    text = preference.title,
                    color = MaterialTheme.colors.primary
                )
            },
            buttons = {},
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colors.surface)
                ) {
                    items.forEach { item ->
                        var checked by rememberState(value = item.checked)
                        val onCheckedChange = {
                            checked = !checked
                            item.checked = checked
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCheckedChange()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.title,
                                modifier = Modifier.padding(vertical = 16.dp)
                            )
                            Checkbox(
                                checked = checked,
                                onCheckedChange = {
                                    onCheckedChange()
                                })
                        }
                    }
                }
            }
        )
    }
}