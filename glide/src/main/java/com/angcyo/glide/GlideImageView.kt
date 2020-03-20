package com.angcyo.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import com.angcyo.library.L
import com.angcyo.library.ex.simpleHash
import com.angcyo.widget.image.DslImageView
import com.bumptech.glide.load.resource.gif.GifDrawable

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/20
 */

open class GlideImageView : DslImageView {

    val dslGlide: DslGlide by lazy {
        DslGlide().apply {
            targetView = this@GlideImageView
        }
    }

    constructor(context: Context) : super(context) {
        initAttribute(context, null)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initAttribute(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initAttribute(context, attrs)
    }

    private fun initAttribute(context: Context, attributeSet: AttributeSet?) {
        val typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.GlideImageView)
        drawBorder = typedArray.getBoolean(R.styleable.ShapeImageView_r_draw_border, true)
        typedArray.recycle()
        dslGlide.placeholderDrawable = drawable
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)

        drawable?.apply {
            L.d("${this@GlideImageView.simpleHash()}:${this.simpleHash()} w:$minimumWidth:$measuredWidth h:$minimumHeight:$minimumHeight")
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        if (drawable is GifDrawable) {
            (drawable as GifDrawable).recycle()
            setImageDrawable(null)
        }
        if (drawable is pl.droidsonroids.gif.GifDrawable) {
            (drawable as GifDrawable).recycle()
            setImageDrawable(null)
        }
    }

    //<editor-fold desc="操作">

    open fun load(url: String?, action: DslGlide.() -> Unit = {}) {
        dslGlide.apply {
            action()
            load(url)
        }
    }

    open fun load(uri: Uri?, action: DslGlide.() -> Unit = {}) {
        dslGlide.apply {
            action()
            load(uri)
        }
    }

    //</editor-fold desc="操作">
}