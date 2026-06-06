@file:Suppress("unchecked_cast")

package com.akari.ppx.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import com.akari.ppx.App
import com.akari.ppx.data.Const.PREFS_NAME

/**
 * DataStore 偏好存储管理器（主进程侧）。
 * 提供基于 [preferencesDataStore] 的泛型读写方法，
 * 使用 [CoroutineScope] 在 IO 调度器上执行异步写入，
 * 读取操作通过 [runBlocking] 阻塞当前线程获取值。
 */
object Prefs {
    /** DataStore 实例的扩展属性委托 */
    private val Context.dataStore by preferencesDataStore(PREFS_NAME)
    /** IO 协程作用域，用于异步写入操作 */
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    /** 全局 DataStore 实例 */
    val dataStore
        get() = App.context.dataStore
    /** 缓存的 DataStore 数据 StateFlow，通过 [runBlocking] 懒加载初始化 */
    val dsData by lazy {
        runBlocking(scope.coroutineContext) { dataStore.data.stateIn(scope) }
    }

    /**
     * 同步读取偏好值。
     * @param T 值类型（Boolean 或 String）
     * @param key 偏好键名
     * @param defaultValue 默认值，键不存在时返回
     * @return 存储的值或 [defaultValue]
     */
    inline fun <reified T> get(key: String, defaultValue: T? = null): T? =
        runBlocking(scope.coroutineContext) {
            dsData.first()[getPrefsKey<T>(key)] ?: defaultValue
        }

    /**
     * 异步写入偏好值，在 IO 协程中执行。
     * @param T 值类型（Boolean 或 String）
     * @param key 偏好键名
     * @param value 要写入的值
     */
    inline fun <reified T> set(key: String, value: T) {
        scope.launch {
            dataStore.edit { prefs -> prefs[getPrefsKey(key)] = value }
        }
    }

    /**
     * 根据泛型类型获取对应的 [Preferences.Key]。
     * Boolean 类型使用 [booleanPreferencesKey]，其余使用 [stringPreferencesKey]。
     * @param T 键值类型
     * @param key 键名字符串
     * @return 类型化的 Preferences.Key
     */
    inline fun <reified T> getPrefsKey(key: String): Preferences.Key<T> =
        when (T::class.java) {
            Boolean::class.java -> booleanPreferencesKey(key)
            else -> stringPreferencesKey(key)
        } as Preferences.Key<T>
}