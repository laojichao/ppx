package com.akari.ppx.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.akari.ppx.data.Const.ALIPAY_URI
import com.akari.ppx.data.Const.GIT_PAGE_URI
import com.akari.ppx.data.Const.QQ_GROUP_URI
import com.akari.ppx.data.Const.TARGET_APP_ID
import com.akari.ppx.data.Const.TELEGRAM_URI
import com.akari.ppx.ui.MainActivity
import com.akari.ppx.xp.Init.asyncCallbackClass
import com.akari.ppx.xp.Init.cellDigger
import com.akari.ppx.xp.Init.cellDiggerClass
import com.akari.ppx.xp.Init.cellDisserClass
import com.akari.ppx.xp.Init.cl
import com.akari.ppx.xp.Init.ctx
import com.akari.ppx.xp.Init.godCommentDiggerClass
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.text.FieldPosition
import java.text.SimpleDateFormat
import java.util.*

/**
 * 显示标准 Toast 提示（使用 Xposed 上下文）。
 * @param text 提示文本
 */
fun showToast(text: String) {
    Toast.makeText(ctx, text, Toast.LENGTH_LONG).show()
}

/**
 * 通过目标应用的 ToastManager 显示系统级 Toast。
 * @param text 提示文本
 */
fun showSystemToast(text: String) {
    "com.sup.android.uikit.base.ToastManager".findClass(cl)
        .callStaticMethod("showSystemToast", ctx, text, 0, 0)
}

/**
 * 显示粘性 Toast（常驻显示，指定时长后消失）。
 * @param text 提示文本
 * @param duration 显示时长（毫秒），默认 3000
 */
fun showStickyToast(text: String, duration: Int = 3000) {
    "com.sup.android.uikit.base.ToastManager".findClass(cl)
        .callStaticMethod("showStickyToast", text, duration)
}

/**
 * 通过目标应用的 UIBaseDialogBuilder 显示对话框。
 * @param context Activity 上下文
 * @param title 对话框标题
 * @param message 对话框内容
 * @param positiveText 确认按钮文本
 * @param onPositiveClickListener 确认按钮点击监听
 * @param negativeText 取消按钮文本
 * @param onNegativeClickListener 取消按钮点击监听
 */
fun showDialog(
    context: Activity,
    title: String,
    message: String,
    positiveText: String? = null,
    onPositiveClickListener: View.OnClickListener? = null,
    negativeText: String? = null,
    onNegativeClickListener: View.OnClickListener? = null
) {
    var builder = "com.sup.android.uikit.base.UIBaseDialogBuilder".findClass(cl).new(context)
        .callMethod("setTitle", title)
        ?.callMethod("setMessage", message)
        ?.callMethod("setOnPositiveClickListener", onPositiveClickListener)
        ?.callMethod("setOnNegativeClickListener", onNegativeClickListener)
    positiveText?.run { builder?.callMethod("setPositiveText", this).also { builder = it } }
    negativeText?.run { builder?.callMethod("setNegativeText", this).also { builder = it } }
        ?.callMethod("create")
        ?.callMethod("show")
}

/**
 * 对帖子/内容执行点赞操作。
 * @param cellType 内容类型，默认 8（帖子）
 * @param cellId 内容 ID
 * @param diggStyle 点赞样式，默认 10（默认样式）
 * @param reserved 保留参数
 */
fun diggCell(cellType: Int = 8, cellId: Long, diggStyle: Int = 10, reserved: Int = 1) {
    cellDiggerClass!!.run {
        getStaticObjectField(declaredFields.find { it.type == this }?.name)
    }?.callMethod(cellDigger(), cellType, cellId, true, diggStyle, reserved)
}

/**
 * 对帖子/内容执行点踩操作。
 * @param cellType 内容类型，默认 8
 * @param cellId 内容 ID
 * @param dissStyle 点踩样式，默认 10
 * @param h 异步回调 InvocationHandler
 */
fun dissCell(cellType: Int = 8, cellId: Long, dissStyle: Int = 10, h: InvocationHandler) {
    cellDisserClass!!.run {
        getStaticObjectField(declaredFields.find { it.type == this }?.name)
    }?.callMethod(
        "b",
        cellType,
        cellId,
        true,
        dissStyle,
        Proxy.newProxyInstance(cl, arrayOf(asyncCallbackClass), h)
    )
}

