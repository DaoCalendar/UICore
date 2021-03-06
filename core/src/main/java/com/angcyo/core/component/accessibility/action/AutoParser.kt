package com.angcyo.core.component.accessibility.action

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import androidx.collection.ArrayMap
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import com.angcyo.core.component.accessibility.*
import com.angcyo.core.component.accessibility.parse.ConditionBean
import com.angcyo.core.component.accessibility.parse.ConstraintBean
import com.angcyo.core.component.accessibility.parse.isConstraintEmpty
import com.angcyo.core.component.accessibility.parse.isOnlyPathConstraint
import com.angcyo.library.ex.isListEmpty
import com.angcyo.library.utils.getLongNum
import kotlin.math.max
import kotlin.math.min

/**
 * 解析处理
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/08
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
open class AutoParser {

    companion object {

        /**
         * [textList]优先从[wordList]集合中取值.
         * 支持表达式:
         * $N $0将会替换为[wordList]索引为0的值.最大支持10000
         * 1-4 取索引为[1-4]的值
         * 0--1 取索引为[0-倒数第1个]的值
         * -1 取倒数第1个的值
         * */
        fun parseWordTextList(originList: List<String>?, indexList: List<String>): List<String>? {
            if (originList.isListEmpty()) {
                return null
            }

            val originList: List<String> = originList!!

            val result = mutableListOf<String>()
            indexList.forEach { indexStr ->
                if (indexStr.contains("$")) {
                    //匹配 $表达式

                    var target: String? = null
                    for (i in 0..1_00_00) {
                        val pattern = "$$i"
                        if (indexStr.contains(pattern)) {
                            target =
                                (target ?: indexStr)
                                    .replace(pattern, originList.getOrNull(i) ?: "")
                        }
                    }
                    target?.let { result.add(it) }
                } else {
                    val num = indexStr.toIntOrNull()

                    if (num == null) {
                        //匹配 [1-4] 范围

                        val indexOf = indexStr.indexOf("-")
                        if (indexOf != -1) {
                            val startIndex = indexStr.substring(0, indexOf).toIntOrNull() ?: 0
                            val endIndex =
                                indexStr.substring(indexOf + 1, indexStr.length).toIntOrNull() ?: 0

                            val fist =
                                if (startIndex < 0) startIndex + originList.size else startIndex
                            val second =
                                if (endIndex < 0) endIndex + originList.size else endIndex

                            for (i in min(fist, second)..max(fist, second)) {
                                originList.getOrNull(i)?.let {
                                    result.add(it)
                                }
                            }
                        }
                    } else {
                        // 匹配 1, -1 索引
                        originList.getOrNull(if (num < 0) num + originList.size else num)?.let {
                            result.add(indexStr)
                        }
                    }
                }
            }
            //返回
            return if (result.isEmpty()) {
                null
            } else {
                result
            }
        }
    }

    /**解析id时, 需要补全的id全路径包名*/
    var idPackageName: String? = null

    /**根节点在屏幕中的坐标*/
    var _rootNodeRect = Rect()

    //临时存放
    var _tempNodeRect = Rect()

    /**返回当前界面, 是否包含[constraintList]约束的Node信息
     * [onTargetResult] 当找到目标时, 通过此方法回调目标给调用者. first:对应的约束, second:约束对应的node集合
     * */
    open fun parse(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfo>,
        constraintList: List<ConstraintBean>,
        onTargetResult: (List<ParseResult>) -> Unit = {}
    ): Boolean {

        val result: MutableList<ParseResult> = mutableListOf()

        constraintList.forEach { constraint ->
            if (constraint.enable || constraint.actionList?.contains(ConstraintBean.ACTION_ENABLE) == true) {
                //如果当前的[constraint]处于激活状态, 或者拥有激活自身的能力
                val itemResult = ParseResult(constraint, constraintList)
                findConstraintNode(service, autoParseAction, nodeList, itemResult)
                if (itemResult.nodeList.isNotEmpty()) {
                    result.add(itemResult)
                }
            }
        }

        if (result.isNotEmpty()) {
            //通过约束, 找到了目标

            onTargetResult(result)
            return true
        }

        return false
    }

    /**查找满足约束的Node*/
    open fun findConstraintNode(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        nodeList: List<AccessibilityNodeInfo>,
        parseResult: ParseResult
    ) {
        val rootNodeInfo: AccessibilityNodeInfo = nodeList.mainNode() ?: return

        //存储一下跟node的矩形, 方便用于坐标比例计算
        rootNodeInfo.getBoundsInScreen(_rootNodeRect)

        if (parseResult.constraint.isConstraintEmpty()) {
            //空约束返回[rootNodeInfo]
            parseResult.nodeList.add(rootNodeInfo.wrap())
            return
        }

        //查找所有
        findConstraintNodeByRootNode(
            service,
            autoParseAction,
            rootNodeInfo,
            parseResult
        )
    }

    /**在指定的根节点[rootNodeInfo]下, 匹配节点*/
    open fun findConstraintNodeByRootNode(
        service: AccessibilityService,
        autoParseAction: AutoParseAction,
        rootNodeInfo: AccessibilityNodeInfo,
        parseResult: ParseResult
    ) {

        //约束条件
        val constraintBean: ConstraintBean = parseResult.constraint

        //返回值
        val result: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()

        //需要匹配的文本
        val text: List<String>? = autoParseAction.getTextList(constraintBean)

        //根节点
        val rootNodeWrap: AccessibilityNodeInfoCompat = rootNodeInfo.wrap()

        if (text == null) {
            //不约束文本, 单纯约束其他规则
            if (constraintBean.isOnlyPathConstraint()) {
                //单纯的path约束
                parseConstraintPath(
                    constraintBean,
                    constraintBean.pathList?.getOrNull(0),
                    rootNodeWrap,
                    result
                )
            } else {
                //其他约束组合
                rootNodeInfo.findNode(result) { nodeInfoCompat ->
                    if (match(constraintBean, nodeInfoCompat, 0)) {
                        if (!constraintBean.pathList.isNullOrEmpty()) {
                            parseConstraintPath(
                                constraintBean,
                                constraintBean.pathList?.getOrNull(0),
                                nodeInfoCompat,
                                result
                            )
                            -1
                        } else {
                            1
                        }
                    } else {
                        -1
                    }
                }
            }
        } else {
            val packageName: String = idPackageName ?: service.packageName

            //临时存储node
            var tempList: MutableList<AccessibilityNodeInfoCompat>

            //列表中的所有文本是否都匹配通过
            val matchMap = ArrayMap<Int, List<AccessibilityNodeInfoCompat>>()
            for (index: Int in text.indices) {
                tempList = mutableListOf()
                try {

                    //完整id 是需要包含包名的
                    val isIdText: Boolean = constraintBean.idList?.getOrNull(index) == 1
                    val subText: String? =
                        if (isIdText) packageName.id(text[index]) else text[index]

                    if (!isIdText && subText == null) {
                        //text匹配模式下, 空字符串处理
                        tempList.add(rootNodeWrap)
                        matchMap[index] = tempList
                    } else {
                        rootNodeInfo.findNode(tempList) { nodeInfoCompat ->
                            var findNode = -1
                            if (isIdText) {
                                //id 全等匹配
                                val idName = nodeInfoCompat.viewIdName()
                                findNode = if (subText == idName) {
                                    1
                                } else {
                                    -1
                                }
                            } else {
                                //文本包含匹配
                                findNode = if (nodeInfoCompat.haveText(subText ?: "")) {
                                    1
                                } else {
                                    -1
                                }
                            }

                            if (findNode == 1) {
                                findNode = if (match(constraintBean, nodeInfoCompat, index)) {
                                    //其他约束匹配成功
                                    if (!constraintBean.pathList.isNullOrEmpty()) {
                                        parseConstraintPath(
                                            constraintBean,
                                            constraintBean.pathList?.getOrNull(index),
                                            nodeInfoCompat,
                                            tempList
                                        )
                                        -1
                                    } else {
                                        1
                                    }
                                } else {
                                    -1
                                }
                            }

                            findNode
                        }

                        if (tempList.isNotEmpty()) {
                            matchMap[index] = tempList
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            //是否所有文本都匹配到了
            var allTextMatch = true
            for (index: Int in text.indices) {
                if (matchMap[index].isNullOrEmpty()) {
                    allTextMatch = false
                    break
                }
            }

            //全部匹配到, 将匹配到的node返回
            if (allTextMatch) {
                for (index: Int in text.indices) {
                    matchMap[index]?.forEach {
                        if (!result.contains(it)) {
                            result.add(it)
                        }
                    }
                }
            }
        }

        //条件过滤筛选
        if (!constraintBean.conditionList.isNullOrEmpty()) {
            //需要条件筛选
            val conditionNodeList: MutableList<AccessibilityNodeInfoCompat> = mutableListOf()
            result.forEach { node ->
                for (condition in constraintBean.conditionList!!) {
                    val isGet = parseCondition(node, condition)
                    if (isGet) {
                        //筛选通过
                        conditionNodeList.add(node)
                        break
                    }
                }
            }
            parseResult.conditionNodeList = conditionNodeList
        }

        if (constraintBean.after == null ||
            constraintBean.after?.isConstraintEmpty() == true ||
            result.isEmpty()
        ) {
            if (result.isNotEmpty()) {
                parseResult.nodeList.addAll(result)
            }
        } else {
            //还有[after]约束
            val afterNodeList =
                if (parseResult.conditionNodeList == null) result else parseResult.conditionNodeList!!

            afterNodeList.forEach { node ->
                //继续查找
                val nextParseResult =
                    ParseResult(constraintBean.after!!, parseResult.constraintList)
                findConstraintNodeByRootNode(
                    service,
                    autoParseAction,
                    node.unwrap(),
                    nextParseResult
                )
                parseResult.nodeList.addAll(nextParseResult.nodeList)

                //条件约束筛选后的节点集合
                if (parseResult.conditionNodeList == null) {
                    parseResult.conditionNodeList = nextParseResult.conditionNodeList
                } else {
                    parseResult.conditionNodeList?.addAll(
                        nextParseResult.conditionNodeList ?: emptyList()
                    )
                }
            }
        }
    }

    /**其他规则匹配*/
    open fun match(
        constraintBean: ConstraintBean,
        node: AccessibilityNodeInfoCompat,
        index: Int /*对应text中的索引*/
    ): Boolean {
        val cls = constraintBean.clsList?.getOrNull(index)
        val rect = constraintBean.rectList?.getOrNull(index)

        //是否匹配成功
        var result = false

        if (!cls.isNullOrEmpty() /*指定了匹配类名*/ &&
            node.className?.contains(cls.toString().toRegex()) != true /*类名匹配未命中*/
        ) {
            //但是类名不同
            result = false
        } else {
            //类名命中

            if (rect != null) {
                //坐标约束

                val bound = node.bounds()

                if (node.isValid()) {
                    //如果设置了矩形匹配规则, 那么这个node的rect一定要是有效的
                    rect.let {
                        if (it.isEmpty()) {
                            //空字符只要宽高大于0, 就命中
                            result = node.isValid()
                        } else {

                            it.split("-").apply {
                                val p1 = getOrNull(0)?.toPointF(
                                    _rootNodeRect.width(),
                                    _rootNodeRect.height()
                                )
                                val p2 = getOrNull(1)?.toPointF(
                                    _rootNodeRect.width(),
                                    _rootNodeRect.height()
                                )

                                if (p1 == null && p2 == null) {
                                } else {
                                    if (p2 == null) {
                                        //只设置了单个点
                                        if (bound.contains(p1!!.x.toInt(), p1.y.toInt())) {
                                            result = true
                                        }
                                    } else {
                                        _tempNodeRect.set(
                                            p1!!.x.toInt(),
                                            p1.y.toInt(),
                                            p2.x.toInt(),
                                            p2.y.toInt()
                                        )
                                        //设置了多个点, 那么只要2个矩形相交, 就算命中
                                        if (bound.intersect(_tempNodeRect)) {
                                            result = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                //other
                result = true
            }
        }

        //状态约束
        if (result) {
            val state: List<String>? = constraintBean.stateList
            if (state != null && !state.isListEmpty()) {
                var match = true
                state.forEach {
                    when (it) {
                        ConstraintBean.STATE_CLICKABLE -> {
                            //需要具备可以点击的状态
                            if (!node.isClickable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_NOT_CLICKABLE -> {
                            //需要具备不可以点击的状态
                            if (node.isClickable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_FOCUSABLE -> {
                            //需要具备可以获取焦点状态
                            if (!node.isFocusable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_FOCUSED -> {
                            //需要具备焦点状态
                            if (!node.isFocused) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_UNFOCUSED -> {
                            //需要具备无焦点状态
                            if (node.isFocused) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_SELECTED -> {
                            //需要具备选中状态
                            if (!node.isSelected) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_UNSELECTED -> {
                            //需要具备不选中状态
                            if (node.isSelected) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_SCROLLABLE -> {
                            //需要具备可滚动状态
                            if (!node.isScrollable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_LONG_CLICKABLE -> {
                            //需要具备可以长按的状态
                            if (!node.isLongClickable) {
                                match = false
                            }
                        }
                        ConstraintBean.STATE_NOT_LONG_CLICKABLE -> {
                            //需要具备不可以长按的状态
                            if (node.isLongClickable) {
                                match = false
                            }
                        }
                    }
                }

                if (!match) {
                    //匹配状态失败
                    result = false
                }
            }
        }

        return result
    }

    /**根据约束的路径, 找出对应的node*/
    open fun parseConstraintPath(
        constraintBean: ConstraintBean,
        path: String?,
        node: AccessibilityNodeInfoCompat,
        result: MutableList<AccessibilityNodeInfoCompat>
    ) {
        if (path.isNullOrEmpty()) {
            result.add(node)
        } else {
            var target: AccessibilityNodeInfoCompat? = node
            //格式: +1 -2 >3 <4
            val paths = path.split(" ").toList()
            for (p in paths) {
                target = parsePath(p, target)

                if (target == null) {
                    break
                }
            }
            if (target != null) {
                result.add(target)
            }
        }
    }

    open fun parsePath(
        path: String,
        node: AccessibilityNodeInfoCompat?
    ): AccessibilityNodeInfoCompat? {
        return if (node == null || path.isEmpty()) {
            node
        } else {
            //[+1] 兄弟下1个的节点
            //[-2] 兄弟上2个的节点
            //[>3] child第3个节点
            //[<4] 第4个parent

            val num = path.substring(1, path.length).toIntOrNull() ?: 0

            when {
                path.startsWith("+") -> node.getBrotherNode(num)
                path.startsWith("-") -> node.getBrotherNode(-num)
                path.startsWith(">") -> node.getParentOrChildNode(num)
                path.startsWith("<") -> node.getParentOrChildNode(-num)
                else -> null
            }
        }
    }

    /**条件过滤, 返回值表示过滤成功*/
    open fun parseCondition(node: AccessibilityNodeInfoCompat, condition: ConditionBean): Boolean {
        var isGet = false

        //child 数量条件
        val childCount = condition.childCount
        if (childCount != null) {
            if (childCount.isEmpty()) {
                isGet = true
            } else {
                val num = childCount.getLongNum()?.toInt()
                num?.let {
                    if (childCount.startsWith(">=")) {
                        if (node.childCount >= num) {
                            isGet = true
                        }
                    } else if (childCount.startsWith(">")) {
                        if (node.childCount > num) {
                            isGet = true
                        }
                    } else if (childCount.startsWith("<=")) {
                        if (node.childCount <= num) {
                            isGet = true
                        }
                    } else if (childCount.startsWith("<")) {
                        if (node.childCount < num) {
                            isGet = true
                        }
                    } else {
                        if (node.childCount == num) {
                            isGet = true
                        }
                    }
                }
                if (!isGet) {
                    return false
                }
            }
        }

        //包含文本
        if (condition.containsText != null) {
            var isHave = true
            condition.containsText?.forEach {
                if (!node.haveText(it)) {
                    isHave = false
                }
            }
            if (isHave) {
                isGet = true
            }

            if (!isGet) {
                return false
            }
        }

        //不包含文本
        if (condition.notContainsText != null) {
            var isHave = false
            condition.notContainsText?.forEach {
                if (node.haveText(it)) {
                    isHave = true
                }
            }
            if (isHave) {
                isGet = false
            }

            if (!isGet) {
                return false
            }
        }

        return isGet
    }
}