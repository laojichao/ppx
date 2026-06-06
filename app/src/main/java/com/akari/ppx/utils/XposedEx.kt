@file:Suppress("unused")

package com.akari.ppx.utils

import android.content.res.XResources
import dalvik.system.BaseDexClassLoader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XC_MethodHook.MethodHookParam
import de.robv.android.xposed.XC_MethodReplacement
import de.robv.android.xposed.XposedBridge.*
import de.robv.android.xposed.XposedHelpers.*
import de.robv.android.xposed.callbacks.XC_LayoutInflated
import java.lang.reflect.Field
import java.lang.reflect.Member
import java.util.*

/**
 * Xposed 框架扩展工具集。
 *
 * 提供对 Xposed API 的 Kotlin 扩展封装，简化 Hook 注册、方法替换、字段访问等操作。
 * 所有扩展函数均内置异常捕获，避免因反射失败导致目标应用崩溃。
 */
typealias Replacer = (MethodHookParam) -> Any?
typealias Hooker = (MethodHookParam) -> Unit

/**
 * 通过方法名和参数类型 Hook 指定类的方法。
 *
 * @param method 目标方法名
 * @param args 方法参数类型及回调（最后一个参数应为 [XC_MethodHook]）
 * @return [XC_MethodHook.Unhook] 解绑对象，失败时返回 null
 */
fun Class<*>.hookMethod(method: String?, vararg args: Any?) = try {
    findAndHookMethod(this, method, *args)
} catch (e: Throwable) {
    Log.e(e)
    null
}

/**
 * 对已获取的 [Member] 直接注册 Hook 回调。
 *
 * @param callback Hook 回调对象
 * @return [XC_MethodHook.Unhook] 解绑对象，失败时返回 null
 */
fun Member.hookMethod(callback: XC_MethodHook) = try {
    hookMethod(this, callback)
} catch (e: Throwable) {
    Log.e(e)
    null
}

/**
 * 安全执行 Hook 前/后回调，捕获异常并记录日志。
 *
 * @param hooker 回调函数
 */
inline fun MethodHookParam.callHooker(crossinline hooker: Hooker) = try {
    hooker(this)
} catch (e: Throwable) {
    Log.e(e)
}

/**
 * 安全执行方法替换回调，捕获异常并记录日志。
 *
 * @param replacer 替换函数，返回值将作为原方法的返回值
 * @return 替换结果，失败时返回 null
 */
inline fun MethodHookParam.callReplacer(crossinline replacer: Replacer) = try {
    replacer(this)
} catch (e: Throwable) {
    Log.e(e)
    null
}

/**
 * 替换 [Member] 对应的方法实现。
 *
 * @param replacer 替换函数
 */
inline fun Member.replaceMethod(crossinline replacer: Replacer) =
    hookMethod(object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam) = param.callReplacer(replacer)
    })

/**
 * 在 [Member] 方法执行之后插入回调。
 *
 * @param hooker 回调函数
 */
inline fun Member.hookAfterMethod(crossinline hooker: Hooker) =
    hookMethod(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 在 [Member] 方法执行之前插入回调。
 *
 * @param hooker 回调函数
 */
inline fun Member.hookBeforeMethod(crossinline hooker: (MethodHookParam) -> Unit) =
    hookMethod(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 通过方法名在类执行之前插入回调。
 *
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param hooker 回调函数
 */
inline fun Class<*>.hookBeforeMethod(
    method: String?,
    vararg args: Any?,
    crossinline hooker: Hooker
) = hookMethod(method, *args, object : XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
})

/**
 * 通过方法名在类方法执行之后插入回调。
 *
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param hooker 回调函数
 */
inline fun Class<*>.hookAfterMethod(
    method: String?,
    vararg args: Any?,
    crossinline hooker: Hooker
) = hookMethod(method, *args, object : XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
})

/**
 * 通过方法名替换类的方法实现。
 *
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param replacer 替换函数
 */
inline fun Class<*>.replaceMethod(
    method: String?,
    vararg args: Any?,
    crossinline replacer: Replacer
) = hookMethod(method, *args, object : XC_MethodReplacement() {
    override fun replaceHookedMethod(param: MethodHookParam) = param.callReplacer(replacer)
})

/**
 * Hook 类中所有同名方法（含重载）。
 *
 * @param methodName 目标方法名
 * @param hooker Hook 回调
 * @return 所有 [XC_MethodHook.Unhook] 的集合
 */
