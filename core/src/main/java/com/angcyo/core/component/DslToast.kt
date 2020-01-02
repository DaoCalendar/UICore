package com.angcyo.core.component

import android.content.Context
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import com.angcyo.core.R
import com.angcyo.drawable.dpi
import com.angcyo.drawable.getDimen
import com.angcyo.library.app
import com.angcyo.library.ex.undefined_int
import com.angcyo.library.ex.undefined_res
import com.angcyo.library.getScreenHeight
import com.angcyo.library.getScreenWidth
import com.angcyo.library.getStatusBarHeight

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/02
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
object DslToast {
    var _toast: Toast? = null

    fun show(context: Context = app(), action: ToastConfig.() -> Unit) {
        val config = ToastConfig()
        config.action()

        if (_toast == null || Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            _toast = Toast.makeText(context, "", config.duration)
        }

        if (config.fullScreen) {
            initFullScreenToast(_toast!!, config.fullMargin * 2)
        }

        _toast?.apply {
            duration = config.duration

            setGravity(config.gravity, config.xOffset, config.yOffset)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                view.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }

            if (config.layoutId == undefined_int) {
                //没有自定义的布局
                setText(config.text)
            } else {
                val layout = LayoutInflater.from(context)
                    .inflate(config.layoutId, FrameLayout(context), false)

                layout.findViewById<TextView>(R.id.lib_text_view)?.apply {
                    if (config.text.isEmpty()) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        this.text = config.text
                    }
                }
                layout.findViewById<ImageView>(R.id.lib_image_view)?.apply {
                    if (config.icon == undefined_res) {
                        visibility = View.GONE
                    } else {
                        visibility = View.VISIBLE
                        setImageResource(config.icon)
                    }
                }

                config.onBindView(layout)

                view = layout
            }
        }

        _toast?.show()
    }

    fun initFullScreenToast(toast: Toast, usedWidth: Int) {
        try {
            val mTN = toast::class.java.getDeclaredField("mTN")
            mTN.isAccessible = true
            val mTNObj = mTN.get(toast)

            val mParams = mTNObj.javaClass.getDeclaredField("mParams")
            mParams.isAccessible = true
            val params = mParams.get(mTNObj) as WindowManager.LayoutParams
            params.width = getScreenWidth().coerceAtMost(getScreenHeight()) - usedWidth
            params.height = -2
            //params.gravity = Gravity.TOP//无法生效, 请在Toast对象里面设置
            params.windowAnimations = R.style.LibToastAnimation
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

data class ToastConfig(
    var duration: Int = Toast.LENGTH_LONG,
    var text: CharSequence = "",
    @DrawableRes
    var icon: Int = undefined_res,
    var layoutId: Int = undefined_int,
    var gravity: Int = Gravity.CENTER_HORIZONTAL or Gravity.TOP,
    var xOffset: Int = 0,
    var yOffset: Int = getStatusBarHeight() + getDimen(R.dimen.action_bar_height) + 10 * dpi,
    var fullScreen: Boolean = true,//全屏模式
    var fullMargin: Int = 20 * dpi,//全屏模式下, 宽度左右的margin

    var onBindView: (rootView: View) -> Unit = {}
)

fun toast(action: ToastConfig.() -> Unit) {
    DslToast.show(action = action)
}

fun toast(text: CharSequence?, @DrawableRes icon: Int = undefined_res) {
    DslToast.show {
        layoutId = R.layout.lib_toast_layout
        this.text = text ?: ""
        this.icon = icon
    }
}