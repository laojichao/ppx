package com.akari.ppx.utils

import com.akari.ppx.App.Companion.context
import com.akari.ppx.data.Const.CURRENT_URI
import com.akari.ppx.data.Const.LATEST_URI
import com.akari.ppx.data.Const.TARGET_APP_ID
import com.akari.ppx.data.model.VersionWrapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL

/**
 * 版本检查工具对象。
 *
 * 负责检查目标应用（皮皮虾）的当前版本，并通过网络请求获取最新版本信息和匹配信息。
 * 使用协程在 IO 线程执行网络请求，避免阻塞主线程。
 */
object VersionChecker {
    /**
     * 获取目标应用的当前版本号。
     *
     * @return 版本号字符串，若获取失败则返回 "(未知)"
     */
    val targetVersion: String
        get() = runCatching {
            context.packageManager.getPackageInfo(TARGET_APP_ID, 0).versionName
        }.getOrDefault("(未知)")

    /**
     * 从服务器获取版本更新信息。
     *
     * 同时请求当前版本匹配信息和最新版本信息，封装为 [VersionWrapper] 返回。
     * 若网络请求失败则返回空的 [VersionWrapper]。
     *
     * @return 包含当前版本匹配信息和最新版本信息的 [VersionWrapper]
     */
    suspend fun getUpdates() = runCatching {
        withContext(Dispatchers.IO) {
            VersionWrapper(
                current = URL(CURRENT_URI).readText().fromJson(),
                latest = URL(LATEST_URI).readText().fromJson()
            )
        }
    }.getOrElse {
        VersionWrapper()
    }
}