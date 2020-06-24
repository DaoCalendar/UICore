package com.angcyo.dsladapter

import com.angcyo.library.ex.className
import com.angcyo.library.ex.isListEmpty
import com.angcyo.library.model.Page
import kotlin.math.max
import kotlin.math.min

/**
 *
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/03/05
 * Copyright (c) 2019 ShenZhen O&M Cloud Co., Ltd. All rights reserved.
 */

/**用于[Adapter]中单一数据类型的列表*/
inline fun <reified Item : DslAdapterItem> DslAdapter.loadSingleData(
    dataList: List<Any>?,
    page: Int = Page.FIRST_PAGE_INDEX,
    pageSize: Int = Page.PAGE_SIZE,
    filterParams: FilterParams = defaultFilterParams!!.apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    },
    crossinline initOrCreateDslItem: (oldItem: Item?, data: Any) -> Item
) {
    changeDataItems(filterParams) {
        //移除所有不同类型的item
        val removeItemList = mutableListOf<DslAdapterItem>()
        it.forEach { item ->
            if (item !is Item) {
                removeItemList.add(item)
            }
        }
        it.removeAll(removeItemList)

        //加载数据
        val list = dataList ?: emptyList()
        //第一页 数据检查
        if (page <= Page.FIRST_PAGE_INDEX) {
            if (it.size > list.size) {
                for (i in max(it.lastIndex, 0) downTo max(list.size, 0)) {
                    it.removeAt(i)
                }
            }
            //重新旧数据
            it.forEachIndexed { index, dslAdapterItem ->
                val data = list[index]
                dslAdapterItem.itemChanging = dslAdapterItem.itemData != data
                dslAdapterItem.itemData = data
                initOrCreateDslItem(dslAdapterItem as Item, data)
            }
            if (list.size > it.size) {
                //需要补充新的DslAdapterItem
                for (i in it.size until list.size) {
                    val data = list[i]
                    val dslItem = initOrCreateDslItem(null, data)
                    dslItem.itemData = data
                    it.add(dslItem)
                }
            }
            if (it.isEmpty() && headerItems.isEmpty() && footerItems.isEmpty()) {
                //空数据
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
            } else {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
                if (dslLoadMoreItem.itemStateEnable) {
                    if (it.size < pageSize) {
                        setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
                    } else {
                        setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
                    }
                }
            }
        } else {
            //第二页 追加数据检查
            for (data in list) {
                val dslItem = initOrCreateDslItem(null, data)
                dslItem.itemData = data
                it.add(dslItem)
            }
            setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
            if (dslLoadMoreItem.itemStateEnable) {
                if (list.size < pageSize) {
                    setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
                } else {
                    setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
                }
            }
        }
    }
}

inline fun <reified Item : DslAdapterItem> DslAdapter.loadSingleData2(
    dataList: List<Any>?,
    page: Int = Page.FIRST_PAGE_INDEX,
    pageSize: Int = Page.PAGE_SIZE,
    filterParams: FilterParams = defaultFilterParams!!.apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    },
    crossinline initItem: Item.(data: Any) -> Unit = {}
) {
    loadSingleData<Item>(dataList, page, pageSize, filterParams) { oldItem, data ->
        (oldItem ?: Item::class.java.newInstance()).apply {
            initItem(data)
        }
    }
}

/**数据更新Dsl配置项*/
class UpdateDataConfig {
    /**需要加载的页码, 会偏移到指定位置*/
    var updatePage: Int = Page.FIRST_PAGE_INDEX

    /**页面数量*/
    var pageSize: Int = Page.PAGE_SIZE

    /**需要加载的数据*/
    var updateDataList: List<Any>? = null

    /**是否一直激活加载更多, 不管第一页数据不够*/
    var alwaysEnableLoadMore: Boolean = false

    var filterParams: FilterParams = FilterParams().apply {
        payload = listOf(
            DslAdapterItem.PAYLOAD_UPDATE_PART,
            DslAdapterItem.PAYLOAD_UPDATE_MEDIA
        )
    }

    /**
     * 更新已有的item, 创建不存在的item, 移除不需要的item
     * [oldItem] 如果有值, 则希望更新[oldItem]
     * @return 返回null, 则会删除对应的[oldItem], 返回与[oldItem]不一样的item, 则会替换原来的[oldItem]
     * */
    var updateOrCreateItem: (oldItem: DslAdapterItem?, data: Any?, index: Int) -> DslAdapterItem? =
        { oldItem, data, index ->
            oldItem
        }

    /**数据计算完之后*/
    var adapterUpdateResult: (dslAdapter: DslAdapter) -> Unit = { dslAdapter ->
        with(dslAdapter) {
            if (dataItems.isEmpty() && headerItems.isEmpty() && footerItems.isEmpty()) {
                //空数据
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_EMPTY)
            } else {
                setAdapterStatus(DslAdapterStatusItem.ADAPTER_STATUS_NONE)
            }
        }
        adapterCheckLoadMore(dslAdapter)
    }

    /**加载更多检查*/
    var adapterCheckLoadMore: (dslAdapter: DslAdapter) -> Unit = { dslAdapter ->
        dslAdapter.updateLoadMore(
            updatePage,
            if (updateDataList.isListEmpty()) 0 else (updateDataList?.size ?: 0),
            pageSize,
            alwaysEnableLoadMore
        )
    }
}