/**
 * 对帖子执行插眼（收藏/关注）操作。
 * @param cellId 内容 ID
 */
fun wardCell(cellId: Long) {
    "com.sup.android.module.feed.repo.FeedCellService".findClass(cl)
        .callStaticMethod("getInst")?.callMethod("wardCell", cellId, 1, true)
}

/**
 * 对帖子执行评论操作。
 * @param cellType 内容类型，默认 8
 * @param cellId 内容 ID
 * @param commentText 评论文本
 */
fun commentCell(cellType: Int = 8, cellId: Long, commentText: String) {
    val uid = "com.bytedance.news.common.service.manager.ServiceManager".findClass(cl)
        .callStaticMethod(
            "getService",
            "com.sup.android.mi.usercenter.IUserCenterService".findClass(cl)
        )
        ?.callMethod("getMyUserInfo")?.callMethodAs<Long>("getId") ?: 0L
    "com.sup.android.module.publish.publish.PublishLooper\$enqueue$1".findClass(cl)
        .getStaticObjectField("INSTANCE")?.callMethod(
            "invoke", "com.sup.android.mi.publish.bean.CommentBean".findClass(cl).new(
                commentText,
                uid,
                cellId,
                cellType,
                0L,
                0L,
                null,
                "cell_detail",
                "input",
                false,
                0,
                false,
                false,
                -1L
            )
        )
}

/**
 * 对神评执行送神操作。
 * @param itemId 帖子 ID
 * @param commentId 评论 ID
 */
fun diggGodComment(itemId: Long, commentId: Long) {
    godCommentDiggerClass!!.run {
        getStaticObjectField(declaredFields.find { it.type == this }?.name)
    }?.callMethod("a", itemId, commentId, null)
}

/**
 * Hook SettingService.getValue 方法，拦截设置键值对的读取。
 * @param callback 回调函数，接收键名，返回替换值（返回 null 表示不替换）
 */
fun setSettingKeyValue(callback: (key: String) -> Any?) {
    "com.sup.android.social.base.settings.SettingService".hookAfterMethod(
        cl,
        "getValue",
        String::class.java,
        Object::class.java,
        "java.lang.String[]"
    ) { param ->
        callback(param.args[0] as String)?.let { param.result = it }
    }
}

/**
 * 打开 Telegram 群组，失败时执行备选操作。
 * @param context 上下文
 * @param unsatisfiedAction 启动失败时的回调
 */
fun joinTelegram(context: Context, unsatisfiedAction: () -> Unit = {}) = runCatching {
    Intent().also { Uri.parse(TELEGRAM_URI).let(it::setData) }.let(context::startActivity)
}.getOrNull() ?: run(unsatisfiedAction)

/**
 * 加入 QQ 群，失败时执行备选操作。
 * @param context 上下文
 * @param unsatisfiedAction 启动失败时的回调
 */
fun joinQQGroup(context: Context, unsatisfiedAction: () -> Unit = {}) = runCatching {
    Intent().also { Uri.parse(QQ_GROUP_URI).let(it::setData) }.let(context::startActivity)
}.getOrNull() ?: run(unsatisfiedAction)

/**
 * 在浏览器中打开 GitHub 页面，失败时执行备选操作。
 * @param context 上下文
 * @param unsatisfiedAction 打开失败时的回调
 */
fun openGitPage(context: Context, unsatisfiedAction: () -> Unit = {}) = runCatching {
    context.openBrowser(GIT_PAGE_URI)
}.getOrNull() ?: run(unsatisfiedAction)

/**
 * 打开支付宝扫码赞赏页面，失败时执行备选操作。
 * @param context 上下文
 * @param unsatisfiedAction 启动失败时的回调
 */
fun startAlipay(context: Context, unsatisfiedAction: () -> Unit = { }) = runCatching {
    Intent().also { Uri.parse(ALIPAY_URI).let(it::setData) }.let(context::startActivity)
}.getOrNull() ?: run(unsatisfiedAction)

/**
 * 启动皮皮虾（目标应用），失败时执行备选操作。
 * @param context 上下文
 * @param unsatisfiedAction 启动失败时的回调
 */
