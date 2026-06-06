package com.akari.ppx.ui.widget

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.akari.ppx.data.Preference
import com.akari.ppx.data.model.ChannelItem
import com.akari.ppx.utils.rememberState
import org.burnoutcrew.reorderable.*

/**
 * 频道列表偏好组件，结合开关和可拖拽排序的弹窗列表。
 *
 * 开启开关后弹出对话框，展示频道列表项，支持长按拖拽排序和勾选操作。
 * 关闭对话框时通过 [onDismiss] 持久化列表顺序和勾选状态。
 *
 * @param preference 频道列表偏好配置
 * @param value 当前开关状态
 * @param onValueChange 开关状态变更回调
 * @param items 可观察的频道列表，支持拖拽排序
 * @param onDismiss 对话框关闭时的回调，用于持久化数据
 */
@Composable
fun ChannelListPreferenceWidget(
    preference: Preference.PreferenceItem.ChannelListPreference,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    items: SnapshotStateList<ChannelItem>,
    onDismiss: () -> Unit
) {
    var isDialogShown by rememberState(value = false)
    SwitchPreferenceWidget(
        preference = preference,
        value = value,
        onValueChange = {
            onValueChange(it)
            isDialogShown = it
        },
    )
    if (isDialogShown) {
        AlertDialog(
            onDismissRequest = {
                onDismiss()
                isDialogShown = !isDialogShown
            },
            title = {
                Text(
                    text = preference.dialogTitle,
                    color = MaterialTheme.colors.primary
                )
            },
            buttons = {},
            text = {
                val state = rememberReorderState()
                LazyColumn(
                    state = state.listState,
                    modifier = Modifier.reorderable(
                        state = state,
                        onMove = { from, to -> items.move(from.index, to.index) })
                ) {
                    items(items, { it.type }) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .draggedItem(state.offsetByKey(item.type))
                                .background(MaterialTheme.colors.surface)
                                .detectReorderAfterLongPress(state)
                        ) {
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
                                    text = item.name,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = {
                                        onCheckedChange()
                                    })
                            }
                            Divider()
                        }
                    }
                }
            }
        )
    }
}