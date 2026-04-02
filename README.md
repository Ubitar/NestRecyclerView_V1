# NestRecyclerView

仿淘宝、京东首页嵌套滚动 RecyclerView 组件，支持复杂的列表嵌套滑动场景。

## 效果预览

![示例.gif](https://github.com/Ubitar/NestRecyclerView/blob/master/screenshot/%E7%A4%BA%E4%BE%8B.gif)

## 功能特点

- ✅ 支持父子 RecyclerView 嵌套滚动
- ✅ Fling 惯性滚动在父子组件间连续传递
- ✅ 支持预加载，避免滑动时出现空白
- ✅ 配合 ViewPager + TabLayout 实现类似电商首页效果
- ✅ 子列表滚动到底部回调，方便加载更多
- ✅ Tab 切换事件监听

## 下载体验

![下载二维码.png](https://github.com/Ubitar/NestRecyclerView_V1/blob/master/screenshot/%E4%B8%8B%E8%BD%BD%E4%BA%8C%E7%BB%B4%E7%A0%81.png)

## 快速开始

### 1. 添加依赖

确保你的项目已添加以下依赖：

```gradle
dependencies {
    // AndroidX RecyclerView
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    
    // 可选：BaseRecyclerViewAdapterHelper（示例中使用）
    implementation "com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.11"
}
```

### 2. 核心组件

本项目包含两个核心组件：

| 组件 | 说明 |
|------|------|
| `NestRecyclerView` | 父级 RecyclerView，作为外层容器 |
| `NestChildRecyclerView` | 子级 RecyclerView，嵌套在父级内部 |

### 3. 使用步骤

#### 步骤一：布局文件

在 XML 布局中添加 `NestRecyclerView`：

```xml
<com.example.nestrecyclerview.demo.ry.NestRecyclerView
    android:id="@+id/recyclerView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

#### 步骤二：创建 Adapter

你的 Adapter 需要实现 `NestRecyclerView.INestAdapter` 接口：

```kotlin
class MainAdapter : BaseMultiItemQuickAdapter<MainListBean, BaseViewHolder>(null),
    NestRecyclerView.INestAdapter<MainListBean> {

    // 获取当前显示的子 RecyclerView
    override fun getCurrentChildRecyclerView(): NestChildRecyclerView? {
        // 返回当前 ViewPager 中显示的子 RecyclerView
    }

    // 创建子 RecyclerView
    override fun createChildRecyclerView(item: MainListBean, index: Int): NestChildRecyclerView {
        // 创建并配置子 RecyclerView
    }
}
```

#### 步骤三：初始化 RecyclerView

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var mainAdapter: MainAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化 Adapter
        mainAdapter = MainAdapter()
        
        // 可选：设置预加载高度（解决滑动时短暂空白问题）
        // binding.recyclerView.setPreLoadHeight(200) // px
        
        // 设置 Adapter
        binding.recyclerView.adapter = mainAdapter
        
        // 加载数据
        loadData()
        
        // 设置事件监听
        initEvent()
    }
}
```

#### 步骤四：设置事件监听

```kotlin
private fun initEvent() {
    // 子列表滚动到底部监听
    mainAdapter.setOnChildScrollEndListener { recyclerView, newState ->
        // 在这里加载更多数据
        loadMoreData()
    }
    
    // Tab 点击切换监听
    mainAdapter.setTabSelectListener { viewPager, position ->
        // 切换 ViewPager 页面
        viewPager.setCurrentItem(position, false)
    }
    
    // ViewPager 翻页监听
    mainAdapter.setOnTurnPageListener { tabRecyclerView, position ->
        // 更新 Tab 选中状态
    }
}
```

## 核心类说明

### NestRecyclerView（父容器）

父级 RecyclerView，负责管理整体滚动逻辑。

**主要方法：**

| 方法 | 说明 |
|------|------|
| `setPreLoadHeight(height: Int)` | 设置预加载高度，避免滑动时空白 |
| `setAdapter(adapter)` | 设置 Adapter（必须实现 INestAdapter 接口）|

**接口定义：**

```kotlin
interface INestAdapter<T> {
    // 获取当前显示的子 RecyclerView
    fun getCurrentChildRecyclerView(): NestChildRecyclerView?
    
    // 创建子 RecyclerView
    fun createChildRecyclerView(item: T, index: Int): NestChildRecyclerView
}
```

### NestChildRecyclerView（子组件）

子级 RecyclerView，嵌套在父级内部，支持独立的滚动逻辑。

**特点：**
- 自动处理与父级 RecyclerView 的滚动协调
- 滚动到顶部时，剩余惯性传递给父级
- 支持所有标准 RecyclerView 的功能

## 项目结构

```
app/src/main/java/com/example/nestrecyclerview/
├── demo/
│   ├── App.kt                    # Application 类
│   ├── MainActivity.kt           # 主 Activity
│   ├── adapter/
│   │   ├── MainAdapter.kt        # 主列表 Adapter
│   │   ├── SubAdapter.kt         # 子列表 Adapter
│   │   ├── TabAdapter.kt         # Tab Adapter
│   │   └── VViewPagerAdapter.kt  # ViewPager 适配器
│   ├── bean/
│   │   ├── MainListBean.kt       # 数据实体类
│   │   └── MainTabBean.kt        # Tab 数据实体
│   └── ry/
│       ├── NestRecyclerView.kt       # 父级 RecyclerView
│       ├── NestChildRecyclerView.kt  # 子级 RecyclerView
│       ├── FlingHelper.kt            # Fling 辅助类
│       └── MultiLinearLayoutManager.kt # 自定义 LayoutManager
```

## 使用场景

本组件适用于以下场景：

- 🛒 电商首页（淘宝、京东风格）
- 📰 新闻资讯首页
- 🎬 视频平台首页
- 🏠 生活服务类 App 首页

## 原理解析

详细的技术原理分析请参考文章：

[深入理解嵌套滚动原理](https://www.jianshu.com/p/cbb415e84de8)

## 常见问题

### Q: 滑动时出现短暂空白怎么办？

A: 调用 `setPreLoadHeight()` 方法设置预加载高度：

```kotlin
binding.recyclerView.setPreLoadHeight(200) // 单位：px
```

### Q: 如何实现下拉刷新？

A: 可以在外层包裹 SwipeRefreshLayout：

```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/refreshLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <com.example.nestrecyclerview.demo.ry.NestRecyclerView
        android:id="@+id/recyclerView"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
        
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

### Q: 子列表如何加载更多数据？

A: 监听 `onChildScrollEndListener` 回调：

```kotlin
mainAdapter.setOnChildScrollEndListener { recyclerView, newState ->
    // 子列表滚动到底部，加载更多
    loadMoreData()
}
```

## License

MIT License

## 贡献

欢迎提交 Issue 和 Pull Request！