package com.folioreader.ui.view

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ActionMode.Callback
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.R
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl.HighlightStyle
import com.folioreader.model.MarkVo
import com.folioreader.model.event.HighlightNoteEvent
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.fragment.DictionaryFragment
import com.folioreader.ui.fragment.FolioPageFragment
import com.folioreader.ui.fragment.NoteDetailFragment
import com.folioreader.util.AppUtil
import com.folioreader.util.HighlightUtil
import com.folioreader.util.UiUtil
import dalvik.system.PathClassLoader
import kotlinx.android.synthetic.main.text_selection.view.*
import kotlinx.android.synthetic.main.widget_text_selection.view.UnderlineDotted2
import kotlinx.android.synthetic.main.widget_text_selection.view.iv_blue
import kotlinx.android.synthetic.main.widget_text_selection.view.iv_font_light
import kotlinx.android.synthetic.main.widget_text_selection.view.iv_green
import kotlinx.android.synthetic.main.widget_text_selection.view.iv_pink
import kotlinx.android.synthetic.main.widget_text_selection.view.iv_right_white
import kotlinx.android.synthetic.main.widget_text_selection.view.tv_copy
import kotlinx.android.synthetic.main.widget_text_selection.view.tv_dv_line
import kotlinx.android.synthetic.main.widget_text_selection.view.tv_write
import kotlinx.android.synthetic.main.widget_text_selection.view.underlineHighlight2
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import org.springframework.util.ReflectionUtils
import java.lang.ref.WeakReference
import kotlin.math.floor

/**
 * @author by mahavir on 3/31/16.
 */
class FolioWebView : WebView {