fun Class<*>.hookAllMethods(methodName: String?, hooker: XC_MethodHook): Set<XC_MethodHook.Unhook> =
    try {
        hookAllMethods(this, methodName, hooker)
    } catch (e: NoSuchMethodError) {
        Log.e(e)
        emptySet()
    } catch (e: ClassNotFoundError) {
        Log.e(e)
        emptySet()
    } catch (e: ClassNotFoundException) {
        Log.e(e)
        emptySet()
    }

/**
 * 在类中所有同名方法执行之前插入回调。
 *
 * @param methodName 目标方法名
 * @param hooker 回调函数
 */
inline fun Class<*>.hookBeforeAllMethods(methodName: String?, crossinline hooker: Hooker) =
    hookAllMethods(methodName, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 在类中所有同名方法执行之后插入回调。
 *
 * @param methodName 目标方法名
 * @param hooker 回调函数
 */
inline fun Class<*>.hookAfterAllMethods(methodName: String?, crossinline hooker: Hooker) =
    hookAllMethods(methodName, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) = param.callHooker(hooker)

    })

/**
 * 替换类中所有同名方法的实现。
 *
 * @param methodName 目标方法名
 * @param replacer 替换函数
 */
inline fun Class<*>.replaceAfterAllMethods(methodName: String?, crossinline replacer: Replacer) =
    hookAllMethods(methodName, object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam) = param.callReplacer(replacer)
    })

/**
 * Hook 类的构造方法。
 *
 * @param args 构造方法参数类型及回调
 * @return [XC_MethodHook.Unhook] 解绑对象，失败时返回 null
 */
fun Class<*>.hookConstructor(vararg args: Any?) = try {
    findAndHookConstructor(this, *args)
} catch (e: NoSuchMethodError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundException) {
    Log.e(e)
    null
}

/**
 * 在类构造方法执行之前插入回调。
 *
 * @param args 构造方法参数类型
 * @param hooker 回调函数
 */
