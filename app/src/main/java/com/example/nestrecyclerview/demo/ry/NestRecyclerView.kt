package com.example.nestrecyclerview.demo.ry

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.lang.RuntimeException


/**
 * 首页仿淘宝、京东嵌套滚动组件，需配合NestChildRecyclerView使用
 */
class NestRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {


    private val mFlingHelper = FlingHelper(context)

    /**
     * 记录上次TouchEvent事件的y坐标
     */
    private var oldTouchY: Float = 0f

    /**
     * 累计从ACTION_DOWN开始滑动的距离
     */
    private var scrollDy = 0

    /**
     * 用于判断RecyclerView是否在fling
     */
    private var isStartFling = false

    /**
     * 记录当前滑动的y轴加速度
     */
    private var scrollVelocityY: Int = 0

    /**
     * 设置预加载RY显示区域外的内容（-1时默认使用RY的高度）
     */
    private var preLoadHeight: Int = -1

    init {
        initLayoutManager()
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //如果父RecyclerView fling过程中已经到底部，需要让子RecyclerView滑动剩下的fling
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    dispatchChildFling()
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isStartFling) {
                    scrollDy = 0
                    isStartFling = false
                }
                //累计当前RecyclerView在相对滚动的位置，用于计算onFling时的量
                scrollDy += dy
            }
        })
    }

    private fun dispatchChildFling() {
        if (isScrollToBottom() && scrollVelocityY != 0) {
            val splineFlingDistance = mFlingHelper.getSplineFlingDistance(scrollVelocityY)
            if (splineFlingDistance > scrollDy) {
                childFling(mFlingHelper.getVelocityByDistance(splineFlingDistance - scrollDy.toDouble()))
            }
        }
        scrollDy = 0
        scrollVelocityY = 0
    }

    private fun childFling(velY: Int) {
        findNestedScrollingChildRecyclerView()?.fling(0, velY)
    }

    private fun initLayoutManager() {
        //这个LayoutManager你也可以使用官方的LinearLayoutManager，这个MultiLinearLayoutManager仅仅时添加了计算滚动高度的功能而已
        val linearLayoutManager = object : MultiLinearLayoutManager(context) {
            override fun canScrollVertically(): Boolean {
                val childRecyclerView = findNestedScrollingChildRecyclerView()
                return childRecyclerView == null || childRecyclerView.isScrollToTop()
            }

            //预加载RY显示区域外的内容，预防RY下滑时由于子RY第一次创建而造成的短暂空白问题
            override fun getExtraLayoutSpace(state: State?): Int {
                return if (preLoadHeight >= 0) preLoadHeight
                else height
            }
        }
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        layoutManager = linearLayoutManager
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            //ACTION_DOWN的时候重置加速度
            scrollVelocityY = 0
            stopScroll()
        }
        if (e.action != MotionEvent.ACTION_MOVE) {
            //在非ACTION_MOVE的情况下，将oldTouchY置为0
            oldTouchY = 0f
        }
        return super.dispatchTouchEvent(e)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (isScrollToBottom()) {
            val oldTouchY = if (this.oldTouchY == 0f) e.y else oldTouchY
            val deltaTouchY = (oldTouchY - e.y).toInt()
            if (deltaTouchY != 0) {
                //如果父RecyclerView已经滑动到底部，需要让子RecyclerView滑动剩余的距离
                findNestedScrollingChildRecyclerView()?.run {
                    scrollBy(0, deltaTouchY)
                }
            }
        }
        //记录当前触摸的位置
        oldTouchY = e.y
        return super.onTouchEvent(e)
    }

    override fun fling(velX: Int, velY: Int): Boolean {
        val fling = super.fling(velX, velY)
        if (!fling || velY <= 0) {
            scrollVelocityY = 0
        } else {
            isStartFling = true
            scrollVelocityY = velY
        }
        return fling
    }

    private fun isScrollToBottom(): Boolean {
        //RecyclerView.canScrollVertically(1)的值表示是否能向上滚动，false表示已经滚动到底部
        return !canScrollVertically(1)
    }

    private fun findNestedScrollingChildRecyclerView(): NestChildRecyclerView? {
        (adapter as? INestAdapter<*>)?.apply {
            return this.getCurrentChildRecyclerView()
        }
        return null
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        if (adapter is INestAdapter<*>)
            super.setAdapter(adapter)
        else throw RuntimeException("${javaClass.simpleName}:Adapter需要实现INestAdapter接口")
    }

    fun setPreLoadHeight(height: Int) {
        this.preLoadHeight = height
    }

    interface INestAdapter<T> {
        fun getCurrentChildRecyclerView(): NestChildRecyclerView?
        fun createChildRecyclerView(item: T, index: Int): NestChildRecyclerView
    }
}