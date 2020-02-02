package com.angcyo.library.ex

import android.content.Context
import android.net.Uri
import java.io.InputStream

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/03
 */
fun Uri.inputStream(context: Context): InputStream? {
    return context.contentResolver.openInputStream(this)
}

fun <R> Uri.use(context: Context, block: (InputStream) -> R): R? {
    return inputStream(context)?.use(block)
}