@file:Suppress("unused", "unchecked_cast")

package com.akari.ppx.xp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AndroidAppHelper
import android.content.Context
import android.os.Handler
import android.view.MotionEvent
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import com.akari.ppx.BuildConfig.APPLICATION_ID
import com.akari.ppx.data.Const.CACHE_NAME
import com.akari.ppx.utils.allClassesList
import com.akari.ppx.utils.findClass
import java.io.*
import java.lang.ref.WeakReference
import kotlin.math.max
import kotlin.reflect.KProperty

/**
 * 模块初始化对象，负责查找和缓存皮皮虾应用中的类、方法、字段引用。
 *
 * 通过反射和启发式字段/方法特征匹配，在运行时定位目标类。
 * 查找结果使用文件缓存机制持久化，避免每次启动重复扫描，
 * 缓存失效条件为目标应用或模块更新。
 *
 * 所有类引用使用 [Weak] 委托持有，避免长时间占用 ClassLoader 引用。
 */
@SuppressLint("StaticFieldLeak")
object Init {
    /** 目标应用的 ClassLoader */
    lateinit var cl: ClassLoader

    /** 目标应用的 Context */
    lateinit var ctx: Context

    /** 缓存映射表，键为功能标识，值为类名或方法名 */
    private lateinit var cache: MutableMap<String, String?>

    /**
     * 初始化模块，读取缓存并在需要时更新。
     *
     * @param context 目标应用的 Context
     */
    operator fun invoke(context: Context) {
        ctx = context
        cache = readCache(context)
        if (checkCache()) {
            writeCache(context)
        }
    }

    val safeModeApplicationClass by Weak {
        "com.sup.android.safemode.SafeModeApplication".findClass(cl)
    }

    val mainActivityClass by Weak {
        "com.sup.android.base.MainActivity".findClass(cl)
    }

    val absFeedCellClass by Weak {
        "com.sup.android.mi.feed.repo.bean.cell.AbsFeedCell".findClass(cl)
    }

    val detailPagerFragClass by Weak {
        "com.sup.android.detail.ui.DetailPagerFragment".findClass(cl)
    }

    val detailViewControllerClass by Weak {
        cache["class_detail_view_controller"]?.findClass(cl)
    }

    val splashAdClass by Weak {
        cache["class_splash_ad"]?.findClass(cl)
    }

    val tabItemsClass by Weak {
        cache["class_tab_items"]?.findClass(cl)
    }

    val myTabListClass by Weak {
        cache["class_my_tab_list"]?.findClass(cl)
    }

    val myTabViewClass by Weak {
        cache["class_my_tab_view"]?.findClass(cl)
    }

    val asyncCallbackClass by Weak {
        cache["class_async_callback"]?.findClass(cl)
    }

    val shareViewClass by Weak {
        cache["class_share_view"]?.findClass(cl)
    }

    val commentResponseClass by Weak {
        cache["class_comment_response"]?.findClass(cl)
    }

    val feedResponseClass by Weak {
        cache["class_feed_response"]?.findClass(cl)
    }

    val historyPosterClass by Weak {
        cache["class_history_poster"]?.findClass(cl)
    }

    val routerClass by Weak {
        cache["class_router"]?.findClass(cl)
    }

    val actionType1Class by Weak {
        cache["class_action_type_1"]?.findClass(cl)
    }

    val actionType2Class by Weak {
        cache["class_action_type_2"]?.findClass(cl)
    }

    val downListenerClass by Weak {
        cache["class_down_listener"]?.findClass(cl)
    }

    val enterPi1Class by Weak {
        cache["class_enter_pi_1"]?.findClass(cl)
    }

    val enterPi2Class by Weak {
        cache["class_enter_pi_2"]?.findClass(cl)
    }

    val profileCondClass by Weak {
        cache["class_profile_cond"]?.findClass(cl)
    }

    val searchHintClass by Weak {
        cache["class_search_hint"]?.findClass(cl)
    }

    val cellDiggerClass by Weak {
        cache["class_cell_digger"]?.findClass(cl)
    }

    val cellDisserClass by Weak {
        cache["class_cell_disser"]?.findClass(cl)
    }

    val godCommentDiggerClass by Weak {
        cache["class_god_comment_digger"]?.findClass(cl)
    }

    val vControllerHandlerClass by Weak {
        cache["class_video_controller_handler"]?.findClass(cl)
    }

