package com.angcyo.behavior

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.angcyo.widget.base.isTouchDown
import com.angcyo.widget.base.isTouchFinish

/**
 * 支持[GestureDetector]的处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/20
 */
abstract class BaseGestureBehavior<T : View>(
    context: Context,
    attributeSet: AttributeSet? = null
) : BaseScrollBehavior<T>(context, attributeSet), Runnable {

    /**touch scroll 阈值*/
    var touchSlop = 0

    /**是否开启touch捕捉*/
    var enableGesture = true

    //手势检测
    val _gestureDetector: GestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return this@BaseGestureBehavior.onFling(e1, e2, velocityX, velocityY)
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                return this@BaseGestureBehavior.onScroll(e1, e2, distanceX, distanceY)
            }
        })
    }

    /**手势捕捉*/
    override fun onInterceptTouchEvent(
        parent: CoordinatorLayout,
        child: T,
        ev: MotionEvent
    ): Boolean {
        return super.onInterceptTouchEvent(parent, child, ev) || handleTouchEvent(parent, child, ev)
    }

    /**手势捕捉*/
    override fun onTouchEvent(parent: CoordinatorLayout, child: T, ev: MotionEvent): Boolean {
        return super.onTouchEvent(parent, child, ev) || handleTouchEvent(parent, child, ev)
    }

    /**统一手势处理*/
    open fun handleTouchEvent(parent: CoordinatorLayout, child: T, ev: MotionEvent): Boolean {
        var result = false
        if (enableGesture) {
            result = _gestureDetector.onTouchEvent(ev)
        }
        if (ev.isTouchFinish()) {
            parent.requestDisallowInterceptTouchEvent(false)
            onTouchFinish(parent, child, ev)
        } else if (ev.isTouchDown()) {
            onTouchDown(parent, child, ev)
        }
        return result
    }

    open fun onTouchDown(parent: CoordinatorLayout, child: T, ev: MotionEvent) {

    }

    open fun onTouchFinish(parent: CoordinatorLayout, child: T, ev: MotionEvent) {

    }

    /**手势Fling处理*/
    open fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        return false
    }

    /**手势Scroll处理*/
    open fun onScroll(
        e1: MotionEvent?,
        e2: MotionEvent?,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        return false
    }

    override fun run() {

    }
}