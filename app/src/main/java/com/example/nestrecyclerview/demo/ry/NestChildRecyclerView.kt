package com.example.nestrecyclerview.demo.ry

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/**
 * 首页仿淘宝、京东嵌套滚动子组件，需配合NestRecyclerView使用
 */
class NestChildRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private val mFlingHelper = FlingHelper(context)

    /**
     * 记录当前滑动的y轴加速度
     */
    private var scrollVelocityY = 0

    /**
     * 用于判断RecyclerView是否在fling
     */
    private var isStartFling: Boolean = false

    /**
     * 累计从ACTION_DOWN开始滑动的距离
     */
    private var scrollDy: Int = 0

    private var mParentRecyclerView: NestRecyclerView? = null

    init {
        overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isStartFling) {
                    scrollDy = 0
                    isStartFling = false
                }
                //记录当前RecyclerView在相对滚动的位置
                scrollDy += dy
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    dispatchParentFling()
                }
            }
        })
    }

    private fun dispatchParentFling() {
        if (mParentRecyclerView == null)
            mParentRecyclerView = findParentRecyclerView()
        mParentRecyclerView?.run {
            if (isScrollToTop() && scrollVelocityY != 0) {
                //当前ChildRecyclerView已经滑动到顶部，且竖直方向加速度不为0,如果有多余的需要交由父RecyclerView继续fling
                val flingDistance = mFlingHelper.getSplineFlingDistance(scrollVelocityY)
                if (flingDistance > (Math.abs(scrollDy))) {
                    fling(0, -mFlingHelper.getVelocityByDistance(flingDistance + scrollDy))
                }
                scrollDy = 0
                scrollVelocityY = 0
            }
        }
    }

    override fun dispatchTouchEvent(e: MotionEvent): Boolean {
        if (e.action == MotionEvent.ACTION_DOWN) {
            scrollVelocityY = 0
        }
        return super.dispatchTouchEvent(e)
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        if (isAttachedToWindow.not()) return false
        val fling = super.fling(velocityX, velocityY)
        if (!fling || velocityY >= 0) {
            //fling为false表示加速度达不到fling的要求，将mVelocityY重置
            scrollVelocityY = 0
        } else {
            //正在进行fling
            isStartFling = true
            scrollVelocityY = velocityY
        }
        return fling
    }


    fun isScrollToTop(): Boolean {
        //RecyclerView.canScrollVertically(-1)的值表示是否能向下滚动，false表示已经滚动到顶部
        return !canScrollVertically(-1)
    }

    private fun findParentRecyclerView(): NestRecyclerView? {
        var parentView = parent
        while (parentView !is NestRecyclerView) {
            parentView = parentView.parent
        }
        return parentView
    }

}