package com.angcyo.item

import android.graphics.drawable.Drawable
import android.view.View
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.item.style.TextStyleConfig
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.*

/**
 * 带有label的单行输入item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/23
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */
open class DslLabelEditItem : DslBaseEditItem() {

    /**编辑提示按钮, 负数会返回null对象*/
    var itemEditTipIcon: Int = R.drawable.lib_icon_edit_tip

    /**优先于属性[itemEditTipIcon]*/
    var itemEditTipDrawable: Drawable? = null

    /**右边图标点击事件, 如果设置回调. 会影响默认的事件处理*/
    var itemRightIcoClick: ((DslViewHolder, View) -> Unit)? = null

    /**右边的文本*/
    var itemRightText: CharSequence? = null
        set(value) {
            field = value
            itemRightTextStyle.text = value
        }

    /**统一样式配置*/
    var itemRightTextStyle = TextStyleConfig()

    init {
        itemLayoutId = R.layout.dsl_label_edit_item
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.img(R.id.lib_right_ico_view)?.apply {
            val drawable = itemEditTipDrawable ?: loadDrawable(itemEditTipIcon)
            if (drawable == null) {
                gone()
            } else {
                visible()
                setImageDrawable(drawable)
            }

            //处理默认弹出软键盘
            if (itemRightIcoClick == null) {
                if (itemEditTextStyle.noEditModel) {
                    gone()
                } else {
                    throttleClickIt {
                        itemHolder.focus<View>(R.id.lib_edit_view)?.showSoftInput()
                    }
                }
            } else {
                throttleClickIt {
                    itemRightIcoClick?.invoke(itemHolder, it)
                }
            }

        }

        itemHolder.gone(R.id.lib_right_text_view, itemRightTextStyle.text == null)
        itemHolder.tv(R.id.lib_right_text_view)?.apply {
            itemRightTextStyle.updateStyle(this)
        }
    }
}