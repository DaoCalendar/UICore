package com.angcyo.viewmodel

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStoreOwner
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/01/31
 */

/**强制使用[activity]的[ViewModelStore]*/
class VMAProperty<VM : ViewModel>(val vmCls: Class<VM>) : ReadOnlyProperty<Fragment, VM> {
    override fun getValue(thisRef: Fragment, property: KProperty<*>): VM {
        return thisRef.ofa().get(vmCls)
    }
}

class VMProperty<VM : ViewModel>(val vmCls: Class<VM>) : ReadOnlyProperty<ViewModelStoreOwner, VM> {
    override fun getValue(thisRef: ViewModelStoreOwner, property: KProperty<*>): VM {
        return thisRef.of().get(vmCls)
    }
}

/**
 * 优先使用[application]的[ViewModelStore]
 * 其次使用[activity]
 * 再次[Fragment]
 * 最后[ViewModelStoreOwner]
 * */
class VMCoreProperty<VM : ViewModel>(val vmCls: Class<VM>) :
    ReadOnlyProperty<ViewModelStoreOwner, VM> {
    override fun getValue(thisRef: ViewModelStoreOwner, property: KProperty<*>): VM {
        return if (thisRef is Fragment) {
            val activity = thisRef.requireActivity()
            val application = activity.application
            if (application is ViewModelStoreOwner) {
                application.of()
            } else {
                activity.of()
            }
        } else {
            thisRef.of()
        }.get(vmCls)
    }
}