    val inexactDateClass by Weak {
        cache["class_inexact_date"]?.findClass(cl)
    }

    val vMotionEventHandlerClass by Weak {
        cache["class_video_motion_event_handler"]?.findClass(cl)
    }

    val commentHolderClass by Weak {
        cache["class_comment_holder"]?.findClass(cl)
    }

    val photoEditorLauncherClass by Weak {
        cache["class_photo_editor_launcher"]?.findClass(cl)
    }

    val photoEditorCallbackClass by Weak {
        cache["class_photo_editor_callback"]?.findClass(cl)
    }

    val photoEditorParamsClass by Weak {
        cache["class_photo_editor_params"]?.findClass(cl)
    }

    val webSharerClass by Weak {
        cache["class_web_sharer"]?.findClass(cl)
    }

    val locationShowerClass by Weak {
        cache["class_location_shower"]?.findClass(cl)
    }

    /** 移除详情页底部视图的方法名 */
    fun removeDetailBottom() = cache["method_remove_detail_bottom_view"]!!

    /** 获取标签项列表的方法名 */
    fun tabItems() = cache["method_tab_items"]!!

    /** 获取我的标签列表的方法名 */
    fun myTabList() = cache["method_my_tab_list"]!!

    /** 获取我的标签视图的方法名 */
    fun myTabView() = cache["method_my_tab_view"]!!

    /** 异步回调方法名 */
    fun asyncCallback() = cache["method_async_callback"]!!

    /** Feed 响应处理方法名 */
    fun feedResponse() = cache["method_feed_response"]!!

    /** 历史发布者方法名 */
    fun historyPoster() = cache["method_history_poster"]!!

    /** 路由方法名 */
    fun router() = cache["method_router"]!!

    /** 操作类型1方法名 */
    fun actionType1() = cache["method_action_type_1"]!!

    /** 操作类型2方法名 */
    fun actionType2() = cache["method_action_type_2"]!!

    /** 下载配置字段名 */
    fun downConfig() = cache["field_down_config"]!!

    /** 进入皮皮虾1方法名 */
    fun enterPi1() = cache["method_enter_pi_1"]!!

    /** 进入皮皮虾2方法名 */
    fun enterPi2() = cache["method_enter_pi_2"]!!

    /** 个人资料条件方法名 */
    fun profileCond() = cache["method_profile_cond"]!!

    /** 搜索提示方法名 */
    fun searchHint() = cache["method_search_hint"]!!

    /** 内容挖掘方法名 */
    fun cellDigger() = cache["method_cell_digger"]!!

    /** 不精确日期格式化方法名 */
    fun inexactDate() = cache["method_inexact_date"]!!

    /** 视频触摸事件处理方法名 */
    fun vMotionEventHandler() = cache["method_video_motion_event_handler"]!!

    /** 评论持有者方法名 */
    fun commentHolder() = cache["method_comment_holder"]!!

    /** 图片编辑器启动方法名 */
    fun photoEditorLauncher() = cache["method_photo_editor_launcher"]!!

    /** 位置展示方法名 */
    fun locationShower() = cache["method_location_shower"]!!

    /**
     * 查找详情页视图控制器类及移除底部视图的方法。
     *
     * 通过包名前缀 "com.sup.android.detail.util.viewcontroller" 和
     * 拥有 DetailBottomView 类型字段的特征匹配目标类。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findDetailViewController(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.detail.util.viewcontroller")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.detail.view.DetailBottomView" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 2 && m.parameterTypes[0] == Int::class.java && m.parameterTypes[1] == Boolean::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找开屏广告类，匹配包含 CopyOnWriteArraySet 字段的 m_ad.initializer 包下的类。
     *
     * @return 类名，未找到返回 null
     */
    private fun findSplashAd(): String? =
        classesList.filter {
            it.startsWith("com.sup.android.superb.m_ad.initializer")
        }.find { c ->
            c.findClass(cl).declaredFields.any { it.type.name == "java.util.concurrent.CopyOnWriteArraySet" }
        }

