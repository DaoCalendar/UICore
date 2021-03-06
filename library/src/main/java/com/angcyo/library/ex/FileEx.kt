package com.angcyo.library.ex

import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.TextUtils
import android.text.format.Formatter
import com.angcyo.library.app
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.channels.FileChannel
import java.security.DigestInputStream
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

/**
 * /kotlin/io/FileReadWrite.kt
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/22
 */

enum class SizeUnit {
    Byte, KB, MB, GB, TB, PB, Auto
}

/**文件是否存在*/
fun String?.isFileExist(): Boolean {
    return try {
        if (this.isNullOrBlank()) {
            return false
        }
        val file = File(this)
        file.exists() && file.canRead()
    } catch (e: Exception) {
        false
    }
}

/**将文件内容, 转移到另一个文件*/
fun String.transferToFile(filePath: String) {
    if (this.equals(filePath, ignoreCase = true)) {
        return
    }
    var outputChannel: FileChannel? = null
    var inputChannel: FileChannel? = null
    try {
        inputChannel = FileInputStream(File(this)).channel
        outputChannel = FileOutputStream(File(filePath)).channel
        inputChannel.transferTo(0, inputChannel.size(), outputChannel)
        inputChannel.close()
    } finally {
        inputChannel?.close()
        outputChannel?.close()
    }
}

/**获取文件md5值*/
fun File.md5(): String? {
    return getFileMD5()?.toHexString()
}

fun File.copyTo(path: String) {
    copyTo(File(path), true)
}

fun String.file(): File? {
    return try {
        File(this)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun String?.fileSize(): Long {
    if (TextUtils.isEmpty(this)) {
        return 0L
    }
    val file = this?.file()
    return if (file?.exists() == true) {
        file.length()
    } else {
        0L
    }
}

/**格式化文件大小, 根据系统版本号选择实现方式*/
fun formatFileSize(context: Context, size: Long): String {
    return if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        size.fileSizeString()
    } else Formatter.formatFileSize(context, size)
}

fun String?.fileSizeString(): String {
    return fileSize().fileSizeString()
}

/**b*/
fun Long.fileSizeString(unit: SizeUnit = SizeUnit.Auto): String {
    val size = this
    var _unit = unit
    if (size < 0) {
        return ""
    }
    val KB = 1024.0
    val MB = KB * 1024
    val GB = MB * 1024
    val TB = GB * 1024
    val PB = TB * 1024
    if (_unit == SizeUnit.Auto) {
        _unit = if (size < KB) {
            SizeUnit.Byte
        } else if (size < MB) {
            SizeUnit.KB
        } else if (size < GB) {
            SizeUnit.MB
        } else if (size < TB) {
            SizeUnit.GB
        } else if (size < PB) {
            SizeUnit.TB
        } else {
            SizeUnit.PB
        }
    }
    return when (_unit) {
        SizeUnit.Byte -> size.toString() + "B"
        SizeUnit.KB -> String.format(Locale.US, "%.2fKB", size / KB)
        SizeUnit.MB -> String.format(Locale.US, "%.2fMB", size / MB)
        SizeUnit.GB -> String.format(Locale.US, "%.2fGB", size / GB)
        SizeUnit.TB -> String.format(Locale.US, "%.2fTB", size / TB)
        SizeUnit.PB -> String.format(Locale.US, "%.2fPB", size / PB)
        else -> size.toString() + "B"
    }
}

/**
 * 获取文件的MD5校验码
 *
 * @param file 文件
 * @return 文件的MD5校验码
 */
fun File.getFileMD5(): ByteArray? {
    var dis: DigestInputStream? = null
    try {
        val fis = FileInputStream(this)
        var md = MessageDigest.getInstance("MD5")
        dis = DigestInputStream(fis, md)
        val buffer = ByteArray(1024 * 256)
        while (dis.read(buffer) > 0);
        md = dis.messageDigest
        return md.digest()
    } catch (e: NoSuchAlgorithmException) {
        e.printStackTrace()
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        dis?.close()
    }
    return null
}

/**打开文件*/
fun File.open(context: Context = app()) {
    val intent = Intent()
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    //设置intent的Action属性
    intent.action = Intent.ACTION_VIEW
    //获取文件file的MIME类型
    val type: String = this.absolutePath.mimeType() ?: "text/plain"
    //设置intent的data和Type属性。
    intent.setDataAndType(fileUri(context, this), type)
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    //跳转
    try {
        //这里最好try一下，有可能会报错。
        // 比如说你的MIME类型是打开邮箱，但是你手机里面没装邮箱客户端，就会报错。
        context.startActivity(intent)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
}

//    public static void shareBitmap(Context context, byte[] data, boolean shareQQ) {
//        Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);//设置分享行为
//        intent.setType("image/*");//设置分享内容的类型
//        intent.putExtra(Intent.EXTRA_STREAM, data);
//        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//
//        List<ResolveInfo> resolveInfos = context.getPackageManager().queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);
//
//        if (shareQQ) {
//            configQQIntent(intent);
//        } else {
//            intent = Intent.createChooser(intent, "分享图片");
//        }
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
//    }
/** 分享文件 */
fun File.shareFile(context: Context = app(), fileProvider: Boolean = true) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_STREAM, fileUri(context, this, fileProvider))
    share.type = name.mimeType() ?: "*/*" //此处可发送多种文件
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    if (fileProvider) {
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        context.startActivity(
            Intent.createChooser(
                share,
                "发送给..."
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**分享图片文件*/
fun File.shareImage(context: Context = app(), content: String?) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_TEXT, content)
    share.putExtra(Intent.EXTRA_STREAM, fileUri(context, this))
    share.type = "image/*"
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(share, "发送给...")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

/**分享视频文件*/
fun File.shareVideo(context: Context = app(), content: String?) {
    val share = Intent(Intent.ACTION_SEND)
    share.putExtra(Intent.EXTRA_TEXT, content)
    share.putExtra(Intent.EXTRA_STREAM, fileUri(context, this))
    share.type = "video/*"
    share.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    context.startActivity(
        Intent.createChooser(share, "发送给...")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    )
}

fun File?.isFile(): Boolean = this?.isFile == true

fun File?.isFolder(): Boolean = this?.isDirectory == true

fun File?.readText() = try {
    this?.readText(Charsets.UTF_8)
} catch (e: Exception) {
    e.printStackTrace()
    null
}