    companion object {

        val LOG_TAG: String = FolioWebView::class.java.simpleName
        private const val IS_SCROLLING_CHECK_TIMER = 100
        private const val IS_SCROLLING_CHECK_MAX_DURATION = 10000

        @JvmStatic
        fun onWebViewConsoleMessage(cm: ConsoleMessage, LOG_TAG: String, msg: String): Boolean {
            when (cm.messageLevel()) {
                ConsoleMessage.MessageLevel.LOG -> {
                    Log.v(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.DEBUG, ConsoleMessage.MessageLevel.TIP -> {
                    Log.d(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.WARNING -> {
                    Log.w(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.ERROR -> {
                    Log.e(LOG_TAG, msg)
                    return true
                }
                else -> return false
            }
        }
    }

    private var horizontalPageCount = 0
    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var mScrollListener: ScrollListener? = null
    private var mSeekBarListener: SeekBarListener? = null
    private lateinit var gestureDetector: GestureDetectorCompat
    private var eventActionDown: MotionEvent? = null
    private var pageWidthCssDp: Int = 0
    var pageWidthCssPixels: Float = 0.toFloat()
    private lateinit var webViewPager: WebViewPager
    private lateinit var uiHandler: Handler
    private lateinit var folioActivityCallback: FolioActivityCallback
    private lateinit var parentFragment: FolioPageFragment

    private var actionMode: ActionMode? = null
    private var textSelectionCb: TextSelectionCb? = null
    private var textSelectionCb2: TextSelectionCb2? = null
    private var selectionRect = Rect()
    private val popupRect = Rect()
    private var popupWindow = PopupWindow()
    private lateinit var viewTextSelection: View
    private var isScrollingCheckDuration: Int = 0
    private var isScrollingRunnable: Runnable? = null
    private var oldScrollX: Int = 0
    private var oldScrollY: Int = 0
    private var lastTouchAction: Int = 0
    private var destroyed: Boolean = false
    private var handleHeight: Int = 0
    private var calculatedProgress = 0.0

    private var lastScrollType: LastScrollType? = null
    private  var highlightId: String? = null
    private var textUnderlineTextView: TextView? = null
    public var stopScroll: Boolean = false
    val contentHeightVal: Int
        get() = floor((this.contentHeight * this.scale).toDouble()).toInt()

    val webViewHeight: Int
        get() = this.measuredHeight

    val currentProgress: Double
        get() = calculatedProgress

    private enum class LastScrollType {
        USER, PROGRAMMATIC
    }

    @JavascriptInterface
    fun getDirection(): String {
        return folioActivityCallback.direction.toString()
    }

    @JavascriptInterface
    fun getTopDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getTopDistraction(unit)
    }

    @JavascriptInterface
    fun getBottomDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getBottomDistraction(unit)
    }

    @JavascriptInterface
    fun getViewportRect(unitString: String): String {
        val unit = DisplayUnit.valueOf(unitString)
        val rect = folioActivityCallback.getViewportRect(unit)
        return UiUtil.rectToDOMRectJson(rect)
    }

    @JavascriptInterface
    fun toggleSystemUI() {
        uiHandler.post {
            folioActivityCallback.toggleSystemUI()
        }
    }

    @JavascriptInterface
    fun isPopupShowing(): Boolean {
        return popupWindow.isShowing
    }

    private inner class HorizontalGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            //Log.d(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.d(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);

            if (!webViewPager.isScrolling) {
                // Need to complete the scroll as ViewPager thinks these touch events should not
                // scroll it's pages.
                //Log.d(LOG_TAG, "-> onFling -> completing scroll");
                uiHandler.postDelayed({
                    // Delayed to avoid inconsistency of scrolling in WebView
                    scrollTo(getScrollXPixelsForPage(webViewPager.currentItem), 0)
                }, 100)
            }

            lastScrollType = LastScrollType.USER
            return true
        }

        override fun onDown(event: MotionEvent): Boolean {
            Log.v(LOG_TAG, "-> onDown -> " + event.toString());

            eventActionDown = MotionEvent.obtain(event)
            super@FolioWebView.onTouchEvent(event)
            return true
        }
    }

    @JavascriptInterface
    fun dismissPopupWindow(): Boolean {
        Log.d(LOG_TAG, "-> dismissPopupWindow -> " + parentFragment.spineItem?.href)
        val wasShowing = popupWindow.isShowing
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            popupWindow.dismiss()
        } else {
            uiHandler.post { popupWindow.dismiss() }
        }
        selectionRect = Rect()
        if(isScrollingRunnable!=null){
            uiHandler.removeCallbacks(isScrollingRunnable!!)
        }
        isScrollingCheckDuration = 0
        return wasShowing
    }

    override fun destroy() {
        super.destroy()
        Log.d(LOG_TAG, "-> destroy")
        dismissPopupWindow()
        destroyed = true
    }

    private inner class VerticalGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
//            Log.v(LOG_TAG, "-> onScroll -> e1 = " + e1 + ", e2 = " + e2 + ", distanceX = " + distanceX + ", distanceY = " + distanceY);
            parentFragment.mWebview?.let { wv ->
                calculatedProgress = (wv.scrollY.toDouble() / wv.contentHeightVal.toDouble()) * 100.0
//                val fmtProgress = "%.2f".format(calculatedProgress)
//                println("VProgress: $fmtProgress%")
            }
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            //Log.v(LOG_TAG, "-> onFling -> e1 = " + e1 + ", e2 = " + e2 + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            lastScrollType = LastScrollType.USER
            return false
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private fun init() {
        Log.v(LOG_TAG, "-> init")
        setWebContentsDebuggingEnabled(true);
        uiHandler = Handler()
        displayMetrics = resources.displayMetrics
        density = displayMetrics!!.density

        gestureDetector = if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            GestureDetectorCompat(context, HorizontalGestureListener())
        } else {
            GestureDetectorCompat(context, VerticalGestureListener())
        }

        initViewTextSelection()
    }

