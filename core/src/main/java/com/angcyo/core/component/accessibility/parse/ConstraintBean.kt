package com.angcyo.core.component.accessibility.parse

import com.angcyo.core.component.accessibility.back

/**
 * 参数严格的约束
 * Email:angcyo@126.com
 * @author angcyo
 * @date 2020/07/07
 * Copyright (c) 2020 ShenZhen Wayto Ltd. All rights reserved.
 */
data class ConstraintBean(

    /**约束的文本, 这个文本可以是对应的id, 或者node上的文本内容
     * 文本需要全部命中.
     * null会匹配除[textList]约束的其他约束规则的node
     * 空字符会匹配所有包含文本的node
     * */
    var textList: List<String>? = null,

    /**
     * [textList]优先从[wordList]集合中取值.
     * 支持表达式:
     * $N $0将会替换为[wordList]索引为0的值.最大支持10000
     * 1-4 取索引为[1-4]的值
     * 0--1 取索引为[0-倒数第1个]的值
     * -1 取倒数第1个的值
     * */
    var wordTextIndexList: List<String>? = null,

    /**
     * 上述[textList]字段, 对应的是否是id, 否则就是文本.一一对应的关系.
     * 可以是 完整的id, 也可以是 gj4.
     * 完整的id应该是: com.ss.android.ugc.aweme:id/gj4
     *
     * ids 列表中, 只要满足任意一个约束条件, 即视为发现目标
     * */
    var idList: List<Int>? = null,

    /**类名约束, 和[textList]为一一对应的关系.为空, 表示不约束类名
     * 匹配规则时包含, 只要当前设置的cls包含视图中的cls就算命中.
     * 空字符会命中所有
     * */
    var clsList: List<String>? = null,

    /**此约束需要执行的动作, 不指定坐标. 将随机产生. 小于1的数, 表示比例
     * [click] 触发当前节点的点击事件
     * [click2] 在当前节点区域双击
     * [longClick] 触发当前节点的长按事件
     * [touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
     * [double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
     * [move:10,10-100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
     * [fling:10,10-100,100]
     * [back] 执行返回操作
     * [getText] 获取文本内容
     * [setText] 设置文本内容
     * [random] 随机执行
     * ...参考下面的静态声明
     *
     * 空字符会进行随机操作.
     * null 默认是click操作
     * */
    var actionList: List<String>? = null,

    /**
     * [setText]时的输入数据集合, 随机从里面取一个.
     * 如果为null, 则从代码中随机产生
     * */
    var inputList: List<String>? = null,

    /**
     * [setText]时的输入数据在[wordList]集合的索引集合, 随机从里面取一个.
     * 如果为null, 则从[inputList]读取. 如果[inputList]也为null, 则从代码中随机产生
     * */
    var wordInputIndexList: List<String>? = null,

    /**忽略此次[Action]操作的返回值, 不忽略的话, 如果action返回true, 则可能会执行[doActionFinish].
     * 忽略之后, 将不会判断[jump]
     * */
    var ignore: Boolean = false,

    /**此次[Action] [操作成功]之后, 是否跳过之后的[handle]约束处理.
     * [stateList] 中的[finish]操作, 拥有相同效果
     * */
    var jump: Boolean = false,

    /**和[textList]为一一对应的关系.
     * 坐标矩形约束. 格式10,10-100,100 小于1的数, 表示比例否则就是dp.
     * 空字符只要宽高大于0, 就命中.
     * 只要满足一组矩形约束, 就算命中
     *
     * 如果只设置了1个点坐标, 则目标Rect包含这个点就算命中.
     * 如果设置了2个点坐标(左上,右下), 则目标Rect相交这个矩形就算命中.
     *
     * 注意系统状态栏和导航栏对坐标的影响, 参考的是根节点的宽高
     * */
    var rectList: List<String>? = null,

    /**状态约束,只有全部满足状态才能命中
     * [clickable] 具备可点击
     * [unclickable] 具备不可点击
     * [focusable] 具备可获取交点
     * [selected] 具备选中状态
     * [unselected] 具备选未中状态
     * [focused] 具备焦点状态
     * [unfocused] 具备无焦点状态
     * [finish] 直接完成操作
     * ...参考下面的静态声明
     * */
    var stateList: List<String>? = null,

    /**和[textList]为一一对应的关系. null和空字符表示匹配自己
     * 约束路径, 通过上述条件找到node之后, 再使用路径查找到真正的目标
     * 格式: +1 -2 >3 <4
     * [+1] 兄弟下1个的节点
     * [-2] 兄弟上2个的节点
     * [>3] child第3个节点
     * [<4] 第4个parent
     * */
    var pathList: List<String>? = null,

    /**当以上规则匹配到很多节点时, 挑出指定索引的节点执行[actionList]. 不指定默认所有节点
     * index>=0, 正向取索引
     * index<0, 倒数第几个*/
    var handleNodeList: List<Int>? = null
) {
    companion object {

        //可以执行的操作 [action]
        const val ACTION_CLICK = "click" //触发当前节点的点击事件, null 默认是click操作
        const val ACTION_CLICK2 = "click2" //在当前节点区域双击
        const val ACTION_LONG_CLICK = "longClick" //触发当前节点的长按事件
        const val ACTION_DOUBLE = "double" //[double:20,30] 在屏幕坐标x=20dp y=30dp的地方双击
        const val ACTION_TOUCH = "touch" //[touch:10,10] 在屏幕坐标x=10dp y=10dp的地方点击
        const val ACTION_MOVE = "move" //[move:10,10-100,100] 从屏幕坐标x=10dp y=10dp的地方移动到100dp 100dp的地方
        const val ACTION_FLING = "fling" //[fling:10,10-100,100]
        const val ACTION_BACK = "back" //执行返回操作
        const val ACTION_HOME = "home" //回到桌面
        const val ACTION_GET_TEXT = "getText" //获取文本内容
        const val ACTION_SET_TEXT = "setText" //设置文本内容 [inputList]
        const val ACTION_RANDOM = "random" //随机执行, 空字符会进行随机操作.
        const val ACTION_FINISH = "finish" //直接完成操作
        const val ACTION_START = "start" //[start:com.xxx.xxx]启动应用程序 [:main]本机 [:target]目标(空或null)
        const val ACTION_COPY = "copy" //复制文本 [inputList]
        const val ACTION_KEY = "key" //发送按键事件[key:66] KEYCODE_ENTER=66 发送回车按键. (test)
        const val ACTION_FOCUS = "focus" //请求焦点
        const val ACTION_SCROLL_FORWARD = "scrollForward" //向前滚动
        const val ACTION_SCROLL_BACKWARD = "scrollBackward" //向后滚动

        //需要指定的状态 [state]
        const val STATE_CLICKABLE = "clickable" //具备可点击
        const val STATE_UNCLICKABLE = "unclickable" //具备不可点击
        const val STATE_FOCUSABLE = "focusable" //具备可获取交点
        const val STATE_FOCUSED = "focused" //具备焦点状态
        const val STATE_UNFOCUSED = "unfocused" //具备选未中状态
        const val STATE_SELECTED = "selected" //具备选中状态
        const val STATE_UNSELECTED = "unselected" //具备选未中状态
        const val STATE_SCROLLABLE = "scrollable" //具备可滚动状态
    }
}