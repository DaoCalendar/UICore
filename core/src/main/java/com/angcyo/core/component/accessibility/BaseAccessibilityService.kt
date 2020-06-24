package com.angcyo.core.component.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import com.angcyo.library.L

/**
 * 无障碍服务分发.
 *
 * https://developer.android.google.cn/guide/topics/ui/accessibility/service
 *
 * 1: 声明无障碍服务. (为了与 Android 4.1 及更高版本兼容，) (https://developer.android.google.cn/guide/topics/ui/accessibility/service#manifest)
 *
 * ```
 * <application>
 *   <service android:name=".MyAccessibilityService"
 *       android:enabled="true"
 *       android:exported="true"
 *       android:permission="android.permission.BIND_ACCESSIBILITY_SERVICE"
 *       android:label="@string/accessibility_service_label">
 *     <intent-filter>
 *       <action android:name="android.accessibilityservice.AccessibilityService" />
 *     </intent-filter>
 *   </service>
 * </application>
 * ```
 *
 * 2: 配置无障碍服务 (https://developer.android.google.cn/guide/topics/ui/accessibility/service#service-config)
 *
 * ```
 * <service android:name=".MyAccessibilityService">
 *   ...
 *   <meta-data
 *     android:name="android.accessibilityservice"
 *     android:resource="@xml/lib_accessibility_service_config" />
 *   </service>
 * ```
 *
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/06/23
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */

abstract class BaseAccessibilityService : AccessibilityService() {

    companion object {
        var isServiceConnected = false
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        AccessibilityHelper.log("onServiceConnected")
        isServiceConnected = true
        serviceInfo.apply {
            //可以获取window内容, getWindows
            flags = flags or AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            //服务需要所有视图
            flags = flags or AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
            serviceInfo = this
        }
    }

    /**
     * 当收到应用发过来的[AccessibilityEvent]无障碍事件
     * */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            AccessibilityHelper.log(it.toString())
            L.d(it)
        }
    }

    /**中断了反馈*/
    override fun onInterrupt() {
        AccessibilityHelper.log("onInterrupt")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        AccessibilityHelper.log("onUnbind")
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        AccessibilityHelper.log("onDestroy")
        isServiceConnected = false
    }

    override fun getWindows(): MutableList<AccessibilityWindowInfo> {
        return super.getWindows()
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        return super.onKeyEvent(event)
    }

    override fun getRootInActiveWindow(): AccessibilityNodeInfo? {
        return super.getRootInActiveWindow()
    }

    override fun findFocus(focus: Int): AccessibilityNodeInfo? {
        return super.findFocus(focus)
    }

    override fun onGesture(gestureId: Int): Boolean {
        return super.onGesture(gestureId)
    }

    override fun getSystemService(name: String): Any? {
        return super.getSystemService(name)
    }
}