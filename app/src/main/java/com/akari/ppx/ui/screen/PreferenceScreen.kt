package com.akari.ppx.ui.screen

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.akari.ppx.data.*
import com.akari.ppx.data.Const.CHANNEL_DEFAULT
import com.akari.ppx.data.Const.CHANNEL_KEY
import com.akari.ppx.data.model.ChannelItem
import com.akari.ppx.data.model.CheckBoxItem
import com.akari.ppx.ui.widget.*
import com.akari.ppx.utils.fromJsonList
import com.akari.ppx.utils.get
import com.akari.ppx.utils.splitByOr
import com.akari.ppx.utils.toJson

/** 频道列表项的懒加载状态列表，从 DataStore 读取或使用默认值初始化 */
val channelItems by lazy {
    Prefs.get<String>(CHANNEL_KEY)?.fromJsonList<ChannelItem>()?.toMutableStateList() ?: run {
        Prefs.set(CHANNEL_KEY, CHANNEL_DEFAULT.toJson())
        CHANNEL_DEFAULT.toMutableStateList()
    }
}

/**
 * 根据偏好项类型分发渲染对应的 Widget 组件。
 *
 * 支持的类型包括：文本偏好、开关偏好、列表选择偏好、编辑文本偏好、
 * 复选框列表偏好、频道列表偏好。每种类型均与 DataStore 双向绑定。
 *
 * @param preference 偏好项配置，决定渲染哪种 Widget
 */
@Composable
fun PreferenceItem(
    preference: Preference.PreferenceItem<*>,
) {
    val prefs by Prefs.dsData.collectAsState(initial = null)
    val context = LocalContext.current
    when (preference) {
        is Preference.PreferenceItem.TextPreference -> {
            TextPreferenceWidget(
                preference = preference,
                onClick = { preference.onClick(context) }
            )
        }
        is Preference.PreferenceItem.SwitchPreference -> {
            SwitchPreferenceWidget(
                preference = preference,
                value = Prefs.dataStore.get(
                    preference.key,
                    false
                ).value.flow.collectAsState(initial = false).value,
                onValueChange = { newValue ->
                    Prefs.set(preference.key, newValue)
                    preference.onClick(context, newValue)
                }
            )
        }
        is Preference.PreferenceItem.ListPreference -> {
            ListPreferenceWidget(
                preference = preference,
                value = prefs?.get(stringPreferencesKey(preference.key)) ?: run {
                    Prefs.get<String>(preference.key) ?: run {
                        Prefs.set(preference.key, preference.default)
                    }
                    preference.default
                },
                onValueChange = { newValue ->
                    Prefs.set(preference.key, newValue)
                }
            )
        }
        is Preference.PreferenceItem.EditPreference -> {
            EditPreferenceWidget(
                preference = preference,
                value = prefs?.get(stringPreferencesKey(preference.key)) ?: run {
                    Prefs.get<String>(preference.key) ?: run {
                        Prefs.set(preference.key, preference.default)
                    }
                    preference.default
                },
                default = preference.default,
                summary = { v, m -> if (m) "共${v.splitByOr().size}条数据" else "${if (v.isBlank()) "" else "当前："}$v" },
                onValueChange = { newValue ->
                    Prefs.set(preference.key, newValue)
                }
            )
        }
        is Preference.PreferenceItem.CheckboxListPreference -> {
            val items by lazy {
                Prefs.get<String>(preference.key)?.fromJsonList<CheckBoxItem>()
                    ?.toMutableStateList() ?: run {
                    Prefs.set(preference.key, preference.items.toJson())
                    preference.items
                }
            }
            CheckBoxListPreferenceWidget(
                preference = preference,
                items = items,
                onDismiss = {
                    Prefs.set(preference.key, items.toJson())
                }
            )
        }
        is Preference.PreferenceItem.ChannelListPreference -> {
            ChannelListPreferenceWidget(
                preference = preference,
                value = prefs?.get(booleanPreferencesKey(preference.key)) ?: false,
                onValueChange = { newValue ->
                    Prefs.set(preference.key, newValue)
                },
                items = channelItems,
                onDismiss = {
                    Prefs.set(CHANNEL_KEY, channelItems.toJson())
                }
            )
        }
    }
}

/**
 * 设置偏好页面，以懒加载列表形式展示当前标签页的所有偏好配置项。
 *
 * 根据 [index] 从 [prefItems] 中获取对应标签页的配置列表，
 * 并将数据模型转换为 [Preference.PreferenceItem] 渲染到 UI。
 *
 * @param index 当前标签页索引
 * @param state 懒加载列表的滚动状态，用于与外层 [com.google.accompanist.pager.HorizontalPager] 协同
 */
@Composable
fun PreferenceScreen(
    index: Int,
    state: LazyListState
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(0.95f)
            .fillMaxHeight(),
        state = state
    ) {
        prefItems[index].map { item ->
            when (item) {
                is TextItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.TextPreference(
                            title = item.title,
                            summary = item.summary,
                            onClick = item.onClick
                        )
                    )
                }
                is SwitchItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.SwitchPreference(
                            key = item.key,
                            title = item.title,
                            summary = item.summary,
                            dependency = item.dependency,
                            onClick = item.onClick
                        )
                    )
                }
                is EditItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.EditPreference(
                            key = item.key,
                            title = item.title,
                            default = item.default,
                            multi = item.multi,
                            dependency = item.dependency
                        )
                    )
                }
                is ListItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.ListPreference(
                            key = item.key,
                            title = item.title,
                            default = item.default,
                            entries = item.entries,
                            dependency = item.dependency
                        )
                    )
                }
                is CheckBoxListItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.CheckboxListPreference(
                            key = item.key,
                            title = item.title,
                            summary = item.summary,
                            items = item.items,
                            dependency = item.dependency
                        )
                    )
                }
                is ChannelListItem -> item {
                    PreferenceItem(
                        Preference.PreferenceItem.ChannelListPreference(
                            key = item.key,
                            title = item.title,
                            dialogTitle = item.dialogTitle,
                            summary = item.summary,
                            dependency = item.dependency
                        )
                    )
                }
                ItemDivider -> item {
                    Divider()
                }
                null -> {}
            }
        }
    }
}