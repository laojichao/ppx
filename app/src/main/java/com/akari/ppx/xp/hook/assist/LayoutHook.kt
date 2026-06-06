@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.data.XPrefs
import com.akari.ppx.data.model.CheckBoxItem
import com.akari.ppx.utils.*
import com.akari.ppx.xp.hook.BaseHook

/**
 * 双列布局样式自定义Hook。
 *
 * 根据用户偏好设置，批量修改双列Feed布局的各个样式开关（如列数、间距等）。
 * 配置项存储在CheckBoxItem列表中，通过setSettingKeyValue逐一应用。
 */
class LayoutHook : BaseHook {
    override fun onHook() {
        val pref = XPrefs<String>("enable_double_layout_style")
        pref.check("") { return }  // 配置为空则不执行任何操作
        // 将JSON配置反序列化为CheckBoxItem列表，根据每个项的key和checked状态设置对应配置
        val items = pref.fromJsonList<CheckBoxItem>()
        setSettingKeyValue { key ->
            items.find {
                it.key == key
            }?.checked
        }
    }
}