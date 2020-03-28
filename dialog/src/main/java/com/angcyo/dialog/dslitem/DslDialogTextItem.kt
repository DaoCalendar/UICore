package com.angcyo.dialog.dslitem

import android.graphics.drawable.Drawable
import android.view.Gravity
import com.angcyo.dialog.R
import com.angcyo.dsladapter.DslAdapterItem
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.base.setBoldText
import com.angcyo.widget.base.setLeftIco

/**
 * Dialog中的文本item
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

open class DslDialogTextItem : DslAdapterItem() {

    /**支持span*/
    var itemText: CharSequence? = null

    /**是否是粗体*/
    var itemTextBold: Boolean = false

    /**重力*/
    var itemTextGravity: Int = Gravity.CENTER

    /**使用系统的[drawableLeft]属性*/
    var itemLeftDrawable: Drawable? = null

    init {
        itemLayoutId = R.layout.item_dialog_text
    }

    override fun onItemBind(
        itemHolder: DslViewHolder,
        itemPosition: Int,
        adapterItem: DslAdapterItem,
        payloads: List<Any>
    ) {
        super.onItemBind(itemHolder, itemPosition, adapterItem, payloads)

        itemHolder.itemView.isSelected = itemIsSelected

        itemHolder.visible(R.id.lib_image_tip_view, itemIsSelected)

        itemHolder.tv(R.id.lib_text_view)?.apply {
            setLeftIco(itemLeftDrawable)
            gravity = itemTextGravity
            setBoldText(itemTextBold)
            text = itemText
        }
    }
}