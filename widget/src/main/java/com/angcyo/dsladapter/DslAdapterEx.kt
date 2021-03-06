package com.angcyo.dsladapter

import android.graphics.Color
import androidx.annotation.LayoutRes
import com.angcyo.library.ex.dpi
import com.angcyo.widget.DslViewHolder
import com.angcyo.widget.R
import com.angcyo.widget.base.Anim
import com.angcyo.widget.base.setWidthHeight


/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2019/08/09
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

//<editor-fold desc="Item操作">

/**
 * 通过条件, 查找[DslAdapterItem].
 *
 * @param useFilterList 是否使用过滤后的数据源. 通常界面上显示的是过滤后的数据, 所有add的数据源在非过滤列表中
 * */
fun DslAdapter.findItem(
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return getDataList(useFilterList).find(predicate)
}

fun DslAdapter.updateItem(
    payload: Any? = DslAdapterItem.PAYLOAD_UPDATE_PART,
    useFilterList: Boolean = true,
    predicate: (DslAdapterItem) -> Boolean
): DslAdapterItem? {
    return findItem(useFilterList, predicate)?.apply {
        updateAdapterItem(payload, useFilterList)
    }
}

fun DslAdapter.findItemByTag(
    tag: String?,
    useFilterList: Boolean = true
): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return findItem(useFilterList) {
        it.itemTag == tag
    }
}

fun DslAdapter.findItemByGroup(
    groups: List<String>,
    useFilterList: Boolean = true
): List<DslAdapterItem> {
    return getDataList(useFilterList).findItemByGroup(groups)
}

/**通过Tag查找item*/
fun List<DslAdapterItem>.findItemByTag(tag: String?): DslAdapterItem? {
    if (tag == null) {
        return null
    }
    return find {
        it.itemTag == tag
    }
}

/**通过group查找item*/
fun List<DslAdapterItem>.findItemByGroup(groups: List<String>): List<DslAdapterItem> {
    val result = mutableListOf<DslAdapterItem>()

    groups.forEach { group ->
        forEach {
            if (it.itemGroups.contains(group)) {
                result.add(it)
            }
        }
    }
    return result
}

/**返回[position]对应的item集合.[dataItems] [headerItems] [footerItems]*/
fun DslAdapter.getItemListByPosition(position: Int): List<DslAdapterItem>? {
    return when {
        position in headerItems.indices -> headerItems
        position - headerItems.size in dataItems.indices -> dataItems
        position - headerItems.size - dataItems.size in footerItems.indices -> footerItems
        else -> null
    }
}

fun DslAdapter.getItemListByItem(item: DslAdapterItem?): List<DslAdapterItem>? {
    return when {
        item == null -> null
        headerItems.contains(item) -> headerItems
        dataItems.contains(item) -> dataItems
        footerItems.contains(item) -> footerItems
        else -> null
    }
}

/**返回对应的集合, 和在集合中的索引*/
fun DslAdapter.getItemListPairByPosition(position: Int): Pair<MutableList<DslAdapterItem>?, Int> {
    val hSize = headerItems.size
    val dSize = dataItems.size
    return when {
        position in headerItems.indices -> headerItems to position
        position - hSize in dataItems.indices -> dataItems to (position - hSize)
        position - hSize - dSize in footerItems.indices -> footerItems to (position - hSize - dSize)
        else -> null to -1
    }
}

fun DslAdapter.getItemListPairByItem(item: DslAdapterItem?): Pair<MutableList<DslAdapterItem>?, Int> {
    return when {
        item == null -> null to -1
        headerItems.contains(item) -> headerItems to headerItems.indexOf(item)
        dataItems.contains(item) -> dataItems to dataItems.indexOf(item)
        footerItems.contains(item) -> footerItems to footerItems.indexOf(item)
        else -> null to -1
    }
}

fun DslAdapter.dslItem(@LayoutRes layoutId: Int, config: DslAdapterItem.() -> Unit = {}) {
    val item = DslAdapterItem()
    item.itemLayoutId = layoutId
    addLastItem(item)
    item.config()
}

fun <T : DslAdapterItem> DslAdapter.dslItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    dslCustomItem(dslItem, config)
}

fun <T : DslAdapterItem> DslAdapter.dslCustomItem(
    dslItem: T,
    config: T.() -> Unit = {}
) {
    addLastItem(dslItem)
    dslItem.config()
}

