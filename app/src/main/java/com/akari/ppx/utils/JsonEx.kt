package com.akari.ppx.utils

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

/**
 * 将对象序列化为 JSON 字符串。
 * @return JSON 字符串，null 对象返回 "null"
 */
fun Any?.toJson(): String = Gson().toJson(this)

/**
 * 将 JSON 字符串反序列化为指定类型的对象。
 * @param T 目标类型
 * @return 反序列化后的对象
 * @throws com.google.gson.JsonSyntaxException JSON 格式错误时
 */
inline fun <reified T> String?.fromJson(): T = Gson().fromJson(this, T::class.java)

/**
 * 将 JSON 字符串反序列化为指定类型的列表。
 * @param T 列表元素类型
 * @return 反序列化后的列表
 */
inline fun <reified T> String?.fromJsonList(): List<T> =
    Gson().fromJson(this, object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(T::class.java)

        override fun getRawType(): Type = List::class.java

        override fun getOwnerType(): Type? = null
    })

/**
 * 将 JSON 字符串反序列化为指定类型的 Map。
 * @param K Map 键类型
 * @param V Map 值类型
 * @return 反序列化后的 Map
 */
inline fun <reified K, reified V> String?.fromJsonMap(): Map<K, V> =
    Gson().fromJson(this, object : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> = arrayOf(K::class.java, V::class.java)

        override fun getRawType(): Type = Map::class.java

        override fun getOwnerType(): Type? = null
    })

/** 将对象或字符串转换为 [JsonElement] */
fun Any?.fromJsonElement(): JsonElement = run { if (this is String) this else toJson() }.fromJson()

/** 将对象或字符串转换为 [JsonArray] */
fun Any?.fromJsonArray(): JsonArray = fromJsonElement().asJsonArray

/** 通过整数索引访问 JsonArray 元素 */
operator fun JsonElement.get(i: Int): JsonElement = this.asJsonArray.get(i)

/** 通过字符串键访问 JsonObject 成员 */
operator fun JsonElement.get(s: String): JsonElement = this.asJsonObject.get(s)
