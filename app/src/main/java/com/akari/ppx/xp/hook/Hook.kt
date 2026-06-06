package com.akari.ppx.xp.hook

/**
 * Hook基接口，所有Hook类均实现此接口。
 */
interface BaseHook {
    /** 执行Hook逻辑的入口方法 */
    fun onHook() = Unit
}

/**
 * 带开关控制的Hook基类。
 *
 * @property key 对应XPrefs中的开关键名，用于判断此Hook是否启用
 */
open class SwitchHook(val key: String) : BaseHook