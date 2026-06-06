@file:Suppress("unused")

package com.akari.ppx.xp.hook.purity

import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.hook.SwitchHook
import kotlin.math.abs

/**
 * 原图保存Hook。
 *
 * 替换图库页面(NewGalleryActivity)的下载方法，实现以原始画质保存图片：
 * 1. 获取当前图片和缩略图信息，通过宽高差判断当前显示的是否为缩略图
 * 2. 若为缩略图，通过CDN接口获取原始图片URL，检测Content-Type确认非webp格式
 * 3. 若非缩略图或CDN获取失败，从URL列表中取原始URL并修改后缀为.png或.gif
 * 4. 替换URL后调用原始下载方法保存
 *
 * 网络请求通过okhttp3在IO线程中执行，UI操作在主线程协程中完成。
 */
class ImageHook : SwitchHook("save_image") {
    override fun onHook() {
        "com.sup.android.m_gallery.NewGalleryActivity".replaceMethod(cl, "onDownload") { param ->
            CoroutineScope(Dispatchers.Main).launch {
                with(param.thisObject as Activity) {
                    val index = callMethod("getVpGallery")?.callMethodAs<Int>("getCurrentItem")!!
                    val current = callMethodAs<ArrayList<*>>("getImages")[index]
                    val thumbs = callMethodAs<ArrayList<*>?>("getThumbs")
                    var useCdn = false
                    var url = ""

                    /** 通过宽高差(<5像素)判断两张图片是否为同一张(原图vs缩略图) */
                    fun isThumb(img1: Any, img2: Any) =
                        abs(img1.callMethodAs<Int>("getWidth") - img2.callMethodAs<Int>("getWidth")) < 5
                                && abs(
                            img1.callMethodAs<Int>("getHeight") - img2.callMethodAs<Int>(
                                "getHeight"
                            )
                        ) < 5

                    if (thumbs == null || !isThumb(current, thumbs[index])) {
                        withContext(Dispatchers.IO) {
                            url =
                                "https://sf1-ttcdn-tos.pstatp.com/obj/${current.callMethod("getUri")}"
                            val req = "okhttp3.Request\$Builder".findClass(cl).new()
                                .callMethod("url", url)?.callMethod("build")
                            val resp = "okhttp3.OkHttpClient".findClass(cl).new()
                                .callMethod("newCall", req)?.callMethod("execute")
                            // CDN返回的Content-Type非webp则可直接使用CDN链接
                            useCdn = resp?.callMethod("body")?.callMethod("contentType")
                                .toString() != "image/webp"
                        }
                    }
                    val urlList = current.callMethodAs<ArrayList<*>>("getUrlList")
                    val isGif = current.callMethodAs<Boolean>("isGif")
                    if (!useCdn) {
                        url = urlList[0].callMethodAs("getUrl")
                        // 将URL后缀替换为实际格式
                        url = url.substring(0, url.lastIndexOf('.')) + if (isGif) ".gif" else ".png"
                    }
                    urlList[0].callMethod("setUrl", url)
                    callMethod("downloadImage", urlList, isGif)
                }
            }
        }
    }
}