    fun initViewTextSelection() {
        Log.v(LOG_TAG, "-> initViewTextSelection")

        val textSelectionMiddleDrawable = ContextCompat.getDrawable(
            context,
            R.drawable.arrow_down
        )
        handleHeight = textSelectionMiddleDrawable?.intrinsicHeight ?: (24 * density).toInt()

        val config = AppUtil.getSavedConfig(context)!!
        val ctw = if (config.isNightMode) {
            ContextThemeWrapper(context, R.style.FolioNightTheme)
        } else {
            ContextThemeWrapper(context, R.style.FolioDayTheme)
        }
        Log.i(LOG_TAG, config.toString())
        viewTextSelection = LayoutInflater.from(ctw).inflate(R.layout.widget_text_selection, null)

        textUnderlineTextView = viewTextSelection.findViewById(R.id.tv_dv_line)
        viewTextSelection.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)

        viewTextSelection.iv_font_light.setOnClickListener{
            Log.v(LOG_TAG, "-> onClick -> yellowHighlight")
            onHighlightColorItemsClicked(HighlightStyle.highlight_01, false)
        }
        viewTextSelection.iv_right_white.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> yellowHighlight")
            onHighlightColorItemsClicked(HighlightStyle.highlight_01, false)
        }
        viewTextSelection.iv_green.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> greenHighlight")
            onHighlightColorItemsClicked(HighlightStyle.highlight_02, false)
        }
        viewTextSelection.iv_blue.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> blueHighlight")
            onHighlightColorItemsClicked(HighlightStyle.highlight_03, false)
        }
        viewTextSelection.iv_pink.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> pinkHighlight")
            onHighlightColorItemsClicked(HighlightStyle.highlight_04, false)
        }
      /*  viewTextSelection.underlineHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> underlineHighlight")
            onHighlightColorItemsClicked(HighlightStyle.UnderlineDotted, false)
        }*/
        //下划线
        viewTextSelection.underlineHighlight2.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> underlineHighlight")
            onHighlightColorItemsClicked(HighlightStyle.Underline, false)
        }
        viewTextSelection.UnderlineDotted2.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> UnderlineDotted")
            onHighlightColorItemsClicked(HighlightStyle.UnderlineDotted, false)
        }
       /* viewTextSelection.deleteHighlight.setOnClickListener {
            Log.v(LOG_TAG, "-> onClick -> deleteHighlight")
            dismissPopupWindow()
          //  loadUrl("javascript:clearSelection()")
            loadUrl("javascript:deleteThisHighlight()")
        }*/
        //复制
        viewTextSelection.tv_copy.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
        viewTextSelection.tv_dv_line.setOnClickListener {
            dismissPopupWindow()
            if(textUnderlineTextView!!.text =="删除划线"){
                deleteThisHighlight(highlightId)
                highlightId = null
            }
        }
        //写笔记
        viewTextSelection.tv_write.setOnClickListener {
            dismissPopupWindow()
            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
        }
    }

    @JavascriptInterface
    fun onTextSelectionItemClicked(id: Int, selectedText: String?) {


        when (id) {
            R.id.tv_copy -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> copySelection -> $selectedText")
                UiUtil.copyToClipboard(context, selectedText)
                Toast.makeText(context, context.getString(R.string.copied), Toast.LENGTH_SHORT)
                    .show()
                uiHandler.post { loadUrl("javascript:clearSelection()") }

            }
            R.id.shareSelection -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> shareSelection -> $selectedText")
                UiUtil.share(context, selectedText)
            }
            R.id.tv_write -> {
                Log.v(LOG_TAG, "-> onTextSelectionItemClicked -> defineSelection -> $selectedText")
                EventBus.getDefault().post(HighlightNoteEvent(selectedText))
            }
            else -> {
                Log.w(LOG_TAG, "-> onTextSelectionItemClicked -> unknown id = $id")
            }
        }
    }

    private fun showDictDialog(selectedText: String?) {
        val dictionaryFragment = DictionaryFragment()
        val bundle = Bundle()
        bundle.putString(Constants.SELECTED_WORD, selectedText?.trim())
        dictionaryFragment.arguments = bundle
        dictionaryFragment.show(
            parentFragment.fragmentManager!!,
            DictionaryFragment::class.java.name
        )
    }


    private fun onHighlightColorItemsClicked(style: HighlightStyle, isAlreadyCreated: Boolean) {
        parentFragment.highlight(style, "",isAlreadyCreated)
        dismissPopupWindow()
    }

    @JavascriptInterface
    fun deleteThisHighlight(id: String?) {
        Log.d(LOG_TAG, "-> deleteThisHighlight")

        if (id.isNullOrEmpty())
            return

        val highlightImpl = HighLightTable.getHighlightForRangy(id)
       if (HighLightTable.deleteHighlight(id)) {
            //删除选中的下划线
            uiHandler.post { parentFragment.unhighlightSelection() }

            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(
                    context, highlightImpl,
                    HighLight.HighLightAction.DELETE
                )
            }
        }
    }

    fun setParentFragment(parentFragment: FolioPageFragment) {
        this.parentFragment = parentFragment
    }

    fun setFolioActivityCallback(folioActivityCallback: FolioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback
        init()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        pageWidthCssDp = Math.ceil((measuredWidth / density).toDouble()).toInt()
        pageWidthCssPixels = pageWidthCssDp * density
    }

    fun setScrollListener(listener: ScrollListener) {
        mScrollListener = listener
    }

    fun setSeekBarListener(listener: SeekBarListener) {
        mSeekBarListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //Log.v(LOG_TAG, "-> onTouchEvent -> " + AppUtil.actionToString(event.getAction()));

        if (event == null)
            return false

        lastTouchAction = event.action

        return if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            computeHorizontalScroll(event)
        } else {
            computeVerticalScroll(event)
        }
    }

    private fun computeVerticalScroll(event: MotionEvent): Boolean {

        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun computeHorizontalScroll(event: MotionEvent): Boolean {
        Log.v(LOG_TAG, "-> computeHorizontalScroll--> event.action"+ event.action);
        //非点击事件，停止滑动标志为真时，禁止滑动
        if(event.action != MotionEvent.ACTION_DOWN && event.action != MotionEvent.ACTION_UP && stopScroll){
            return false
        }
        // Rare condition in fast scrolling
        if (!::webViewPager.isInitialized )
            return super.onTouchEvent(event)

        webViewPager.dispatchTouchEvent(event)
        val gestureReturn = gestureDetector.onTouchEvent(event)
        return if (gestureReturn) true else super.onTouchEvent(event)
    }

    fun getScrollXDpForPage(page: Int): Int {
        //Log.v(LOG_TAG, "-> getScrollXDpForPage -> page = " + page);
        return page * pageWidthCssDp
    }

    fun getScrollXPixelsForPage(page: Int): Int {
        //Log.v(LOG_TAG, "-> getScrollXPixelsForPage -> page = " + page);
        return Math.ceil((page * pageWidthCssPixels).toDouble()).toInt()
    }

    fun setHorizontalPageCount(horizontalPageCount: Int) {
        this.horizontalPageCount = horizontalPageCount

        uiHandler.post {
            webViewPager = (parent as View).findViewById(R.id.webViewPager)
            webViewPager.setHorizontalPageCount(this@FolioWebView.horizontalPageCount)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        Log.d(LOG_TAG, "-> scrollTo -> x = " + x);

        if (getDirection() == "HORIZONTAL") { // should always be true
            calculatedProgress =
                (webViewPager.currentItem.toDouble() / webViewPager.horizontalPageCount.toDouble()) * 100.0
//            val fmtProgress = "%.2f".format(calculatedProgress)
//            println("HProgress: $fmtProgress%")
        }

        lastScrollType = LastScrollType.PROGRAMMATIC
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (mScrollListener != null) mScrollListener!!.onScrollChange(t)
        super.onScrollChanged(l, t, oldl, oldt)

        if (lastScrollType == LastScrollType.USER) {
            //Log.d(LOG_TAG, "-> onScrollChanged -> scroll initiated by user");
            parentFragment.searchLocatorVisible = null
        }

        lastScrollType = null
    }

    interface ScrollListener {
        fun onScrollChange(percent: Int)
    }

    interface SeekBarListener {
        fun fadeInSeekBarIfInvisible()
    }

    private inner class TextSelectionCb : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onCreateActionMode")
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onPrepareActionMode")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom"),null
                )
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Log.d(LOG_TAG, "-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode")
            dismissPopupWindow()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private inner class TextSelectionCb2 : ActionMode.Callback2() {

        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onCreateActionMode")
            menu.clear()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            Log.d(LOG_TAG, "-> onPrepareActionMode")
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            Log.d(LOG_TAG, "-> onActionItemClicked")
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            Log.d(LOG_TAG, "-> onDestroyActionMode")
            dismissPopupWindow()
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            Log.d(LOG_TAG, "-> onGetContentRect")

            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom"),null
                )
            }
        }
    }

    override fun startActionMode(callback: Callback): ActionMode {
        Log.d(LOG_TAG, "-> startActionMode")

        textSelectionCb = TextSelectionCb()
        actionMode = super.startActionMode(textSelectionCb)
        actionMode?.finish()

        /*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Log.w(LOG_TAG, "-> startActionMode -> Failed to apply theme colors to selection " +
                    "handles", e)
        }*/

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback)
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun startActionMode(callback: Callback, type: Int): ActionMode {
        Log.d(LOG_TAG, "-> startActionMode")

        textSelectionCb2 = TextSelectionCb2()
        actionMode = super.startActionMode(textSelectionCb2, type)
        actionMode?.finish()

        /*try {
            applyThemeColorToHandles()
        } catch (e: Exception) {
            Log.w(LOG_TAG, "-> startActionMode -> Failed to apply theme colors to selection " +
                    "handles", e)
        }*/

        return actionMode as ActionMode

        //Comment above code and uncomment below line for stock text selection
        //return super.startActionMode(callback, type)
    }

    private fun applyThemeColorToHandles() {
        Log.v(LOG_TAG, "-> applyThemeColorToHandles")

        if (Build.VERSION.SDK_INT < 23) {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField =
                ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val handleViewClass =
                    Class.forName("com.android.org.chromium.content.browser.input.HandleView")

                if (handleViewClass.isInstance(view)) {
                    val mDrawableField = ReflectionUtils.findField(handleViewClass, "mDrawable")
                    mDrawableField.isAccessible = true
                    val mDrawable = mDrawableField.get(view) as BitmapDrawable
                    UiUtil.setColorIntToDrawable(config.currentThemeColor, mDrawable)
                }
            }

        } else {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField =
                ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val popupDecorViewClass =
                    Class.forName("android.widget.PopupWindow\$PopupDecorView")

                if (!popupDecorViewClass.isInstance(view))
                    continue

                val mChildrenField = ReflectionUtils.findField(popupDecorViewClass, "mChildren")
                mChildrenField.isAccessible = true
                val mChildren = mChildrenField.get(view) as kotlin.Array<View>

                //val pathClassLoader = PathClassLoader("/system/app/Chrome/Chrome.apk", ClassLoader.getSystemClassLoader())

                val pathClassLoader =
                    PathClassLoader(
                        "/system/app/Chrome/Chrome.apk",
                        folioActivityRef.get()?.classLoader
                    )

                val popupTouchHandleDrawableClass = Class.forName(
                    "org.chromium.android_webview.PopupTouchHandleDrawable",
                    true, pathClassLoader
                )

                //if (!popupTouchHandleDrawableClass.isInstance(mChildren[0]))
                //    continue

                val mDrawableField =
                    ReflectionUtils.findField(popupTouchHandleDrawableClass, "mDrawable")
                mDrawableField.isAccessible = true
                val mDrawable = mDrawableField.get(mChildren[0]) as Drawable
                UiUtil.setColorIntToDrawable(config.currentThemeColor, mDrawable)
            }
        }
    }

    @JavascriptInterface
    fun setSelectionRect(left: Int, top: Int, right: Int, bottom: Int,id: String?) {

        val currentSelectionRect = Rect()
        currentSelectionRect.left = (left * density).toInt()
        currentSelectionRect.top = (top * density).toInt()
        currentSelectionRect.right = (right * density).toInt()
        currentSelectionRect.bottom = (bottom * density).toInt()
        Log.d(LOG_TAG, "-> setSelectionRect -> $id")
        computeTextSelectionRect(currentSelectionRect)

        uiHandler.post {
            if(id != null && id!!.contains(HighLightTable.MARK_TYPE)){//段落笔记
                //查询
              val markVo: MarkVo =  HighLightTable.getOneHighlightNote(parentFragment!!.mBookId,id)
                if(markVo != null){
                    val noteDetailFragment = NoteDetailFragment(markVo.bookId,markVo.id,markVo.note,markVo.content,MarkVo.HighlightMarkType,parentFragment!!)
                    noteDetailFragment.show(parentFragment.activity!!.supportFragmentManager,"")
                }
            }else{
                showTextSelectionPopup(id)
            }

        }
    }

    private fun computeTextSelectionRect(currentSelectionRect: Rect) {
        Log.v(LOG_TAG, "-> computeTextSelectionRect")

        val viewportRect = folioActivityCallback.getViewportRect(DisplayUnit.PX)
        Log.d(LOG_TAG, "-> viewportRect -> $viewportRect")

        if (!Rect.intersects(viewportRect, currentSelectionRect)) {
            Log.i(LOG_TAG, "-> currentSelectionRect doesn't intersects viewportRect")
            uiHandler.post {
                popupWindow.dismiss()
                if(isScrollingRunnable!=null){
                    uiHandler.removeCallbacks(isScrollingRunnable!!)
                }
            }
            return
        }
        Log.i(LOG_TAG, "-> currentSelectionRect intersects viewportRect")

        if (selectionRect == currentSelectionRect) {
            Log.i(
                LOG_TAG, "-> setSelectionRect -> currentSelectionRect is equal to previous " +
                        "selectionRect so no need to computeTextSelectionRect and show popupWindow again"
            )
            return
        }

        Log.i(
            LOG_TAG, "-> setSelectionRect -> currentSelectionRect is not equal to previous " +
                    "selectionRect so computeTextSelectionRect and show popupWindow"
        )
        selectionRect = currentSelectionRect

        val aboveSelectionRect = Rect(viewportRect)
        aboveSelectionRect.bottom = selectionRect.top - (8 * density).toInt()
        val belowSelectionRect = Rect(viewportRect)
        belowSelectionRect.top = selectionRect.bottom + handleHeight

        //Log.d(LOG_TAG, "-> aboveSelectionRect -> " + aboveSelectionRect);
        //Log.d(LOG_TAG, "-> belowSelectionRect -> " + belowSelectionRect);

        // Priority to show popupWindow will be as following -
        // 1. Show popupWindow below selectionRect, if space available
        // 2. Show popupWindow above selectionRect, if space available
        // 3. Show popupWindow in the middle of selectionRect

        //popupRect initialisation for belowSelectionRect
        popupRect.left = viewportRect.left
        popupRect.top = belowSelectionRect.top
        popupRect.right = popupRect.left + viewTextSelection.measuredWidth
        popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight
        //Log.d(LOG_TAG, "-> Pre decision popupRect -> " + popupRect);

        val popupY: Int
        if (belowSelectionRect.contains(popupRect)) {
            Log.i(LOG_TAG, "-> show below")
            popupY = belowSelectionRect.top

        } else {

            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top
            popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight

            if (aboveSelectionRect.contains(popupRect)) {
                Log.i(LOG_TAG, "-> show above")
                popupY = aboveSelectionRect.bottom - popupRect.height()

            } else {

                Log.i(LOG_TAG, "-> show in middle")
                val popupYDiff = (viewTextSelection.measuredHeight - selectionRect.height()) / 2
                popupY = selectionRect.top - popupYDiff
            }
        }

        val popupXDiff = (viewTextSelection.measuredWidth - selectionRect.width()) / 2
        val popupX = selectionRect.left - popupXDiff

        popupRect.offsetTo(popupX, popupY)
        //Log.d(LOG_TAG, "-> Post decision popupRect -> " + popupRect);

        // Check if popupRect left side is going outside of the viewportRect
        if (popupRect.left < viewportRect.left) {
            popupRect.right += 0 - popupRect.left
            popupRect.left = 0
        }

        // Check if popupRect right side is going outside of the viewportRect
        if (popupRect.right > viewportRect.right) {
            val dx = popupRect.right - viewportRect.right
            popupRect.left -= dx
            popupRect.right -= dx
        }
    }

    private fun showTextSelectionPopup(id: String?) {
        val config = AppUtil.getSavedConfig(context)!!
        if(config.isShowTextSelection) {
            Log.v(LOG_TAG, "-> showTextSelectionPopup")
            Log.d(LOG_TAG, "-> showTextSelectionPopup -> To be laid out popupRect -> $popupRect")
            popupWindow.dismiss()
            oldScrollX = scrollX
            oldScrollY = scrollY

            isScrollingRunnable = Runnable {
                if(isScrollingRunnable!=null){
                    uiHandler.removeCallbacks(isScrollingRunnable!!)
                }
                val currentScrollX = scrollX
                val currentScrollY = scrollY
                val inTouchMode = lastTouchAction == MotionEvent.ACTION_DOWN ||
                        lastTouchAction == MotionEvent.ACTION_MOVE

                if (oldScrollX == currentScrollX && oldScrollY == currentScrollY && !inTouchMode) {
                    Log.i(LOG_TAG, "-> Stopped scrolling, show Popup")
                    popupWindow.dismiss()
                    popupWindow = PopupWindow(viewTextSelection, WRAP_CONTENT, WRAP_CONTENT)
                    popupWindow.isClippingEnabled = false
                    popupWindow.showAtLocation(
                        this@FolioWebView, Gravity.NO_GRAVITY,
                        popupRect.left, popupRect.top
                    )
                    if(id != null){
                        //选择的高亮部分
                        textUnderlineTextView!!.text ="删除划线"
                        highlightId = id
                    }
                } else {
                    Log.i(LOG_TAG, "-> Still scrolling, don't show Popup")
                    oldScrollX = currentScrollX
                    oldScrollY = currentScrollY
                    isScrollingCheckDuration += IS_SCROLLING_CHECK_TIMER
                    if (isScrollingCheckDuration < IS_SCROLLING_CHECK_MAX_DURATION && !destroyed) {
                        if(isScrollingRunnable!=null) {
                            uiHandler.postDelayed(isScrollingRunnable!!,
                                IS_SCROLLING_CHECK_TIMER.toLong())
                        }
                    }
                }
            }

            if(isScrollingRunnable!=null) {
                uiHandler.removeCallbacks(isScrollingRunnable!!)
            }
            isScrollingCheckDuration = 0
            if (!destroyed && isScrollingRunnable!=null) {
                uiHandler.postDelayed(isScrollingRunnable!!, IS_SCROLLING_CHECK_TIMER.toLong())
            }
        } else {
            Log.v(LOG_TAG, "-> doNotShowTextSelectionPopup")
        }

    }
}
