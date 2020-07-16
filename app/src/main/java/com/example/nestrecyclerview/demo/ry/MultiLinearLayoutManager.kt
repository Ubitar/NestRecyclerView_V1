package com.example.nestrecyclerview.demo.ry

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * 用于更精确计算当有多种不同布局的holder时，RY的滚动高度
 */
open class MultiLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

    private val heightMap: MutableMap<Int, Int?> = HashMap()

    override fun onLayoutCompleted(state: RecyclerView.State?) {
        super.onLayoutCompleted(state)
        val count = childCount
        for (i in 0 until count) {
            val view = getChildAt(i)
            heightMap[i] = view?.height
        }
    }

    /** 计算垂直滚动时滚动的距离  */
    fun computeVerticalHeight(): Int {
        return if (childCount == 0) {
            0
        } else try {
            val firstVisiblePosition = findFirstVisibleItemPosition()
            val firstVisibleView = findViewByPosition(firstVisiblePosition)!!
            var offsetY = (-firstVisibleView.y).toInt()
            for (i in 0 until firstVisiblePosition) {
                offsetY += (if (heightMap[i] == null) 0 else heightMap[i])!!
            }
            offsetY
        } catch (e: Exception) {
            0
        }

    }
}