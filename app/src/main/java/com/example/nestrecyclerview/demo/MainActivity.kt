package com.example.nestrecyclerview.demo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.blankj.utilcode.util.ToastUtils
import com.example.nestrecyclerview.demo.adapter.MainAdapter
import com.example.nestrecyclerview.demo.adapter.SubAdapter
import com.example.nestrecyclerview.demo.adapter.TabAdapter
import com.example.nestrecyclerview.demo.bean.MainListBean
import com.example.nestrecyclerview.demo.bean.MainTabBean
import com.example.nestrecyclerview.demo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter
    private lateinit var tabAdapter: TabAdapter
    private lateinit var subAdapters: MutableList<SubAdapter>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        loadData()
        initEvent()
    }

    /**  初始化列表 */
    private fun initRecyclerView() {
        mainAdapter = MainAdapter()
        binding.recyclerView.itemAnimator = null
//        binding.recyclerView.setPreLoadHeight(200)   //px单位,设置RY预加载不可见（屏幕外）距离的视图，默认是一个RY的高度
        binding.recyclerView.adapter = mainAdapter
    }

    /** 加载数据 */
    private fun loadData() {
        tabAdapter = TabAdapter()
        subAdapters = arrayListOf(
            SubAdapter(),
            SubAdapter(),
            SubAdapter(),
            SubAdapter()
        )

        val list = ArrayList<MainListBean>()
        for (i in 1..10)
            list.add(MainListBean(MainListBean.TYPE_MAIN, "${i}"))

        val tabs = ArrayList<MainTabBean>(4)
        for (i in 1..4)
            tabs.add(MainTabBean("Tab${i}", i == 1))
        tabAdapter.setList(tabs)

        subAdapters.forEach {
            val list = ArrayList<String>()
            for (i in 1..12)
                list.add("${i}")
            it.setList(list)
        }

        list.add(MainListBean(MainListBean.TYPE_SUB, tabAdapter, subAdapters))

        mainAdapter.setList(list)
    }

    /** 初始化触发事件  */
    private fun initEvent() {
        mainAdapter.setOnChildScrollEndListener { recyclerView, newState ->
            ToastUtils.showShort("滚动子RY到底部了")
            //这里可以加载更多数据
        }
        mainAdapter.setTabSelectListener { viewPager, position ->
            tabAdapter.setSelectAt(position)
            viewPager.setCurrentItem(position, false)
        }
        mainAdapter.setOnTurnPageListener { tabRecyclerView, position ->
            tabAdapter.setSelectAt(position)
        }
    }


}