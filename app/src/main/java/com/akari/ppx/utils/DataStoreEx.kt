package com.akari.ppx.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 序列化器接口，用于将复杂对象与字符串之间的转换。
 * @param T 被序列化的类型
 */
interface Serializer<T : Any> {
    /**
     * 将字符串反序列化为对象。
     * @param serialized 序列化字符串
     * @return 反序列化后的对象
     */
    fun deserialize(serialized: String): T
    /**
     * 将对象序列化为字符串。
     * @param value 要序列化的对象
     * @return 序列化后的字符串
     */
    fun serialize(value: T): String
}

/**
 * DataStore 偏好项抽象接口。
 * @param T 偏好值类型
 */
interface Preference<T> {
    /** 默认值 */
    val defaultValue: T
    /** 偏好值的数据流，值变更时自动发射新值 */
    val flow: Flow<T>
    /**
     * 异步设置偏好值。
     * @param value 新的偏好值
     */
    suspend fun set(value: T)
}

/**
 * 基础偏好项实现，直接存储原始类型值。
 * @param T 值类型（Int, Double, String, Boolean, Float, Long, Set<String>）
 * @param store DataStore 实例
 * @param key 偏好键
 * @param defaultValue 默认值
 */
class BasePreference<T>(
    private val store: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    override val defaultValue: T
) : Preference<T> {
    override val flow = store.data.map {
        it[key] ?: defaultValue
    }.distinctUntilChanged().conflate()

    override suspend fun set(value: T) {
        store.edit { it[key] = value }
    }
}

/**
 * 对象偏好项实现，通过 [Serializer] 将复杂对象序列化为 String 存储。
 * @param T 对象类型
 * @param store DataStore 实例
 * @param key 偏好键
 * @param serializer 序列化器
 * @param defaultValue 默认值
 */
class ObjectPreference<T : Any>(
    private val store: DataStore<Preferences>,
    private val key: Preferences.Key<String>,
    private val serializer: Serializer<T>,
    override val defaultValue: T
) : Preference<T> {
    override val flow: Flow<T> = store.data.map { preferences ->
        preferences[key]?.let { serializer.deserialize(it) } ?: defaultValue
    }.distinctUntilChanged().conflate()

    override suspend fun set(value: T) {
        store.edit { it[key] = serializer.serialize(value) }
    }
}

/**
 * 创建 Int 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    lazy { BasePreference(this, intPreferencesKey(name), defaultValue) }

/**
 * 创建 Double 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: Double
): Lazy<Preference<Double>> =
    lazy { BasePreference(this, doublePreferencesKey(name), defaultValue) }

/**
 * 创建 String 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: String
): Lazy<Preference<String>> =
    lazy { BasePreference(this, stringPreferencesKey(name), defaultValue) }

/**
 * 创建 Boolean 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: Boolean
): Lazy<Preference<Boolean>> =
    lazy { BasePreference(this, booleanPreferencesKey(name), defaultValue) }

/**
 * 创建 Float 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: Float
): Lazy<Preference<Float>> =
    lazy { BasePreference(this, floatPreferencesKey(name), defaultValue) }

/**
 * 创建 Long 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: Long
): Lazy<Preference<Long>> =
    lazy { BasePreference(this, longPreferencesKey(name), defaultValue) }

/**
 * 创建 Set<String> 类型的懒加载偏好项。
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
fun DataStore<Preferences>.get(
    name: String,
    defaultValue: Set<String>
): Lazy<Preference<Set<String>>> =
    lazy { BasePreference(this, stringSetPreferencesKey(name), defaultValue) }

/**
 * 创建枚举类型的懒加载偏好项，使用枚举名称进行序列化/反序列化。
 * @param T 枚举类型
 * @param name 偏好键名
 * @param defaultValue 默认值
 */
inline fun <reified T : Enum<T>> DataStore<Preferences>.get(
    name: String,
    defaultValue: T
): Lazy<Preference<T>> = lazy {
    val serializer = object : Serializer<T> {
        override fun deserialize(serialized: String) = enumValueOf<T>(serialized)
        override fun serialize(value: T) = value.name
    }
    ObjectPreference(this, stringPreferencesKey(name), serializer, defaultValue)
}

/**
 * 将 [Preference] 作为 Compose State 收集，用于 UI 观察。
 * @param T 值类型
 * @param context 协程上下文
 * @return Compose State
 */
@Composable
fun <T> Preference<T>.collectAsState(
    context: CoroutineContext = EmptyCoroutineContext
): State<T> = flow.collectAsState(getBlocking(), context)

/** 挂起函数，从 Flow 中获取第一个值 */
suspend fun <T> Preference<T>.get(): T = flow.first()

/** 阻塞式获取当前偏好值 */
fun <T> Preference<T>.getBlocking(): T = runBlocking { get() }

/** 阻塞式设置偏好值 */
fun <T> Preference<T>.setBlocking(value: T) = runBlocking { set(value) }

/**
 * 获取枚举的下一个值，到达末尾后循环回第一个值。
 * @param T 枚举类型
 * @return 下一个枚举值
 */
inline fun <reified T : Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}

/** 将枚举偏好值设置为下一个值 */
suspend inline fun <reified T : Enum<T>> Preference<T>.setNext() = set(flow.first().next())