    /**
     * 查找标签项类及方法，通过 IUserCenterService 字段和单参数 ArrayList 方法匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findTabItems(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.m_mine.view.subview")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
                    && !c.declaredFields.any { it.type.name == "com.sup.android.m_mine.bean.MyTabItem" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == ArrayList::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找我的标签列表类及方法，通过 Context 和 String 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findMyTabList(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.m_mine.utils")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == Context::class.java }
                    && c.declaredFields.any { it.type == String::class.java }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.isEmpty() && m.returnType == ArrayList::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找我的标签视图类及方法，通过包含 LinearLayout 字段的特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findMyTabView(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.m_mine.view.subview")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == LinearLayout::class.java }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == c && m.returnType.name == "com.sup.android.m_mine.bean.MyTabItem")
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找异步回调类及方法，通过无字段且单参数 ModelResult 的特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findAsyncCallback(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.mi.usercenter")
        }.map {
            it.findClass(cl)
        }.filter { c ->
            c.declaredFields.isEmpty()
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0].name == "com.sup.android.business_utils.network.ModelResult")
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找分享视图类，通过包含 LoadingLayout 字段的特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findShareView(): String? =
        classesList.filter {
            it.startsWith("com.sup.android.m_sharecontroller.ui")
        }.find { c ->
            c.findClass(cl).declaredFields.any { it.type.name == "com.sup.android.uikit.base.LoadingLayout" }
        }

    /**
     * 查找评论响应类，通过包含 CommentCursor 字段的特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findCommentResponse(): String? =
        classesList.filter {
            it.startsWith("com.sup.android.mi.feed.repo.response")
        }.find { c ->
            c.findClass(cl).declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.bean.comment.CommentCursor" }
        }

    /**
     * 查找 Feed 响应处理类及方法，通过包含 CountDownLatch 字段的特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findFeedResponse(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.module.feed.repo.manager")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "java.util.concurrent.CountDownLatch" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 4 && m.parameterTypes[0] == String::class.java && m.parameterTypes[1].name == "com.sup.android.mi.feed.repo.bean.FeedResponse"
                    && m.parameterTypes[2] == Boolean::class.java && m.parameterTypes[3] == Int::class.java
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找历史发布者类及方法，通过恰好包含一个 String 字段的特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findHistoryPoster(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.superb.feedui.repo")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.count { it.type == String::class.java } == 1
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == List::class.java && m.returnType == Boolean::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找路由类及方法，通过包含 List 和 Context 字段的特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findRouter(): Array<String?> {
        classesList.filter {
            it.startsWith("com.bytedance.router")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == java.util.List::class.java }
                    && c.declaredFields.any { it.type == Context::class.java }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 2 && m.parameterTypes[0] == Context::class.java
                    && m.parameterTypes[1].name == "com.bytedance.router.RouteIntent" && m.returnType.name == "void"
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找操作类型1类及方法，通过 AbsFeedCell 和 ShareletType 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findActionType1(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.superb.feedui.docker.part")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == absFeedCellClass }
                    && c.declaredFields.any { it.type.name == "com.sup.android.social.base.sharebase.model.ShareletType" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == absFeedCellClass
                    && m.returnType.name == "[Lcom.sup.android.i_sharecontroller.model.OptionAction\$OptionActionType;"
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找操作类型2类及方法，通过 AbsFeedCell 和 IShareView 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findActionType2(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.detail.util.viewcontroller")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == absFeedCellClass }
                    && c.declaredFields.any { it.type.name == "com.sup.android.i_sharecontroller.IShareView" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 2 && m.parameterTypes[0] == absFeedCellClass && m.parameterTypes[1] == Boolean::class.java
                    && m.returnType.name == "[Lcom.sup.android.i_sharecontroller.model.OptionAction\$OptionActionType;"
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找下载监听器类及配置字段，通过继承 AbsDownloadListener 的特征匹配。
     *
     * @return [类名, 字段名] 数组，未找到时元素为 null
     */
    private fun findDownloadListener(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.video")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.superclass?.name == "com.ss.android.socialbase.downloader.depend.AbsDownloadListener"
        }?.let { c ->
            c.declaredFields.forEach { f ->
                if (f.type.name == "com.sup.android.video.VideoDownLoadConfig")
                    return arrayOf(c.name, f.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找进入皮皮虾1方法，通过6参数方法签名匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findEnterPi1(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.base.feed")
        }.map {
            it.findClass(cl)
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 6
                    && m.parameterTypes[0] == Activity::class.java && m.parameterTypes[1] == absFeedCellClass
                    && m.parameterTypes[2] == String::class.java && m.parameterTypes[3] == String::class.java
                    && m.parameterTypes[4] == String::class.java && m.parameterTypes[5] == Boolean::class.java
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找进入皮皮虾2方法，通过7参数方法签名匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findEnterPi2(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.base")
        }.map {
            it.findClass(cl)
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 7 && m.parameterTypes[0] == Activity::class.java
                    && m.parameterTypes[1] == absFeedCellClass && m.parameterTypes[2] == String::class.java
                    && m.parameterTypes[3] == String::class.java && m.parameterTypes[4] == String::class.java
                    && m.parameterTypes[5] == java.util.HashMap::class.java && m.parameterTypes[6] == Boolean::class.java
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找个人资料条件类及方法，通过 IUserCenterService 字段和单参数 Long 方法匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findProfileCond(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.module.profile")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == Long::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找搜索提示类及方法，通过 String 和 Fragment 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findSearchHint(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.module.profile.search")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == String::class.java }
                    && c.declaredFields.any { it.type.name == "androidx.fragment.app.Fragment" }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == String::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找内容挖掘类及方法，通过 IFeedCellService、IUserCenterService 和 Handler 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findCellDigger(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.detail.util")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.IFeedCellService" }
                    && c.declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
                    && c.declaredFields.any { it.type == Handler::class.java }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 5 && m.parameterTypes[0] == Int::class.java
                    && m.parameterTypes[1] == Long::class.java && m.parameterTypes[2] == Boolean::class.java
                    && m.parameterTypes[3] == Int::class.java && m.parameterTypes[4] == Int::class.java
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找内容点踩类，通过 IFeedCellService、IUserCenterService 和 IFeedListService 字段特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findCellDisser(): String? =
        classesList.filter {
            it.startsWith("com.sup.superb.m_feedui_common.util")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.IFeedCellService" }
                    && c.declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
                    && c.declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.IFeedListService" }
        }?.name

    /**
     * 查找神评论挖掘类，通过 IFeedCellService 和 IUserCenterService 字段特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findGodCommentDigger(): String? =
        classesList.filter {
            it.startsWith("com.sup.android.m_comment.util.helper")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.IFeedCellService" }
                    && c.declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
        }?.name

    /**
     * 查找视频控制器处理类，通过包含 ProgressBar、2个 ImageView 和 2个 TextView 的特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findVControllerHandler(): String? =
        classesList.filter {
            it.startsWith("com.sup.superb.video.controllerlayer")
        }.find { c ->
            c.findClass(cl).declaredFields.any { it.type == ProgressBar::class.java }
                    && c.findClass(cl).declaredFields.count { it.type == ImageView::class.java } == 2
                    && c.findClass(cl).declaredFields.count { it.type == TextView::class.java } == 2
        }

    /**
     * 查找不精确日期格式化类及方法，通过 Boolean 字段和双参数方法签名匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findInexactDate(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.superb.m_feedui_common.util")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type == Boolean::class.java }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 2 && m.parameterTypes[0] == Long::class.java
                    && m.parameterTypes[1].name == "kotlin.jvm.functions.Function0" && m.returnType == String::class.java
                )
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找视频触摸事件处理类及方法，通过单参数 MotionEvent 方法签名匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findVMotionEventHandler(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.superb.video.viewholder")
        }.map {
            it.findClass(cl)
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == MotionEvent::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找评论持有者类及方法，通过 LottieAnimationView 和 AbsFeedCell 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findCommentHolder(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.m_comment.docker.holder")
        }.map {
            it.findClass(cl)
        }.find { c ->
            c.declaredFields.any { it.type.name == "com.airbnb.lottie.LottieAnimationView" }
                    && c.declaredFields.any { it.type == absFeedCellClass }
        }?.let { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 1 && m.parameterTypes[0] == c && m.returnType == absFeedCellClass)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 查找图片编辑器启动类及相关回调类和参数类。
     *
     * 通过自引用字段和 Boolean 字段特征匹配启动类，
     * 并从方法参数类型中提取回调类和参数类名。
     *
     * @return [类名, 回调类名, 参数类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findPhotoEditorLauncher(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.superb.video")
        }.map {
            it.findClass(cl)
        }.filter { c ->
            c.declaredFields.any { it.type == c }
                    && c.declaredFields.any { it.type == Boolean::class.java }
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.size == 4 && m.parameterTypes[0] == Activity::class.java
                    && m.parameterTypes[1] == String::class.java && m.parameterTypes[2].name.startsWith(
                        "com.sup.android.i_photoeditorui"
                    )
                    && m.parameterTypes[3].name.startsWith("com.sup.android.i_photoeditorui") && m.returnType.name == "void"
                )
                    return arrayOf(
                        c.name,
                        m.parameterTypes[2].name,
                        m.parameterTypes[3].name,
                        m.name
                    )
            }
        }
        return arrayOfNulls(4)
    }

    /**
     * 查找网页分享类，通过 Boolean、Handler 和 IUserCenterService 字段特征匹配。
     *
     * @return 类名，未找到返回 null
     */
    private fun findWebSharer(): String? =
        classesList.filter {
            it.startsWith("com.sup.android.m_web.bridge")
        }.find { c ->
            c.findClass(cl).declaredFields.any { it.type == Boolean::class.java }
                    && c.findClass(cl).declaredFields.any { it.type == Handler::class.java }
                    && c.findClass(cl).declaredFields.any { it.type.name == "com.sup.android.mi.usercenter.IUserCenterService" }
        }

    /**
     * 查找位置展示类及方法，通过 PublishLocationLabelAdapter 和 POIData 字段特征匹配。
     *
     * @return [类名, 方法名] 数组，未找到时元素为 null
     */
    private fun findLocationShower(): Array<String?> {
        classesList.filter {
            it.startsWith("com.sup.android.module.publish.view")
        }.map {
            it.findClass(cl)
        }.filter { c ->
            c.declaredFields.any { it.type.name == "com.sup.android.module.publish.view.PublishLocationLabelAdapter" }
                    && c.declaredFields.any { it.type.name == "com.sup.android.mi.feed.repo.bean.option.POIData" }
        }.forEach { c ->
            c.declaredMethods.forEach { m ->
                if (m.parameterTypes.isEmpty() && m.returnType == Boolean::class.java)
                    return arrayOf(c.name, m.name)
            }
        }
        return arrayOfNulls(2)
    }

    /**
     * 检查缓存完整性，对缺失的缓存项调用对应的查找方法补全。
     *
     * 使用 [checkOrPut] 扩展函数，仅在键不存在时执行查找并写入缓存。
     *
     * @return 有新增缓存项时返回 true，需要写入文件
     */
    private fun checkCache(): Boolean {
        var needUpdate = false

        fun <K, V> MutableMap<K, V>.checkOrPut(
            key: K,
            value: () -> V
        ): MutableMap<K, V> {
            if (!containsKey(key)) {
                put(key, value())
                needUpdate = true
            }
            return this
        }

        fun <K, V> MutableMap<K, V>.checkOrPut(
            vararg keys: K,
            values: () -> Array<V>
        ): MutableMap<K, V> {
            if (!keys.fold(true) { acc, key -> acc && containsKey(key) }) {
                putAll(keys.zip(values()))
                needUpdate = true
            }
            return this
        }

        cache.checkOrPut(
            "class_detail_view_controller",
            "method_remove_detail_bottom_view",
            values = ::findDetailViewController
        )
            .checkOrPut("class_splash_ad", value = ::findSplashAd)
            .checkOrPut("class_tab_items", "method_tab_items", values = ::findTabItems)
            .checkOrPut("class_my_tab_list", "method_my_tab_list", values = ::findMyTabList)
            .checkOrPut("class_my_tab_view", "method_my_tab_view", values = ::findMyTabView)
            .checkOrPut(
                "class_async_callback",
                "method_async_callback",
                values = ::findAsyncCallback
            )
            .checkOrPut("class_share_view", value = ::findShareView)
            .checkOrPut("class_comment_response", value = ::findCommentResponse)
            .checkOrPut("class_feed_response", "method_feed_response", values = ::findFeedResponse)
            .checkOrPut(
                "class_history_poster",
                "method_history_poster",
                values = ::findHistoryPoster
            )
            .checkOrPut("class_router", "method_router", values = ::findRouter)
            .checkOrPut("class_action_type_1", "method_action_type_1", values = ::findActionType1)
            .checkOrPut("class_action_type_2", "method_action_type_2", values = ::findActionType2)
            .checkOrPut("class_down_listener", "field_down_config", values = ::findDownloadListener)
            .checkOrPut("class_enter_pi_1", "method_enter_pi_1", values = ::findEnterPi1)
            .checkOrPut("class_enter_pi_2", "method_enter_pi_2", values = ::findEnterPi2)
            .checkOrPut("class_profile_cond", "method_profile_cond", values = ::findProfileCond)
            .checkOrPut("class_search_hint", "method_search_hint", values = ::findSearchHint)
            .checkOrPut("class_cell_digger", "method_cell_digger", values = ::findCellDigger)
            .checkOrPut("class_cell_disser", value = ::findCellDisser)
            .checkOrPut("class_god_comment_digger", value = ::findGodCommentDigger)
            .checkOrPut("class_video_controller_handler", value = ::findVControllerHandler)
            .checkOrPut("class_inexact_date", "method_inexact_date", values = ::findInexactDate)
            .checkOrPut(
                "class_video_motion_event_handler",
                "method_video_motion_event_handler",
                values = ::findVMotionEventHandler
            )
            .checkOrPut(
                "class_comment_holder",
                "method_comment_holder",
                values = ::findCommentHolder
            )
            .checkOrPut(
                "class_photo_editor_launcher",
                "class_photo_editor_callback",
                "class_photo_editor_params",
                "method_photo_editor_launcher",
                values = ::findPhotoEditorLauncher
            )
            .checkOrPut("class_web_sharer", value = ::findWebSharer)
            .checkOrPut(
                "class_location_shower",
                "method_location_shower",
                values = ::findLocationShower
            )

        return needUpdate
    }

    /** 所有类名的懒加载序列，用于各 find 方法的类扫描 */
    private val classesList by lazy {
        cl.allClassesList().asSequence()
    }

    /**
     * 从缓存文件读取之前的查找结果。
     *
     * 仅当缓存文件的更新时间晚于目标应用和模块的最后更新时间时才有效，
     * 否则返回空 Map 以触发重新扫描。
     *
     * @param context 目标应用 Context，用于获取缓存目录和包信息
     * @return 缓存映射表，无效缓存时返回空 HashMap
     */
    private fun readCache(context: Context): MutableMap<String, String?> {
        try {
            val hookCache = File(context.cacheDir, CACHE_NAME)
            if (hookCache.isFile && hookCache.canRead()) {
                val lastUpdateTime = context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                ).lastUpdateTime
                val lastModuleUpdateTime = try {
                    context.packageManager.getPackageInfo(APPLICATION_ID, 0)
                } catch (e: Throwable) {
                    null
                }?.lastUpdateTime ?: 0
                val stream = ObjectInputStream(FileInputStream(hookCache))
                val lastHookInfoUpdateTime = stream.readLong()
                if (lastHookInfoUpdateTime >= lastUpdateTime && lastHookInfoUpdateTime >= lastModuleUpdateTime)
                    return stream.readObject() as MutableMap<String, String?>
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return HashMap()
    }

    /**
     * 将当前缓存映射表序列化写入文件。
     *
     * 写入前记录目标应用和模块的最后更新时间戳，用于后续缓存有效性判断。
     *
     * @param context 目标应用 Context，用于获取缓存目录和包信息
     */
    private fun writeCache(context: Context) {
        try {
            val hookCache = File(context.cacheDir, CACHE_NAME)
            val lastUpdateTime = context.packageManager.getPackageInfo(
                AndroidAppHelper.currentPackageName(),
                0
            ).lastUpdateTime
            val lastModuleUpdateTime = try {
                context.packageManager.getPackageInfo(APPLICATION_ID, 0)
            } catch (e: Throwable) {
                null
            }?.lastUpdateTime ?: 0
            if (hookCache.exists()) {
                hookCache.delete()
            }
            ObjectOutputStream(FileOutputStream(hookCache)).use { stream ->
                stream.writeLong(max(lastModuleUpdateTime, lastUpdateTime))
                stream.writeObject(cache)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 弱引用属性委托，用于延迟初始化并弱持有 [Class] 引用。
     *
     * 首次访问时调用 [initializer] 获取 Class 对象并用 WeakReference 包装，
     * 后续访问若已被 GC 回收则重新初始化。支持通过属性赋值更新引用。
     *
     * @param initializer 类初始化工厂函数，返回可能为 null 的 Class
     */
    class Weak(val initializer: () -> Class<*>?) {
        private var weakReference: WeakReference<Class<*>?>? = null
        operator fun getValue(thisRef: Any?, property: KProperty<*>) = weakReference?.get() ?: let {
            weakReference = WeakReference(initializer())
            weakReference?.get()
        }

        operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Class<*>) {
            weakReference = WeakReference(value)
        }
    }
}