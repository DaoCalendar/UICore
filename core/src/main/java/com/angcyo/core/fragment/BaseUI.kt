package com.angcyo.core.fragment

import android.view.Gravity
import android.view.View
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.angcyo.base.back
import com.angcyo.core.R
import com.angcyo.library.ex._dimen
import com.angcyo.library.ex.colorFilter
import com.angcyo.library.ex.dpi
import com.angcyo.widget.base.clickIt
import com.angcyo.widget.base.find
import com.angcyo.widget.base.loadDrawable
import com.angcyo.widget.span.span

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/12/31
 */

object BaseUI {
    var fragmentUI = FragmentUI()
}

open class FragmentUI {

    //<editor-fold desc="成员区">

    /**是否显示返回按钮的文本*/
    var showBackText: Boolean = true

    /**返回按钮的[Drawable]资源*/
    @DrawableRes
    var backIconDrawableId: Int = R.drawable.lib_back
    var backTextSize: Float = _dimen(R.dimen.text_body_size).toFloat()

    /**[BaseTitleFragment.onCreate]中触发*/
    var fragmentCreateAfter: (fragment: BaseTitleFragment, fragmentConfig: FragmentConfig) -> Unit =
        { fragment, fragmentConfig ->
            onFragmentCreateAfter(fragment, fragmentConfig)
        }

    /**[BaseTitleFragment.onCreateView]中触发*/
    var fragmentCreateViewAfter: (fragment: BaseTitleFragment) -> Unit = {
        onFragmentCreateViewAfter(it)
    }

    /**创建返回按钮
     * [com.angcyo.core.fragment.BaseTitleFragment.onCreateBackItem]*/
    var fragmentCreateBackItem: (fragment: BaseTitleFragment) -> View? = {
        onFragmentCreateBackItem(it)
    }

    //</editor-fold desc="成员区">

    //<editor-fold desc="方法区">

    open fun onFragmentCreateAfter(fragment: BaseTitleFragment, fragmentConfig: FragmentConfig) {

    }

    open fun onFragmentCreateViewAfter(fragment: BaseTitleFragment) {

    }

    open fun onFragmentCreateBackItem(fragment: BaseTitleFragment): View? {
        return fragment.leftControl()?.inflate(R.layout.lib_text_layout, false) {
            find<TextView>(R.id.lib_text_view)?.apply {
                id = R.id.lib_title_back_view
                setTextColor(fragment.fragmentConfig.titleItemTextColor)
                text = span {
                    drawable {
                        backgroundDrawable =
                            loadDrawable(backIconDrawableId).colorFilter(fragment.fragmentConfig.titleItemIconColor)
                    }
                    if (showBackText) {
                        drawable("返回") {
                            textSize = backTextSize
                            marginLeft = -8 * dpi
                            marginTop = 1 * dpi
                            textGravity = Gravity.CENTER
                        }
                    }
                }
                clickIt {
                    fragment.back()
                }
            }
        }
    }

    //</editor-fold desc="方法区">
}