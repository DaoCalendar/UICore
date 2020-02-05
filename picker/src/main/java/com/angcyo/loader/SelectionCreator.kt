package com.angcyo.loader

import android.provider.MediaStore

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/02/01
 */
open class SelectionCreator {

    /**创建查询语句*/
    fun createSelection(config: LoaderConfig): String {
        val sql = buildString {
            config.getMimeTypeSelectorSelection(this)
            config.getFileSelectorSelection(this)
        }
        return sql
    }

    /**媒体选择查询语句*/
    fun LoaderConfig.getMimeTypeSelectorSelection(builder: StringBuilder) {
        builder.apply {
            val loadTypes = mutableListOf<Int>()
            if (mediaLoaderType and Config.LOADER_TYPE_IMAGE == Config.LOADER_TYPE_IMAGE) {
                loadTypes.add(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)
            }
            if (mediaLoaderType and Config.LOADER_TYPE_VIDEO == Config.LOADER_TYPE_VIDEO) {
                loadTypes.add(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)
            }
            if (mediaLoaderType and Config.LOADER_TYPE_AUDIO == Config.LOADER_TYPE_AUDIO) {
                loadTypes.add(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO)
            }

            if (loadTypes.isEmpty()) {
                append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                append("!=")
                append(MediaStore.Files.FileColumns.MEDIA_TYPE_NONE)
            } else {
                loadTypes.forEachIndexed { index, type ->
                    append(MediaStore.Files.FileColumns.MEDIA_TYPE)
                    append("=")
                    append(type)
                    if (loadTypes.lastIndex != index) {
                        append(" OR ")
                    }
                }
            }
        }
    }

    /**媒体大小选择条件语句*/
    fun LoaderConfig.getFileSelectorSelection(builder: StringBuilder) {
        builder.apply {
            if (limitFileSizeModel == Config.SIZE_MODEL_MEDIA) {
                if (limitFileMinSize > 0f) {
                    append(" AND ")
                    append(MediaStore.Files.FileColumns.SIZE)
                    append(">=")
                    append(limitFileMinSize)
                }

                if (limitFileMaxSize > 0f) {
                    append(" AND ")
                    append(MediaStore.Files.FileColumns.SIZE)
                    append("<=")
                    append(limitFileMaxSize)
                }
            } else {
                append(" AND ")
                append(MediaStore.Files.FileColumns.SIZE)
                append(">0")
            }
        }
    }
}