inline fun Class<*>.hookBeforeConstructor(vararg args: Any?, crossinline hooker: Hooker) =
    hookConstructor(*args, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 在类构造方法执行之后插入回调。
 *
 * @param args 构造方法参数类型
 * @param hooker 回调函数
 */
inline fun Class<*>.hookAfterConstructor(vararg args: Any?, crossinline hooker: Hooker) =
    hookConstructor(*args, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 替换类的构造方法实现。
 *
 * @param args 构造方法参数类型
 * @param hooker 替换函数
 */
inline fun Class<*>.replaceConstructor(vararg args: Any?, crossinline hooker: Hooker) =
    hookConstructor(*args, object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * Hook 类的所有构造方法。
 *
 * @param hooker Hook 回调
 * @return 所有 [XC_MethodHook.Unhook] 的集合
 */
fun Class<*>.hookAllConstructors(hooker: XC_MethodHook): Set<XC_MethodHook.Unhook> = try {
    hookAllConstructors(this, hooker)
} catch (e: NoSuchMethodError) {
    Log.e(e)
    emptySet()
} catch (e: ClassNotFoundError) {
    Log.e(e)
    emptySet()
} catch (e: ClassNotFoundException) {
    Log.e(e)
    emptySet()
}

/**
 * 在类所有构造方法执行之后插入回调。
 *
 * @param hooker 回调函数
 */
inline fun Class<*>.hookAfterAllConstructors(crossinline hooker: Hooker) =
    hookAllConstructors(object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 在类所有构造方法执行之前插入回调。
 *
 * @param hooker 回调函数
 */
inline fun Class<*>.hookBeforeAllConstructors(crossinline hooker: Hooker) =
    hookAllConstructors(object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 替换类所有构造方法的实现。
 *
 * @param hooker 替换函数
 */
inline fun Class<*>.replaceAfterAllConstructors(crossinline hooker: Hooker) =
    hookAllConstructors(object : XC_MethodReplacement() {
        override fun replaceHookedMethod(param: MethodHookParam) = param.callHooker(hooker)
    })

/**
 * 通过类名字符串 Hook 方法。
 *
 * @param classLoader 类加载器
 * @param method 目标方法名
 * @param args 方法参数类型及回调
 * @return [XC_MethodHook.Unhook] 解绑对象，失败时返回 null
 */
fun String.hookMethod(classLoader: ClassLoader, method: String?, vararg args: Any?) = try {
    findClass(classLoader).hookMethod(method, *args)
} catch (e: ClassNotFoundError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundException) {
    Log.e(e)
    null
}

/**
 * 通过类名字符串在方法执行之前插入回调。
 *
 * @param classLoader 类加载器
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param hooker 回调函数
 */
inline fun String.hookBeforeMethod(
    classLoader: ClassLoader,
    method: String?,
    vararg args: Any?,
    crossinline hooker: Hooker
) = try {
    findClass(classLoader).hookBeforeMethod(method, *args, hooker = hooker)
} catch (e: ClassNotFoundError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundException) {
    Log.e(e)
    null
}

/**
 * 通过类名字符串在方法执行之后插入回调。
 *
 * @param classLoader 类加载器
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param hooker 回调函数
 */
inline fun String.hookAfterMethod(
    classLoader: ClassLoader,
    method: String?,
    vararg args: Any?,
    crossinline hooker: Hooker
) = try {
    findClass(classLoader).hookAfterMethod(method, *args, hooker = hooker)
} catch (e: ClassNotFoundError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundException) {
    Log.e(e)
    null
}

/**
 * 通过类名字符串替换方法实现。
 *
 * @param classLoader 类加载器
 * @param method 目标方法名
 * @param args 方法参数类型
 * @param replacer 替换函数
 */
inline fun String.replaceMethod(
    classLoader: ClassLoader,
    method: String?,
    vararg args: Any?,
    crossinline replacer: Replacer
) = try {
    findClass(classLoader).replaceMethod(method, *args, replacer = replacer)
} catch (e: ClassNotFoundError) {
    Log.e(e)
    null
} catch (e: ClassNotFoundException) {
    Log.e(e)
    null
}

/** 调用被 Hook 方法的原始实现。 */
fun MethodHookParam.invokeOriginalMethod(): Any? = invokeOriginalMethod(method, thisObject, args)

/**
 * 安全执行代码块，捕获异常时返回 null。
 *
 * @param func 要执行的代码块
 * @return 执行结果，异常时返回 null
 */
inline fun <T, R> T.runCatchingOrNull(func: T.() -> R?) = try {
    func()
} catch (e: Throwable) {
    null
}

/** 获取对象的指定字段值。 */
fun Any.getObjectField(field: String?): Any? = getObjectField(this, field)

/** 安全获取对象的指定字段值，失败时返回 null。 */
fun Any.getObjectFieldOrNull(field: String?): Any? = runCatchingOrNull {
    getObjectField(this, field)
}

/** 获取对象的指定字段值并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getObjectFieldAs(field: String?) = getObjectField(this, field) as T

/** 安全获取对象的指定字段值并强制转换为 [T]，失败时返回 null。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getObjectFieldOrNullAs(field: String?) = runCatchingOrNull {
    getObjectField(this, field) as T
}

/** 获取对象的 Int 类型字段值。 */
fun Any.getIntField(field: String?) = getIntField(this, field)

/** 安全获取对象的 Int 类型字段值，失败时返回 null。 */
fun Any.getIntFieldOrNull(field: String?) = runCatchingOrNull {
    getIntField(this, field)
}

/** 获取对象的 Long 类型字段值。 */
fun Any.getLongField(field: String?) = getLongField(this, field)

/** 安全获取对象的 Long 类型字段值，失败时返回 null。 */
fun Any.getLongFieldOrNull(field: String?) = runCatchingOrNull {
    getLongField(this, field)
}

/** 获取对象的 Boolean 类型字段值。 */
fun Any.getBooleanField(field: String?) = getBooleanField(this, field)

/** 安全获取对象的 Boolean 类型字段值，失败时返回 null。 */
fun Any.getBooleanFieldOrNull(field: String?) = runCatchingOrNull {
    getBooleanField(this, field)
}

/** 调用对象的指定方法。 */
fun Any.callMethod(methodName: String?, vararg args: Any?): Any? =
    callMethod(this, methodName, *args)

/** 安全调用对象的指定方法，失败时返回 null。 */
fun Any.callMethodOrNull(methodName: String?, vararg args: Any?): Any? = runCatchingOrNull {
    callMethod(this, methodName, *args)
}

/** 调用类的静态方法。 */
fun Class<*>.callStaticMethod(methodName: String?, vararg args: Any?): Any? =
    callStaticMethod(this, methodName, *args)

/** 安全调用类的静态方法，失败时返回 null。 */
fun Class<*>.callStaticMethodOrNull(methodName: String?, vararg args: Any?): Any? =
    runCatchingOrNull {
        callStaticMethod(this, methodName, *args)
    }

/** 调用类的静态方法并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.callStaticMethodAs(methodName: String?, vararg args: Any?) =
    callStaticMethod(this, methodName, *args) as T

/** 安全调用类的静态方法并强制转换为 [T]，失败时返回 null。 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.callStaticMethodOrNullAs(methodName: String?, vararg args: Any?) =
    runCatchingOrNull {
        callStaticMethod(this, methodName, *args) as T
    }

/** 获取类的静态对象字段并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.getStaticObjectFieldAs(field: String?) = getStaticObjectField(this, field) as T

/** 安全获取类的静态对象字段并强制转换为 [T]，失败时返回 null。 */
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>.getStaticObjectFieldOrNullAs(field: String?) = runCatchingOrNull {
    getStaticObjectField(this, field) as T
}

/** 获取类的静态对象字段值。 */
fun Class<*>.getStaticObjectField(field: String?): Any? = getStaticObjectField(this, field)

/** 安全获取类的静态对象字段值，失败时返回 null。 */
fun Class<*>.getStaticObjectFieldOrNull(field: String?): Any? = runCatchingOrNull {
    getStaticObjectField(this, field)
}

/** 设置类的静态对象字段值。 */
fun Class<*>.setStaticObjectField(field: String?, obj: Any?) = apply {
    setStaticObjectField(this, field, obj)
}

/** 安全设置类的静态对象字段值，字段不存在时静默忽略。 */
fun Class<*>.setStaticObjectFieldIfExist(field: String?, obj: Any?) = apply {
    try {
        setStaticObjectField(this, field, obj)
    } catch (ignored: Throwable) {
    }
}

/** 通过精确类型查找类中的第一个字段（泛型版本）。 */
inline fun <reified T> Class<*>.findFieldByExactType(): Field? =
    findFirstFieldByExactType(this, T::class.java)

/** 通过精确类型查找类中的第一个字段。 */
fun Class<*>.findFieldByExactType(type: Class<*>): Field? =
    findFirstFieldByExactType(this, type)

/** 调用对象的指定方法并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.callMethodAs(methodName: String?, vararg args: Any?) =
    callMethod(this, methodName, *args) as T

/** 安全调用对象的指定方法并强制转换为 [T]，失败时返回 null。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.callMethodOrNullAs(methodName: String?, vararg args: Any?) = runCatchingOrNull {
    callMethod(this, methodName, *args) as T
}

/** 通过参数类型数组调用对象的指定方法。 */
fun Any.callMethod(methodName: String?, parameterTypes: Array<Class<*>>, vararg args: Any?): Any? =
    callMethod(this, methodName, parameterTypes, *args)

/** 安全通过参数类型数组调用对象的指定方法，失败时返回 null。 */
fun Any.callMethodOrNull(
    methodName: String?,
    parameterTypes: Array<Class<*>>,
    vararg args: Any?
): Any? = runCatchingOrNull {
    callMethod(this, methodName, parameterTypes, *args)
}

/** 通过参数类型数组调用类的静态方法。 */
fun Class<*>.callStaticMethod(
    vararg args: Any?
): Any? = callStaticMethod(this, methodName, parameterTypes, *args)

/** 安全通过参数类型数组调用类的静态方法，失败时返回 null。 */
fun Class<*>.callStaticMethodOrNull(
    vararg args: Any?
): Any? = runCatchingOrNull {
    callStaticMethod(this, methodName, parameterTypes, *args)
}

/** 通过类名字符串查找类。 */
fun String.findClass(classLoader: ClassLoader): Class<*> = findClass(this, classLoader)

/** 创建类的实例（无参构造）。 */
fun Class<*>.new(vararg args: Any?): Any = newInstance(this, *args)

/** 通过参数类型数组创建类的实例。 */
fun Class<*>.new(parameterTypes: Array<Class<*>>, vararg args: Any?): Any =
    newInstance(this, parameterTypes, *args)

/** 查找类中的指定字段。 */
fun Class<*>.findField(field: String?): Field = findField(this, field)

/** 安全查找类中的指定字段，不存在时返回 null。 */
fun Class<*>.findFieldOrNull(field: String?): Field? = findFieldIfExists(this, field)

/** 设置对象的 Int 类型字段值。 */
fun <T> T.setIntField(field: String?, value: Int) = apply {
    setIntField(this, field, value)
}

/** 设置对象的 Long 类型字段值。 */
fun <T> T.setLongField(field: String?, value: Long) = apply {
    setLongField(this, field, value)
}

/** 设置对象的对象类型字段值。 */
fun <T> T.setObjectField(field: String?, value: Any?) = apply {
    setObjectField(this, field, value)
}

/** 设置对象的 Boolean 类型字段值。 */
fun <T> T.setBooleanField(field: String?, value: Boolean) = apply {
    setBooleanField(this, field, value)
}

/**
 * Hook 资源布局，通过资源 ID 拦截布局膨胀事件。
 *
 * @param id 资源 ID
 * @param hooker 布局膨胀回调
 */
inline fun XResources.hookLayout(
    crossinline hooker: (XC_LayoutInflated.LayoutInflatedParam) -> Unit
) {
    try {
        hookLayout(id, object : XC_LayoutInflated() {
            override fun handleLayoutInflated(liparam: LayoutInflatedParam) {
                try {
                    hooker(liparam)
                } catch (e: Throwable) {
                    Log.e(e)
                }
            }
        })
    } catch (e: Throwable) {
        Log.e(e)
    }
}

/**
 * Hook 资源布局，通过包名、类型和名称定位资源。
 *
 * @param pkg 资源包名
 * @param type 资源类型
 * @param name 资源名称
 * @param hooker 布局膨胀回调
 */
inline fun XResources.hookLayout(
    type: String,
    name: String,
    crossinline hooker: (XC_LayoutInflated.LayoutInflatedParam) -> Unit
) {
    try {
        val id = getIdentifier(name, type, pkg)
        hookLayout(id, hooker)
    } catch (e: Throwable) {
        Log.e(e)
    }
}

/** 通过精确类型查找类中的第一个字段。 */
fun Class<*>.findFirstFieldByExactType(type: Class<*>): Field =
    findFirstFieldByExactType(this, type)

/** 安全通过精确类型查找类中的第一个字段，不存在时返回 null。 */
fun Class<*>.findFirstFieldByExactTypeOrNull(type: Class<*>?): Field? = runCatchingOrNull {
    findFirstFieldByExactType(this, type)
}

/** 获取对象中第一个匹配精确类型的字段值。 */
fun Any.getFirstFieldByExactType(type: Class<*>): Any? =
    javaClass.findFirstFieldByExactType(type).get(this)

/** 获取对象中第一个匹配精确类型的字段值并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getFirstFieldByExactTypeAs(type: Class<*>) =
    javaClass.findFirstFieldByExactType(type).get(this) as? T

/** 通过泛型类型获取对象中第一个匹配精确类型的字段值。 */
inline fun <reified T : Any> Any.getFirstFieldByExactType() =
    javaClass.findFirstFieldByExactType(T::class.java).get(this) as? T

/** 安全获取对象中第一个匹配精确类型的字段值，失败时返回 null。 */
fun Any.getFirstFieldByExactTypeOrNull(type: Class<*>?): Any? = runCatchingOrNull {
    javaClass.findFirstFieldByExactTypeOrNull(type)?.get(this)
}

/** 安全获取对象中第一个匹配精确类型的字段值并强制转换为 [T]。 */
@Suppress("UNCHECKED_CAST")
fun <T> Any.getFirstFieldByExactTypeOrNullAs(type: Class<*>?) =
    getFirstFieldByExactTypeOrNull(type) as? T

/** 通过泛型类型安全获取对象中第一个匹配精确类型的字段值。 */
inline fun <reified T> Any.getFirstFieldByExactTypeOrNull() =
    getFirstFieldByExactTypeOrNull(T::class.java) as? T

/**
 * 获取 ClassLoader 中所有已加载的类名列表。
 *
 * 通过反射遍历 DexFile 中的所有类条目，用于运行时动态查找目标类。
 *
 * @param delegator ClassLoader 委托转换函数，默认直接使用
 * @return 所有已加载类的全限定名列表
 */
fun ClassLoader.allClassesList(delegator: (BaseDexClassLoader) -> BaseDexClassLoader = { x -> x }): List<String> {
    var classLoader = this
    while (classLoader !is BaseDexClassLoader) {
        if (classLoader.parent != null) classLoader = classLoader.parent
        else return emptyList()
    }
    return delegator(classLoader).getObjectField("pathList")
        ?.getObjectFieldAs<Array<Any>>("dexElements")
        ?.flatMap {
            it.getObjectField("dexFile")?.callMethodAs<Enumeration<String>>("entries")?.toList()
                .orEmpty()
        }.orEmpty()
}
