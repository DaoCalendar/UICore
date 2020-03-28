package com.angcyo.library.ex

import com.angcyo.library.BuildConfig
import java.io.BufferedWriter
import java.io.PrintWriter
import java.io.StringWriter

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/19
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

fun isRelease(): Boolean = "release".equals(BuildConfig.BUILD_TYPE, true)

fun isDebugType() = "debug".equals(BuildConfig.BUILD_TYPE, true)

fun isDebug() = BuildConfig.DEBUG

fun Float.abs() = kotlin.math.abs(this)

fun Int.abs() = kotlin.math.abs(this)

fun Any?.hash(): String? {
    return this?.hashCode()?.run { Integer.toHexString(this) }
}

fun Any.simpleHash(): String {
    return "${this.javaClass.simpleName}(${this.hash()})"
}

fun Any.simpleClassName(): String {
    return this.javaClass.simpleName
}

fun Any.className(): String {
    return this.javaClass.name
}

/**如果为空, 则执行[action].
 * 原样返回*/
fun <T> T?.elseNull(action: () -> Unit = {}): T? {
    if (this == null) {
        action()
    }
    return this
}

fun Any?.string(): CharSequence {
    return when {
        this == null -> return ""
        this is CharSequence -> this
        else -> this.toString()
    }
}

fun Any?.str(): String {
    return if (this is String) {
        this
    } else {
        this.toString()
    }
}

/**
 *
 * https://developer.android.google.cn/reference/java/util/Formatter.html#syntax
 * */
fun Any?.format(format: String): String {
    return String.format(format, this)
}

fun Throwable.string(): String {
    val stringWriter = StringWriter()
    val pw = PrintWriter(BufferedWriter(stringWriter))
    pw.use {
        printStackTrace(it)
    }
    return stringWriter.toString()
}