/**简单的判断是否需要替换/更新[oldItem]*/
fun <Item : DslAdapterItem> updateOrCreateItemByClass(
    itemClass: Class<Item>,
    oldItem: DslAdapterItem?,
    initItem: Item.() -> Unit = { }
): DslAdapterItem? {
    var newItem = oldItem
    if (oldItem == null || oldItem.className() != itemClass.className()) {
        newItem = itemClass.newInstance()
    }
    (newItem as? Item?)?.apply {
        this.initItem()
    }
    return newItem
}

/**根据[adapterItems]的数量, 智能切换[AdapterState]*/
fun DslAdapter.updateAdapterState() {
    autoAdapterStatus()
}

/**智能设置加载更多的状态和激活状态*/
fun DslAdapter.updateLoadMore(
    updatePage: Int /*当前数据页*/,
    updateSize: Int /*数据页数据更新量*/,
    pageSize: Int = Page.PAGE_SIZE /*数据页数据最大量*/,
    alwaysEnable: Boolean = false /*是否一直激活加载更多, 不管第一页数据不够*/
) {
    if (updatePage <= Page.FIRST_PAGE_INDEX) {
        //更新第一页的数据
        if (updateSize < pageSize) {
            //数据不够, 关闭加载更多
            if (alwaysEnable) {
                setLoadMoreEnable(true)
                setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
            } else {
                setLoadMoreEnable(false)
            }
        } else {
            //激活加载更多, 初始化默认状态
            setLoadMoreEnable(true)
            setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL)
        }
    } else {
        //更新其他页数据
        if (dslLoadMoreItem.itemStateEnable) {
            if (updateSize < pageSize) {
                setLoadMore(DslLoadMoreItem.LOAD_MORE_NO_MORE)
            } else {
                setLoadMore(DslLoadMoreItem.LOAD_MORE_NORMAL, notify = false)
            }
        }
    }
}

/**轻量差异更新*/
fun UpdateDataConfig.updateData(originList: List<DslAdapterItem>): List<DslAdapterItem> {

    //最后的结果集
    val result = mutableListOf<DslAdapterItem>()

    originList.let {
        //旧数据
        val oldList = ArrayList(it)

        //新数据
        val list = updateDataList ?: emptyList()

        //需要被移除的旧数据集合
        val oldRemoveList = mutableListOf<DslAdapterItem>()
        val newAddList = mutableListOf<DslAdapterItem>()

        val updateStartIndex = max(0, updatePage - 1) * pageSize
        val updateEndIndex = updateStartIndex + min(pageSize, list.size)

        for (i in updateStartIndex until updateEndIndex) {
            val index = i - updateStartIndex
            val data = list[index]
            val oldItem = oldList.getOrNull(i)
            val newItem = updateOrCreateItem(oldItem, data, index)

            if (newItem != null) {
                newItem.itemChanging = true
                newItem.itemData = data
            }

            when {
                //remove old item
                newItem == null -> {
                    if (oldItem != null) {
                        oldRemoveList.add(oldItem)
                    }
                }
                //replace old item
                oldItem != null && oldItem != newItem -> {
                    oldList[i] = newItem
                }
                //update old item
                else -> {
                    if (oldItem == null) {
                        newAddList.add(newItem)
                    }
                }
            }
        }

        //超范围的旧数据
        for (i in updateEndIndex until oldList.size) {
            oldRemoveList.add(oldList[i])
        }

        oldList.removeAll(oldRemoveList)

        result.addAll(oldList)
        result.addAll(newAddList)
    }

    return result
}

/**支持相同类型之间的轻量差异更新*/
fun DslAdapter.updateHeaderData(action: UpdateDataConfig.() -> Unit) {
    val config = UpdateDataConfig()
    config.updatePage = Page.FIRST_PAGE_INDEX
    config.pageSize = Int.MAX_VALUE
    config.action()

    changeHeaderItems(config.filterParams) {
        val result = config.updateData(it)
        it.clear()
        it.addAll(result)
    }
}

fun DslAdapter.updateFooterData(action: UpdateDataConfig.() -> Unit) {
    val config = UpdateDataConfig()
    config.updatePage = Page.FIRST_PAGE_INDEX
    config.pageSize = Int.MAX_VALUE
    config.action()

    changeFooterItems(config.filterParams) {
        val result = config.updateData(it)
        it.clear()
        it.addAll(result)
    }
}

/**更新指定页码的数据, 支持轻量差异更新.*/
fun DslAdapter.updateData(action: UpdateDataConfig.() -> Unit) {
    val config = UpdateDataConfig()
    config.action()

    changeDataItems(config.filterParams) {
        val result = config.updateData(it)
        it.clear()
        it.addAll(result)
        config.adapterUpdateResult(this)
    }
}

/**更新单页数据*/
inline fun <reified Item : DslAdapterItem> DslAdapter.updateSingleData(
    dataList: List<Any>?,
    requestPage: Int = Page.FIRST_PAGE_INDEX,
    requestPageSize: Int = Int.MAX_VALUE,
    crossinline action: UpdateDataConfig.() -> Unit = {},
    crossinline initItem: Item.(data: Any?) -> Unit = {}
) {
    updateData {
        updatePage = requestPage
        pageSize = requestPageSize
        updateDataList = dataList
        updateOrCreateItem = { oldItem, data, _ ->
            updateOrCreateItemByClass(Item::class.java, oldItem) {
                initItem(data)
            }
        }
        action()
    }
}