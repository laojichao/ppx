@file:Suppress("unused")

package com.akari.ppx.xp.hook.assist

import com.akari.ppx.utils.replaceMethod
import com.akari.ppx.xp.Init.locationShower
import com.akari.ppx.xp.Init.locationShowerClass
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 位置标签显示Hook。
 *
 * 将位置标签显示方法的返回值强制设为true，
 * 使帖子始终显示地理位置标签（即使原应用可能隐藏了该功能）。
 */
class LocationHook : SwitchHook("enable_show_location_label") {
    override fun onHook() {
        locationShowerClass!!.replaceMethod(locationShower()) { true }
    }
}