package com.angcyo.download.version

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/04/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class VersionUpdateBean(
    /**需要更新的版本名称*/
    var versionName: String? = null,
    /**需要更新的版本号*/
    var versionCode: Long = 0,
    /**更新描述*/
    var versionDes: String? = null,
    /**强制更新*/
    var versionForce: Boolean = false,
    /**下载地址*/
    var versionUrl: String? = null,
    /**版本更新类型
     * >=0 普通用户版本更新
     * <0 Debug用户版本更新
     * */
    var versionType: Int = 1,

    /**需要更新的包名, 包名不匹配则不需要更新*/
    var packageList: List<String>? = null
)