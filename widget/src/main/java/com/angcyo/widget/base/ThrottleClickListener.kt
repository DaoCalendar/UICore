package com.angcyo.widget.base

import android.view.View

/**
 * 节流点击事件回调
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/06
 */
open class ThrottleClickListener(
    val throttle: (lastTime: Long, nowTime: Long, view: View) -> Boolean = { lastTime, nowTime, _ ->
        (nowTime - lastTime) < 300
    },
    val action: (View) -> Unit = {

    }
) : View.OnClickListener {

    companion object {
        var _lastThrottleClickTime = 0L
    }

    var _lastClickTime = 0L
    override fun onClick(v: View) {
        val nowTime = System.currentTimeMillis()

        if (!throttle(_lastClickTime, nowTime, v)) {
            action(v)
            _lastClickTime = nowTime
        }
    }
}

/**全局节流事件处理*/
fun throttleClick(interval: Long = 300, action: () -> Unit) {
    val nowTime = System.currentTimeMillis()
    if (nowTime - ThrottleClickListener._lastThrottleClickTime > interval) {
        ThrottleClickListener._lastThrottleClickTime = nowTime
        action()
    }
}