fun startPPX(context: Context, unsatisfiedAction: () -> Unit = {}) = runCatching {
    context.packageManager.getLaunchIntentForPackage(TARGET_APP_ID)?.apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }?.let(context::startActivity)
}.getOrNull() ?: run(unsatisfiedAction)

/**
 * 隐藏或显示桌面启动图标（通过禁用/启用 Activity Alias 实现）。
 * @param context 上下文
 * @param value true 为隐藏，false 为显示
 */
fun hideIcon(context: Context, value: Boolean) {
    val aliasName = ComponentName(context, MainActivity::class.java.name + "Alias")
    when (value) {
        true -> PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        false -> PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }.checkUnless(context.packageManager.getComponentEnabledSetting(aliasName)) {
        context.packageManager.setComponentEnabledSetting(
            aliasName,
            this,
            PackageManager.DONT_KILL_APP
        )
    }
}

/**
 * 条件检查：当值等于 [other] 时执行 [satisfiedAction]。
 * @param T 值类型
 * @param R 返回类型
 * @param other 比较目标值
 * @param satisfiedAction 条件满足时的执行体
 * @return 满足条件时返回执行结果，否则返回 null
 */
inline fun <T, R> T.check(other: T, satisfiedAction: T.() -> R?): R? =
    takeIf { it == other }?.run(satisfiedAction)

/**
 * 条件检查：当 [predicate] 返回 true 时执行 [satisfiedAction]。
 * @param T 值类型
 * @param R 返回类型
 * @param predicate 谓词函数
 * @param satisfiedAction 条件满足时的执行体
 * @return 满足条件时返回执行结果，否则返回 null
 */
inline fun <T, R> T.checkIf(predicate: T.() -> Boolean, satisfiedAction: T.() -> R?): R? =
    takeIf(predicate)?.run(satisfiedAction)

/**
 * 反向条件检查：当值不等于 [other] 时执行 [unsatisfiedAction]。
 * @param T 值类型
 * @param R 返回类型
 * @param other 比较目标值
 * @param unsatisfiedAction 条件不满足时的执行体
 * @return 不满足条件时返回执行结果，否则返回 null
 */
inline fun <T, R> T.checkUnless(other: T, unsatisfiedAction: T.() -> R?): R? =
    takeUnless { it == other }?.run(unsatisfiedAction)

/**
 * Context 扩展：显示短时 Toast。
 * @param text 提示文本
 */
fun Context.showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

/**
 * Context 扩展：在浏览器中打开指定 URI。
 * @param uri 要打开的 URI 字符串
 */
fun Context.openBrowser(uri: String) = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
}.let(this::startActivity)

/**
 * 按竖线（|）分割字符串，返回分割后的列表。
 * 常用于正则表达式模式的多关键词输入。
 * @return 分割后的字符串列表
 */
fun String.splitByOr() = TextUtils.SimpleStringSplitter('|').run {
    val dest = ArrayList<String>(length)
    setString(this@splitByOr)
    while (hasNext()) {
        dest += next()
    }
    dest
}

/**
 * 将 Unix 时间戳（秒）转换为格式化的日期字符串。
 * @param pattern 日期格式模式，默认 "yyyy-MM-dd HH:mm:ss"
 * @return 格式化后的日期字符串
 */
fun Long.ts2Date(pattern: String = "yyyy-MM-dd HH:mm:ss") = SimpleDateFormat(pattern, Locale.CHINA)
    .format(Date(this * 1000), StringBuffer(), FieldPosition(0)).toString()

/**
 * 计算 Unix 时间戳（秒）到今天的天数差。
 * @return 天数差（正数表示过去，0 表示今天）
 */
fun Long.getDiffDays(): Int {
    fun clearCalendar(c: Calendar, vararg fields: Int) {
        for (f in fields) c[f] = 0
    }

    val c = Calendar.getInstance()
    clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND)
    val today = c.timeInMillis
    c.timeInMillis = this * 1000
    clearCalendar(c, Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND)
    return ((today - c.timeInMillis) / 1000 / 60 / 60 / 24).toInt()
}

/** List 解构扩展：支持获取第 6 个元素 */
operator fun <T> List<T>.component6(): T = get(5)

/** List 解构扩展：支持获取第 7 个元素 */
operator fun <T> List<T>.component7(): T = get(6)

/** List 解构扩展：支持获取第 8 个元素 */
operator fun <T> List<T>.component8(): T = get(7)