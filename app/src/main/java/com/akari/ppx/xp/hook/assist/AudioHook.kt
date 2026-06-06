@file:Suppress("unused", "unchecked_cast", "type_mismatch_warning")

package com.akari.ppx.xp.hook.assist

import android.app.Activity
import android.view.View
import com.googlecode.mp4parser.authoring.Movie
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator
import de.robv.android.xposed.XC_MethodHook
import com.akari.ppx.utils.*
import com.akari.ppx.xp.Init.absFeedCellClass
import com.akari.ppx.xp.Init.actionType1
import com.akari.ppx.xp.Init.actionType1Class
import com.akari.ppx.xp.Init.actionType2
import com.akari.ppx.xp.Init.actionType2Class
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.downConfig
import com.akari.ppx.xp.Init.downListenerClass
import com.akari.ppx.xp.hook.SwitchHook
import java.io.File
import java.io.FileOutputStream
import java.lang.Enum.valueOf

/**
 * 视频音频提取Hook。
 *
 * 在视频帖子的分享菜单中新增"保存音频"选项，
 * 下载视频后使用mp4parser库分离音频轨并保存为AAC文件到DCIM目录。
 * 仅对视频类型帖子有效，图片帖子会提示无法提取。
 */
class AudioHook : SwitchHook("save_audio") {
    override fun onHook() {
        // 获取ACTION_LIVE_WALLPAPER枚举值，复用其分享选项位来注入"保存音频"选项
        val actionType = valueOf(
            "com.sup.android.i_sharecontroller.model.OptionAction\$OptionActionType".findClass(cl) as Class<Enum<*>>,
            "ACTION_LIVE_WALLPAPER"
        )

        // 扩展MethodHookParam，在已有的操作类型数组中追加"保存音频"选项
        fun XC_MethodHook.MethodHookParam.addOption() {
            result = (result as Array<Any>).plus(actionType)
        }

        actionType1Class!!.hookAfterMethod(actionType1(), absFeedCellClass) { param ->
            param.addOption()
        }
        actionType2Class!!.hookAfterMethod(
            actionType2(),
            absFeedCellClass,
            Boolean::class.java
        ) { param ->
            param.addOption()
        }
        // 拦截分享选项按钮的setTag，将新增选项的显示文字改为"保存音频"
        View::class.java.name.hookBeforeMethod(cl, "setTag", Object::class.java) { param ->
            runCatching {
                param.args[0].check(actionType) {
                    param.thisObject.callMethod("getChildAt", 1)?.callMethod("setText", "保存音频")
                }
            }
        }
        var hasSaved = false       // 标记视频是否已缓存，用于决定下载后是否删除视频文件
        var (name, path) = arrayOfNulls<String>(2)  // 记录下载文件名和保存路径
        // 替换壁纸服务方法，将其改为触发视频下载流程
        "com.sup.android.m_wallpaper.WallPaperService".replaceMethod(
            cl,
            "setLiveWallpaper",
            Activity::class.java,
            "com.sup.android.mi.feed.repo.bean.cell.AbsFeedCell",
            MutableMap::class.java
        ) { param ->
            val downloadHelper = "com.sup.android.video.VideoDownloadHelper".findClass(cl)
                .getStaticObjectField("INSTANCE")!!
            val videoModel = "com.sup.android.mi.feed.repo.utils.AbsFeedCellUtil".findClass(cl)
                .getStaticObjectField("Companion")?.callMethod("getVideoDownload", param.args[1])
            videoModel ?: run {
                showStickyToast("视频才能提取音频哦~")
                return@replaceMethod null
            }
            val downLoadConfig = "com.sup.android.video.VideoDownLoadConfig".findClass(cl).new()
            downLoadConfig.callMethod("setItemId", -1L)
            hasSaved = downloadHelper.callMethodAs("hasDownloadVideo", videoModel, null)
            downloadHelper.callMethod(
                "doDownload",
                param.args[0],
                videoModel,
                downLoadConfig,
                null,
                true,
                null
            )
        }
        // 拦截DownloadTask的name和savePath设置，记录下载文件名和路径供后续音频提取使用
        val downloadTaskClass =
            "com.ss.android.socialbase.downloader.model.DownloadTask".findClass(cl)
        downloadTaskClass.hookBeforeMethod("name", String::class.java) { param ->
            name = param.args[0] as String
        }
        downloadTaskClass.hookBeforeMethod("savePath", String::class.java) { param ->
            path = param.args[0] as String
        }
        // 下载成功回调：使用mp4parser从视频中提取音频轨，保存为AAC文件
        downListenerClass!!.hookAfterMethod(
            "onSuccessed",
            "com.ss.android.socialbase.downloader.model.DownloadInfo"
        ) { param ->
            // itemId为-1表示是音频提取任务，跳过其他下载任务的回调
            param.thisObject.getObjectField(downConfig())?.callMethodAs<Long>("getItemId")
                ?.checkUnless(-1L) { return@hookAfterMethod }
            val (audioDir, audioPath, videoPath) = listOf(
                "$path/audio/",
                "$path/audio/${System.currentTimeMillis() / 1000}.aac",
                "$path/$name"
            )
            runCatching {
                File(audioDir).checkIf({ !exists() }) { mkdir() }
                // 从MP4文件中提取音频轨（handler为"soun"），写入AAC文件
                FileOutputStream(audioPath).use { fos ->
                    Movie().also { movie ->
                        MovieCreator.build(videoPath).tracks.find { "soun" == it.handler }
                            .let(movie::addTrack)
                    }.let(DefaultMp4Builder()::build).writeContainer(fos.channel)
                }
                // 音频提取成功后，如果视频原本未缓存则删除临时视频文件
                hasSaved.check(false) { File(videoPath).delete() }
                showStickyToast(
                    "已保存至DCIM/" +
                            "com.sup.android.business_utils.config.AppConfig".findClass(cl)
                                .callStaticMethodAs<String>("getDownloadDir") +
                            "/audio文件夹"
                )
            }.getOrElse {
                File(audioPath).delete()
                showStickyToast("保存失败，请结束皮皮虾进程后重新启动")
            }
        }
    }
}