/**空的占位item*/
fun DslAdapter.renderEmptyItem(
    height: Int = 120 * dpi,
    color: Int = Color.TRANSPARENT,
    action: DslAdapterItem.() -> Unit = {}
) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemLayoutId = R.layout.lib_empty_item
    adapterItem.itemBindOverride = { itemHolder, _, _, _ ->
        itemHolder.itemView.setBackgroundColor(color)
        itemHolder.itemView.setWidthHeight(-1, height)
    }
    adapterItem.action()
    addLastItem(adapterItem)
}

/**换个贴切的名字*/
fun DslAdapter.render(action: DslAdapter.() -> Unit) {
    this.action()
}

fun DslAdapter.renderItem(count: Int = 1, init: DslAdapterItem.(index: Int) -> Unit) {
    for (i in 0 until count) {
        val adapterItem = DslAdapterItem()
        adapterItem.init(i)
        addLastItem(adapterItem)
    }
}

fun <T> DslAdapter.renderItem(data: T, init: DslAdapterItem.() -> Unit) {
    val adapterItem = DslAdapterItem()
    adapterItem.itemData = data
    adapterItem.init()
    addLastItem(adapterItem)
}

/**获取所有指定类型的数据集合*/
inline fun <reified ItemData> DslAdapter.getAllItemData(useFilterList: Boolean = true): List<ItemData> {
    val result = mutableListOf<ItemData>()
    val itemList = getDataList(useFilterList)
    for (item in itemList) {
        if (item.itemData is ItemData) {
            result.add(item.itemData as ItemData)
        }
    }
    return result
}

/**枚举所有Item*/
fun DslAdapter.eachItem(
    useFilterList: Boolean = true,
    action: (index: Int, dslAdapterItem: DslAdapterItem) -> Unit
) {
    getDataList(useFilterList).forEachIndexed(action)
}

//</editor-fold desc="Item操作">

//<editor-fold desc="payload">

/**是否包含指定的[payload]*/
fun Iterable<*>.containsPayload(any: Any): Boolean {
    var result = false
    for (payload in this) {
        result = if (payload is Iterable<*>) {
            payload.containsPayload(any)
        } else {
            payload == any
        }
        if (result) {
            break
        }
    }
    return result
}

/**是否要更新媒体, 比如:图片*/
fun Iterable<*>.isUpdateMedia(): Boolean {
    return count() <= 0 || containsPayload(DslAdapterItem.PAYLOAD_UPDATE_MEDIA)
}

/**需要更新媒体的负载*/
fun mediaPayload(): List<Int> =
    listOf(DslAdapterItem.PAYLOAD_UPDATE_PART, DslAdapterItem.PAYLOAD_UPDATE_MEDIA)

//</editor-fold desc="payload">

//<editor-fold desc="AdapterStatus">

fun DslAdapter.adapterStatus() = dslAdapterStatusItem.itemState

fun DslAdapter.isAdapterStatusLoading() =
    dslAdapterStatusItem.itemState == DslAdapterStatusItem.ADAPTER_STATUS_LOADING

fun DslAdapter.justRunFilterParams() = defaultFilterParams!!.apply {
    justRun = true
    asyncDiff = false
}

/**显示情感图[加载中]*/
fun DslAdapter.toLoading(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_LOADING, filterParams)
}

/**显示情感图[空数据]*/
fun DslAdapter.toEmpty(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY, filterParams)
}

/**显示情感图[错误]*/
fun DslAdapter.toError(filterParams: FilterParams = justRunFilterParams()) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_ERROR, filterParams)
}

/**显示情感图[正常]*/
fun DslAdapter.toNone(filterParams: FilterParams = defaultFilterParams!!) {
    setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE, filterParams)
}

fun DslAdapter.toLoadMoreError() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_ERROR)
}

/**加载更多技术*/
fun DslAdapter.toLoadMoreEnd() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
}

/**无更多*/
fun DslAdapter.toLoadNoMore() {
    setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
}

/**快速同时监听刷新/加载更多的回调*/
fun DslAdapter.onRefreshOrLoadMore(action: (itemHolder: DslViewHolder, loadMore: Boolean) -> Unit) {
    dslAdapterStatusItem.onRefresh = {
        action(it, false)
    }
    dslLoadMoreItem.onLoadMore = {
        action(it, true)
    }
}

//</editor-fold desc="AdapterStatus">

//<editor-fold desc="Update">

/**立即更新*/
fun DslAdapter.updateNow(filterParams: FilterParams = justRunFilterParams()) =
    updateItemDepend(filterParams)

/**延迟通知*/
fun DslAdapter.delayNotify(filterParams: FilterParams = FilterParams(notifyDiffDelay = Anim.ANIM_DURATION)) {
    updateItemDepend(filterParams)
}

//</editor-fold desc="Update">

