package com.akari.ppx.data

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

/**
 * ContentProvider 实现，用于跨进程共享偏好设置数据。
 * Xposed 模块（运行在目标应用进程）通过 [call] 方法读取主进程中存储的偏好值。
 * 仅支持 String 和 Boolean 两种类型的读取。
 */
class PrefsProvider : ContentProvider() {
    /**
     * 处理跨进程偏好读取请求。
     * @param method 操作类型，对应 [PrefsType] 的 method 值（"s" 或 "b"）
     * @param key 偏好键名
     * @param extras 附加参数（未使用）
     * @return 包含偏好值的 Bundle
     */
    override fun call(method: String, key: String?, extras: Bundle?): Bundle = Bundle().apply {
        Prefs.run {
            when (method) {
                PrefsType.STRING.method -> get<String>(key!!)?.let { putString(PrefsType.STRING.key, it) }
                PrefsType.BOOLEAN.method -> get<Boolean>(key!!)?.let { putBoolean(PrefsType.BOOLEAN.key, it) }
            }
        }
    }

    override fun onCreate(): Boolean = true

    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? = null

    override fun getType(p0: Uri): String? = null

    override fun insert(p0: Uri, p1: ContentValues?): Uri? = null

    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int = 0

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int = 0
}