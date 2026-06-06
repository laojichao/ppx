@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.content.Context
import android.util.Base64
import com.google.gson.JsonElement
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook

/**
 * 视频原画保存Hook。
 *
 * 替换 `VideoDownloadHelper.doDownload()` 方法，在下载前获取视频的最高画质版本：
 * 1. 从VideoModel中获取视频URI，若为空则通过详情API获取原始视频ID
 * 2. 通过snssdk API构造视频播放地址，使用MD5签名鉴权
 * 3. 解析返回的JSON，递归查找最高画质(从video_5到video_1)的main_url
 * 4. 将Base64解码后的真实视频URL替换到URL列表中
 * 5. 调用原始下载方法执行下载
 *
 * 同时替换 `isEnableDownloadGodVideo` 返回false，禁用自带的神评视频下载功能。
 */
class VideoHook : SwitchHook("save_video") {
    override fun onHook() {
        "com.sup.android.video.VideoDownloadHelper".findClass(cl).apply {
            replaceMethod(
                "doDownload",
                Context::class.java,
                "com.sup.android.base.model.VideoModel",
                "com.sup.android.video.VideoDownLoadConfig",
                "com.ss.android.socialbase.downloader.depend.IDownloadListener",
                Boolean::class.java,
                "kotlin.jvm.functions.Function1"
            ) { param ->
                CoroutineScope(Dispatchers.Main).launch {
                    val videoModel = param.args[1]
                    var resp: String

                    /** 通过DefaultHttpServiceImpl发送GET请求并返回响应字符串 */
                    fun String.get() = String(
                        "com.bytedance.apm.net.DefaultHttpServiceImpl".findClass(cl).new()
                            .callMethod("doGet", this, null)?.callMethodAs<ByteArray>("b")!!
                    )

                    withContext(Dispatchers.IO) {
                        var uri = videoModel.callMethodAs<String>("getUri")
                        // URI为空时通过详情API获取原始视频ID
                        uri.check("") {
                            uri = "https://h5.pipix.com/bds/webapi/item/detail/?item_id=${
                                param.args[2].callMethodAs<Long>("getItemId")
                            }".get()
                                .fromJsonElement()["data"]["item"]["origin_video_id"].asString
                        }
                        val ts = System.currentTimeMillis()
                        // 构造带MD5签名的视频播放地址
                        resp = "https://i.snssdk.com/video/play/1/bds/$ts/${
                            "com.bytedance.common.utility.DigestUtils".findClass(cl)
                                .callStaticMethod(
                                    "md5Hex",
                                    "ts${ts}userbdsversion1video${uri}vtypemp4f425df23905d4ee38685e276072faa0c"
                                )
                        }/mp4/$uri".get()
                    }

                    /**
                     * 递归查找最高画质视频URL。
                     * 从video_5(最高)往下尝试到video_1(最低)。
                     *
                     * @param i 画质等级(5=最高, 1=最低)
                     * @return Base64编码的视频URL
                     */
                    fun JsonElement.getMaxQualityVideo(i: Int = 5): String =
                        runCatching {
                            this["video_$i"]["main_url"].asString
                        }.getOrElse { getMaxQualityVideo(i - 1) }

                    // 解析并替换为最高画质的视频URL
                    String(
                        Base64.decode(
                            resp.fromJsonElement()["video_info"]["data"]["video_list"].getMaxQualityVideo(),
                            0
                        )
                    ).let { url ->
                        videoModel.callMethodAs<ArrayList<*>>("getUrlList").forEach {
                            it.callMethod("setUrl", url)
                        }
                    }
                    param.invokeOriginalMethod()
                }
            }
            // 禁用自带的神评视频下载
            replaceMethod("isEnableDownloadGodVideo") { false }
        }
    }
}