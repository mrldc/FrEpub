package com.folioreader.ui.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.webkit.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SCROLL_STATE_IDLE
import com.folioreader.Config
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.mediaoverlay.MediaController
import com.folioreader.mediaoverlay.MediaControllerCallbacks
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl
import com.folioreader.model.MarkVo
import com.folioreader.model.db.PageProgress
import com.folioreader.model.event.*
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.locators.SearchLocator
import com.folioreader.model.sqlite.BookmarkTable
import com.folioreader.model.sqlite.BooksTable
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.base.HtmlTask
import com.folioreader.ui.base.HtmlTaskCallback
import com.folioreader.ui.base.HtmlUtil
import com.folioreader.ui.view.*
import com.folioreader.util.AppUtil
import com.folioreader.util.HighlightUtil
import com.folioreader.util.UiUtil
import com.folioreader.viewmodels.PageTrackerViewModel
import com.scwang.smart.refresh.layout.api.RefreshLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.readium.r2.shared.Link
import org.readium.r2.shared.Locations
import java.util.*
import java.util.regex.Pattern
import kotlin.math.ceil


/**
 * Created by mahavir on 4/2/16.
 */
class FolioPageFragment(private var pageViewModel: PageTrackerViewModel) : Fragment(),
    HtmlTaskCallback, MediaControllerCallbacks, FolioWebView.SeekBarListener {
    override fun onResume() {
        super.onResume()
        pageViewModel.setCurrentPage(webViewPager.currentItem + 1)


        Log.v(LOG_TAG,"onResume--->"+spineIndex)
        //更新进度条
        updatePageProgress()

    }
    companion object {

        var lock ="lock"
        @JvmField
        val LOG_TAG: String = FolioPageFragment::class.java.simpleName

        private const val BUNDLE_SPINE_INDEX = "BUNDLE_SPINE_INDEX"
        private const val BUNDLE_BOOK_TITLE = "BUNDLE_BOOK_TITLE"
        private const val BUNDLE_SPINE_ITEM = "BUNDLE_SPINE_ITEM"
        private const val BUNDLE_READ_LOCATOR_CONFIG_CHANGE = "BUNDLE_READ_LOCATOR_CONFIG_CHANGE"
        const val BUNDLE_SEARCH_LOCATOR = "BUNDLE_SEARCH_LOCATOR"
        private const val BUNDLE_VIEW_MODEL = "BUNDLE_VIEW_MODEL"

        @JvmStatic
        fun newInstance(
            spineIndex: Int,
            bookTitle: String,
            spineRef: Link,
            bookId: String,
            viewModel: PageTrackerViewModel
        ): FolioPageFragment {
            val fragment = FolioPageFragment(viewModel)
            val args = Bundle()
            args.putInt(BUNDLE_SPINE_INDEX, spineIndex)
            args.putString(BUNDLE_BOOK_TITLE, bookTitle)
            args.putString(FolioReader.EXTRA_BOOK_ID, bookId)
            args.putSerializable(BUNDLE_SPINE_ITEM, spineRef)
            fragment.arguments = args
            return fragment
        }
        @JvmStatic
        fun newInstance(){

        }

    }

    //进度条信息
    public var pageProgress :PageProgress? = null
    private lateinit var uiHandler: Handler
    private var mHtmlString: String? = null
    private val hasMediaOverlay = false
    private var mAnchorId: String? = null
    private var rangy = ""
    private var highlightId: String? = null
    private var initPageNumber : Int? = null
    private var webViewOnFinished : Boolean = false
    private var currentPageNumber :Int = 0

    private var lastReadLocator: ReadLocator? = null
    private var pageMarkReadLocator: ReadLocator? = null
    private var outState: Bundle? = null
    private var savedInstanceState: Bundle? = null

    public var mRootView: View? = null

    private var loadingView: LoadingView? = null
    private var mScrollSeekbar: VerticalSeekbar? = null
    var mWebview: FolioWebView? = null
    lateinit var webViewPager: WebViewPager
    private var mPagesLeftTextView: TextView? = null
    private var mMinutesLeftTextView: TextView? = null
    private var mActivityCallback: FolioActivityCallback? = null
    private var refreshLayout : RefreshLayout? =null

    private var mTotalMinutes: Int = 0
    private var mFadeInAnimation: Animation? = null
    private var mFadeOutAnimation: Animation? = null

    lateinit var spineItem: Link
    private var spineIndex = -1
    private var mBookTitle: String? = null
    private var mIsPageReloaded: Boolean = false

    private var highlightStyle: String? = null

    private var mediaController: MediaController? = null
    private var mConfig: Config? = null
    public var mBookId: String? = null
    var searchLocatorVisible: SearchLocator? = null

    private var currentPageHasBookmark: Boolean = false

    private lateinit var chapterUrl: Uri

    private var ivBookmark: ImageView? = null
    private var ivPageNote: ImageView? = null

    //    var pageNo: IntArray = activity!!.intent!!.getIntArrayExtra("pageNo")
    val pageName: String
        get() = mBookTitle + "$" + spineItem.href

    private var bookmarkReadLocator: ReadLocator? = null

    private val isCurrentFragment: Boolean
        get() {
            return isAdded && mActivityCallback!!.currentChapterIndex == spineIndex
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {

        this.savedInstanceState = savedInstanceState
        uiHandler = Handler()

        if (activity is FolioActivityCallback)
            mActivityCallback = activity as FolioActivityCallback?

        EventBus.getDefault().register(this)

        spineIndex = arguments!!.getInt(BUNDLE_SPINE_INDEX)
        mBookTitle = arguments!!.getString(BUNDLE_BOOK_TITLE)
        spineItem = arguments!!.getSerializable(BUNDLE_SPINE_ITEM) as Link
        mBookId = arguments!!.getString(FolioReader.EXTRA_BOOK_ID)

        chapterUrl = Uri.parse(mActivityCallback?.streamerUrl + spineItem.href!!.substring(1))

        searchLocatorVisible = savedInstanceState?.getParcelable(BUNDLE_SEARCH_LOCATOR)

        if (spineItem != null) {
            // SMIL Parsing not yet implemented in r2-streamer-kotlin
            //if (spineItem.getProperties().contains("media-overlay")) {
            //    mediaController = new MediaController(getActivity(), MediaController.MediaType.SMIL, this);
            //    hasMediaOverlay = true;
            //} else {
            mediaController = MediaController(activity, MediaController.MediaType.TTS, this)
            mediaController!!.setTextToSpeech(activity)
            //}
        }
        highlightStyle =
            HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal)
        mRootView = inflater.inflate(R.layout.folio_page_fragment, container, false)
        mPagesLeftTextView = mRootView!!.findViewById<View>(R.id.pagesLeft) as TextView
        mMinutesLeftTextView = mRootView!!.findViewById<View>(R.id.minutesLeft) as TextView
        ivBookmark =  mRootView!!.findViewById<View>(R.id.iv_bookmark) as ImageView
        ivPageNote =  mRootView!!.findViewById<View>(R.id.iv_page_note) as ImageView
        mConfig = AppUtil.getSavedConfig(context)
        //页笔记点击
        ivPageNote!!.setOnClickListener{
            Log.v(LOG_TAG,"ACTION_PAGE_MARK-->页笔记编辑按钮点击")
            val readLocator = getLastReadLocator(FolioReader.ACTION_PAGE_MARK)
            if(readLocator != null){
                Log.v(FolioActivity.LOG_TAG, "笔记详情-->"+readLocator!!.href)
                //获取页笔记
                var markVo = BookmarkTable.getPageNote(mBookId,BookmarkTable.getReadLocatorString(readLocator),readLocator!!.locations.cfi)
                Log.v(FolioActivity.LOG_TAG, "笔记详情$markVo")
                if(markVo != null){
                    val noteDetailFragment = NoteDetailFragment(markVo.bookId,markVo.id,markVo.note,markVo.content,
                        MarkVo.PageNoteType,this,markVo.rangy)
                    noteDetailFragment.show(this.activity!!.supportFragmentManager,"")
                }
            }

        }
        loadingView = mRootView!!.findViewById(R.id.loadingView)
        setIndicatorVisibility()
        initSeekbar()
        initAnimations()
        initWebView()
        updatePagesLeftTextBg()
        //书签添加、删除监听
        initBookMarkListen()

        Log.d("FolioPageFragment", "onCreateView: initialised $spineIndex")

        return mRootView
    }

    /**
     * 书签添加、删除监听； 删除时需判断当前有无书签
     */
    private fun initBookMarkListen() {
        Log.v(LOG_TAG, "initBookMarkListen-->$currentPageHasBookmark-->chapter:$spineIndex")
        if(refreshLayout == null){
            refreshLayout  =  mRootView!!.findViewById<View>(R.id.refreshLayout) as RefreshLayout
        }

        //当前页有书签，添加头部为删除书签头部
        if(currentPageHasBookmark){
            ivBookmark!!.visibility = View.VISIBLE
            refreshLayout!!.setRefreshHeader(DeleteBookmarkHeaderView(context))
        }else{
            //当前页有书签，添加头部为添加书签头部
            refreshLayout!!.setRefreshHeader(AddBookmarkHeaderView(context))
            ivBookmark!!.visibility = View.GONE
        }

//        refreshLayout.setRefreshHeader(AddBookmarkHeaderView(context))
        refreshLayout!!.setEnableRefresh(true)
        refreshLayout!!.setHeaderMaxDragRate(6f)
        refreshLayout!!.setHeaderTriggerRate(0.4f)
        refreshLayout!!.setOnRefreshListener { refreshlayout ->
            Log.v(LOG_TAG,"setOnRefreshListener")
            refreshlayout.finishRefresh(10 )
            //进行
            getLastReadLocator(FolioReader.ACTION_BOOKMARK);
            initBookMarkListen()
            //书签添加、删除对应操作，通过变量值判断
//            if(haveBookMark) {
//                //删除
//            }else{
//                //添加
//                val readLocator = getLastReadLocator(FolioReader.ACTION_BOOKMARK)
//                Log.v(FolioActivity.LOG_TAG, "-> onOptionsItemSelected 'if' -> bookmark")
//
//                bookmarkReadLocator = readLocator
//                val localBroadcastManager = LocalBroadcastManager.getInstance(context!!)
//                val intent = Intent(FolioReader.ACTION_SAVE_READ_LOCATOR)
//                intent.putExtra(FolioReader.EXTRA_READ_LOCATOR, readLocator as Parcelable?)
//                localBroadcastManager.sendBroadcast(intent)
//            }

           // Toast.makeText(context, "刷新", Toast.LENGTH_LONG).show()

        }
    }

    /**
     * 是否允许书签下拉
     */
    fun setBookMarkStatus(status:Boolean){
        if(refreshLayout != null){
            refreshLayout!!.setEnableRefresh(status)

        }
    }


    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from [MediaControllerFragment.initListeners] when pause/play
     * button is clicked
     *
     * @param event of type [MediaOverlayPlayPauseEvent] contains if paused/played
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun pauseButtonClicked(event: MediaOverlayPlayPauseEvent) {
        if (isAdded && spineItem!!.href == event.href) {
            mediaController!!.stateChanged(event)
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from [MediaControllerFragment.initListeners] when speed
     * change buttons are clicked
     *
     * @param event of type [MediaOverlaySpeedEvent] contains selected speed
     * type HALF,ONE,ONE_HALF and TWO.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun speedChanged(event: MediaOverlaySpeedEvent) {
        if (mediaController != null)
            mediaController!!.setSpeed(event.speed)
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered from [MediaControllerFragment.initListeners] when new
     * style is selected on button click.
     *
     * @param event of type [MediaOverlaySpeedEvent] contains selected style
     * of type DEFAULT,UNDERLINE and BACKGROUND.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun styleChanged(event: MediaOverlayHighlightStyleEvent) {
        if (isAdded) {
            when (event.style) {
                MediaOverlayHighlightStyleEvent.Style.DEFAULT -> highlightStyle =
                    HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.Normal)
                MediaOverlayHighlightStyleEvent.Style.UNDERLINE -> highlightStyle =
                    HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.DottetUnderline)
                MediaOverlayHighlightStyleEvent.Style.BACKGROUND -> highlightStyle =
                    HighlightImpl.HighlightStyle.classForStyle(HighlightImpl.HighlightStyle.TextColor)
            }
            mWebview!!.loadUrl(
                String.format(
                    getString(R.string.setmediaoverlaystyle),
                    highlightStyle
                )
            )
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     * Function triggered when any EBook configuration is changed.
     *
     * @param reloadDataEvent empty POJO.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun reload(reloadDataEvent: ReloadDataEvent) {
        if (isCurrentFragment)
            getLastReadLocator("")
        initPageNumber = webViewPager!!.currentItem
        if (isAdded) {
            mWebview!!.dismissPopupWindow()
            mWebview!!.initViewTextSelection()
            loadingView!!.updateTheme()
            loadingView!!.show()
            mIsPageReloaded = true
            setHtml(true)
            updatePagesLeftTextBg()
            updatePageProgress()
        }
    }

    /**
     * [EVENT BUS FUNCTION]
     *
     *
     * Function triggered when highlight is deleted and page is needed to
     * be updated.
     *
     * @param event empty POJO.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun updateHighlight(event: UpdateHighlightEvent) {
        if (isAdded) {
            this.rangy = HighlightUtil.generateRangyString(pageName)
            loadRangy(this.rangy)
        }
    }

    fun scrollToAnchorId(href: String) {

        if (!TextUtils.isEmpty(href) && href.indexOf('#') != -1) {
            mAnchorId = href.substring(href.lastIndexOf('#') + 1)
            if (loadingView != null && loadingView!!.visibility != View.VISIBLE) {
                loadingView!!.show()
                mWebview!!.loadUrl(String.format(getString(R.string.go_to_anchor), mAnchorId))
                mAnchorId = null
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun resetCurrentIndex(resetIndex: RewindIndexEvent) {
        if (isCurrentFragment) {
            mWebview!!.loadUrl("javascript:rewindCurrentIndex()")
        }
    }

    override fun onReceiveHtml(html: String) {
        if (isAdded) {
            mHtmlString = html
            setHtml(false)

        }
    }

    private fun setHtml(reloaded: Boolean) {
        if (spineItem != null) {
            /*if (!reloaded && spineItem.properties.contains("media-overlay")) {
                mediaController.setSMILItems(SMILParser.parseSMIL(mHtmlString));
                mediaController.setUpMediaPlayer(spineItem.mediaOverlay, spineItem.mediaOverlay.getAudioPath(spineItem.href), mBookTitle);
            }*/
            mConfig = AppUtil.getSavedConfig(context)

            val href = spineItem.href
            var path = ""
            val forwardSlashLastIndex = href!!.lastIndexOf('/')
            if (forwardSlashLastIndex != -1) {
                path = href.substring(1, forwardSlashLastIndex + 1)
            }

            val mimeType: String =
                if (spineItem.typeLink!!.equals(getString(R.string.xhtml_mime_type), true)) {
                    getString(R.string.xhtml_mime_type)
                } else {
                    getString(R.string.html_mime_type)
                }

            uiHandler.post {
                mWebview!!.loadDataWithBaseURL(
                    mActivityCallback?.streamerUrl + path,
                    HtmlUtil.getHtmlContent(mWebview!!.context, mHtmlString, mConfig!!),
                    mimeType,
                    "UTF-8", null
                )
            }
        }
    }

    fun scrollToLast() {

        val isPageLoading = loadingView == null || loadingView!!.visibility == View.VISIBLE
        Log.v(LOG_TAG, "-> scrollToLast -> isPageLoading = $isPageLoading")

        if (!isPageLoading) {
            loadingView!!.show()
            mWebview!!.loadUrl("javascript:scrollToLast()")
        }
    }

    fun scrollToFirst() {

        val isPageLoading = loadingView == null || loadingView!!.visibility == View.VISIBLE
        Log.v(LOG_TAG, "-> scrollToFirst -> isPageLoading = $isPageLoading")

        if (!isPageLoading) {
            loadingView!!.show()
            mWebview!!.loadUrl("javascript:scrollToFirst()")
        }
    }
    fun scrollToProgress(progress : Float){
        if(pageProgress != null){
            //计算所在页面,(百分比-开始值)/(结束值-开始值) * 页面数，向上取整

            var toPageNumber = ceil((progress-pageProgress!!.start)/(pageProgress!!.end - pageProgress!!.start)*webViewPager.horizontalPageCount.toDouble()).toInt()
            if(toPageNumber > webViewPager.horizontalPageCount){
                toPageNumber = webViewPager.horizontalPageCount
            }
            webViewPager.currentItem  = toPageNumber
            if(progress == 100f){
                webViewPager.setPageToLast()
            }
        }

    }
    fun scrollToPage(pageNumber : Int){
        if(webViewOnFinished){
            webViewPager.currentItem  = pageNumber
        }else{
            this.initPageNumber = pageNumber
        }

    }
    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun initWebView() {

        val webViewLayout = mRootView!!.findViewById<FrameLayout>(R.id.webViewLayout)
        mWebview = webViewLayout.findViewById(R.id.folioWebView)
        mWebview!!.setParentFragment(this)
        mWebview!!.setBackgroundColor(0)
        mWebview!!.background.alpha = 0

        //加载图片资源
        webViewPager = webViewLayout.findViewById(R.id.webViewPager)

        webViewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                Log.v(LOG_TAG, "-> onPageScrolled ->pagePosition-> $position->chapter->$spineIndex-->positionOffsetPixels:$positionOffsetPixels")
                // pageViewModel.setCurrentPage(position + 1)

                mActivityCallback!!.toggleSystemUI(false)
            }

            override fun onPageSelected(position: Int) {
                pageViewModel.setCurrentPage(position + 1)
                Log.v(LOG_TAG, "-> onPageSelected ->pagePosition-> $position->chapter->$spineIndex")
                updatePageProgress()
                mWebview!!.dismissPopupWindowAndClearSelection()
            }

            override fun onPageScrollStateChanged(state: Int) {
                Log.v(LOG_TAG, "-> onPageScrollStateChanged -->state -> $state->chapter->$spineIndex -->webViewPager position:"+webViewPager.currentItem)
                if(state == SCROLL_STATE_IDLE){


                    //获取当前页位置，判断是否有标签
                    currentPageHasBookmark = false
                    Log.v(LOG_TAG,"ACTION_PAGE_MARK-->folioPageFragment onPageSelected")
                    getLastReadLocator(FolioReader.ACTION_CHECK_BOOKMARK+"|"+ FolioReader.ACTION_PAGE_MARK)
                    //更新阅读进度条
                    updatePageProgress()
                    //更新阅读记录
                    updateReadRecord()
                }

                // pageViewModel.setCurrentPage(webViewPager.currentItem + 1)

            }
        })

        if (activity is FolioActivityCallback)
            mWebview!!.setFolioActivityCallback((activity as FolioActivityCallback?)!!)

        setupScrollBar()
        mWebview!!.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            val height =
                Math.floor((mWebview!!.contentHeight * mWebview!!.scale).toDouble()).toInt()
            val webViewHeight = mWebview!!.measuredHeight
            mScrollSeekbar!!.maximum = height - webViewHeight
        }

        mWebview!!.settings.javaScriptEnabled = true
        mWebview!!.isVerticalScrollBarEnabled = false
        mWebview!!.settings.allowFileAccess = true

        mWebview!!.isHorizontalScrollBarEnabled = false

        mWebview!!.addJavascriptInterface(this, "Highlight")
        mWebview!!.addJavascriptInterface(this, "FolioPageFragment")
        mWebview!!.addJavascriptInterface(webViewPager, "WebViewPager")
        mWebview!!.addJavascriptInterface(loadingView!!, "LoadingView")
        mWebview!!.addJavascriptInterface(mWebview!!, "FolioWebView")

        mWebview!!.setScrollListener(object : FolioWebView.ScrollListener {
            override fun onScrollChange(percent: Int) {
                setIndicatorVisibility()
                mScrollSeekbar!!.setProgressAndThumb(percent)
                updatePagesLeftText(percent)
            }
        })

        mWebview!!.webViewClient = webViewClient
        mWebview!!.webChromeClient = webChromeClient

        mWebview!!.settings.defaultTextEncodingName = "utf-8"
        HtmlTask(this).execute(chapterUrl.toString())
    }

    private val webViewClient = object : WebViewClient() {

        override fun onPageFinished(view: WebView, url: String) {
            Log.v(LOG_TAG,"webViewClient onPageFinished -->chapter:$spineIndex")
            mWebview!!.loadUrl("javascript:checkCompatMode()")
            mWebview!!.loadUrl("javascript:alert(getReadingTime())")
            if (mActivityCallback!!.direction == Config.Direction.HORIZONTAL)
               initHorizontalDirection()

            view.loadUrl(
                String.format(
                    getString(R.string.setmediaoverlaystyle),
                    HighlightImpl.HighlightStyle.classForStyle(
                        HighlightImpl.HighlightStyle.Normal
                    )
                )
            )

            val rangy = HighlightUtil.generateRangyString(pageName)
            this@FolioPageFragment.rangy = rangy
            if (!rangy.isEmpty())
                loadRangy(rangy)

            if (mIsPageReloaded) {

                if (searchLocatorVisible != null) {
                    val callHighlightSearchLocator = String.format(
                        getString(R.string.callHighlightSearchLocator),
                        searchLocatorVisible?.locations?.cfi
                    )
                    mWebview!!.loadUrl(callHighlightSearchLocator)

                } else if (isCurrentFragment) {
                    if(lastReadLocator != null){
                        val cfi = lastReadLocator!!.locations!!.cfi!!
                        mWebview!!.loadUrl(String.format(getString(R.string.callScrollToCfi), cfi))
                    }


                } else {
                    if (spineIndex == mActivityCallback!!.currentChapterIndex - 1 && webViewOnFinished) {
                        // Scroll to last, the page before current page
                        Log.v(LOG_TAG,"webViewClient onPageFinished mIsPageReloaded scrollToLast")
                        mWebview!!.loadUrl("javascript:scrollToLast()")
                    } else {
                        // Make loading view invisible for all other fragments
                        loadingView!!.hide()
                    }
                }

                mIsPageReloaded = false

            } else if (!TextUtils.isEmpty(mAnchorId)) {
                mWebview!!.loadUrl(String.format(getString(R.string.go_to_anchor), mAnchorId))
                mAnchorId = null

            } else if (!TextUtils.isEmpty(highlightId)) {
                mWebview!!.loadUrl(String.format(getString(R.string.go_to_highlight), highlightId))
                highlightId = null

            } else if (searchLocatorVisible != null) {
                val callHighlightSearchLocator = String.format(
                    getString(R.string.callHighlightSearchLocator),
                    searchLocatorVisible?.locations?.cfi
                )
                mWebview!!.loadUrl(callHighlightSearchLocator)

            } else if (isCurrentFragment) {

                val readLocator: ReadLocator?
                if (savedInstanceState == null) {
                    Log.v(LOG_TAG, "-> onPageFinished -> took from getEntryReadLocator")
                    readLocator = mActivityCallback!!.entryReadLocator
                } else {
                    Log.v(LOG_TAG, "-> onPageFinished -> took from bundle")
                    readLocator =
                        savedInstanceState!!.getParcelable(BUNDLE_READ_LOCATOR_CONFIG_CHANGE)
                    savedInstanceState!!.remove(BUNDLE_READ_LOCATOR_CONFIG_CHANGE)
                }

                if (readLocator != null) {
                    val cfi = readLocator.locations.cfi
                    Log.v(LOG_TAG, "-> onPageFinished -> readLocator -> " + cfi!!)
                    mWebview!!.loadUrl(String.format(getString(R.string.callScrollToCfi), cfi))
                } else {
                    loadingView!!.hide()
                }


            } else {

                if (spineIndex == mActivityCallback!!.currentChapterIndex - 1 && webViewOnFinished) {
                    // Scroll to last, the page before current page
                    Log.v(LOG_TAG,"webViewClient onPageFinished scrollToLast -->chapter:$spineIndex")
                    mWebview!!.loadUrl("javascript:scrollToLast()")
                } else {
                    // Make loading view invisible for all other fragments
                    loadingView!!.hide()
                }
            }

        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

            if (url.isEmpty())
                return true

            val urlOfEpub = mActivityCallback!!.goToChapter(url)
            if (!urlOfEpub) {
                // Otherwise, give the default behavior (open in browser)
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                startActivity(intent)
            }

            return true
        }

        // prevent favicon.ico to be loaded automatically
        override fun shouldInterceptRequest(view: WebView, url: String): WebResourceResponse? {
            if (url.toLowerCase().contains("/favicon.ico")) {
                try {
                    return WebResourceResponse("image/png", null, null)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "shouldInterceptRequest failed", e)
                }

            }
            return null
        }

        // prevent favicon.ico to be loaded automatically
        @SuppressLint("NewApi")
        override fun shouldInterceptRequest(
            view: WebView,
            request: WebResourceRequest
        ): WebResourceResponse? {
            if (!request.isForMainFrame
                && request.url.path != null
                && request.url.path!!.endsWith("/favicon.ico")
            ) {
                try {
                    return WebResourceResponse("image/png", null, null)
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "shouldInterceptRequest failed", e)
                }

            }
            return null
        }
    }

    private val webChromeClient = object : WebChromeClient() {

        override fun onConsoleMessage(cm: ConsoleMessage): Boolean {
            super.onConsoleMessage(cm)
            val msg = cm.message() + " [" + cm.sourceId() + ":" + cm.lineNumber() + "]"
            return FolioWebView.onWebViewConsoleMessage(cm, "WebViewConsole", msg)
        }

        override fun onProgressChanged(view: WebView, progress: Int) {}

        override fun onJsAlert(
            view: WebView,
            url: String,
            message: String,
            result: JsResult
        ): Boolean {

            // Check if this `if` block can be dropped?
            if (!this@FolioPageFragment.isVisible)
                return true

            if (TextUtils.isDigitsOnly(message)) {
                try {
                    mTotalMinutes = Integer.parseInt(message)
                } catch (e: NumberFormatException) {
                    mTotalMinutes = 0
                }

            } else {
                // to handle TTS playback when highlight is deleted.
                val p =
                    Pattern.compile("[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}")
                if (!p.matcher(message).matches() && message != "undefined" && isCurrentFragment) {
                    mediaController!!.speakAudio(message)
                }
            }

            result.confirm()
            return true
        }
    }


    override fun onStop() {
        super.onStop()
        Log.v(LOG_TAG, "-> onStop -> " + spineItem.href + " -> " + isCurrentFragment)

        mediaController!!.stop()
        //TODO save last media overlay item

        if (isCurrentFragment)
            getLastReadLocator("")
    }
    fun getLastReadLocator(){
        this.getLastReadLocator("")
    }
    fun getLastReadLocator(actionType: String): ReadLocator? {
        Log.v(LOG_TAG, "-> getLastReadLocator -> " + actionType)
        try {
            synchronized(this) {
                mWebview!!.loadUrl(String.format(getString(R.string.callComputeLastReadCfi),actionType))
                (this as java.lang.Object).wait(500)
            }
        } catch (e: InterruptedException) {
            Log.e(LOG_TAG, "-> ", e)
        }

        return lastReadLocator
    }

    @JavascriptInterface
    fun storeLastReadCfi(actionTypes: String,cfi: String,title: String) {

        synchronized(this) {
            Log.v(LOG_TAG, "-> storeLastReadCfi -> actionType:$actionTypes cfi:$cfi title:$title");
            var href = spineItem.href
            if (href == null) href = ""
            val created = Date().time
            val locations = Locations()
            locations.cfi = cfi
            lastReadLocator = ReadLocator(mBookId!!, href, created,title, locations,null)
            var actionList = actionTypes!!.split("|")
            //书签操作
            if(actionList.contains(FolioReader.ACTION_BOOKMARK)){
                val intent = Intent(FolioReader.ACTION_SAVE_READ_LOCATOR)
                intent.putExtra(FolioReader.EXTRA_READ_LOCATOR, lastReadLocator as Parcelable?)
                if(currentPageHasBookmark){//删除书签
                    intent.putExtra(FolioReader.ACTION_TYPE, FolioReader.EXTRA_BOOKMARK_DELETE)
                    currentPageHasBookmark = false
                }else{//添加书签
                    intent.putExtra(FolioReader.ACTION_TYPE, FolioReader.EXTRA_BOOKMARK_ADD)
                    currentPageHasBookmark = true
                }
                intent.putExtra(FolioReader.EXTRA_BOOK_ID, mBookId)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }
            if(actionList.contains(FolioReader.ACTION_CHECK_BOOKMARK)){//检测是否有书签
                val bookmarkId = BookmarkTable.getBookmarkIdByCfi(href+cfi,mBookId,BookmarkTable.MARK_TYPE,context!!)
                Log.v(LOG_TAG,"check bookmark--->bookmarkId:$bookmarkId")
                currentPageHasBookmark = bookmarkId != -1
                uiHandler.post{initBookMarkListen()}

            }
            if(actionList.contains(FolioReader.ACTION_PAGE_MARK)){//检测是否有页笔记
                pageMarkReadLocator = lastReadLocator
                //获取页笔记
                var markVo = BookmarkTable.getPageNote(mBookId,BookmarkTable.getReadLocatorString(pageMarkReadLocator),pageMarkReadLocator!!.locations.cfi)
                Log.v(LOG_TAG,"check PAGE_MARK--->href:"+BookmarkTable.getReadLocatorString(pageMarkReadLocator)+"-->cfi:"+pageMarkReadLocator!!.locations.cfi+"-->markVo:$markVo")
                if(markVo != null){
                    uiHandler.post{ivPageNote!!.visibility = View.VISIBLE}
                }else{
                    uiHandler.post{ivPageNote!!.visibility = View.GONE}
                }
                val bookmarkId = BookmarkTable.getBookmarkIdByCfi(href+cfi,mBookId,BookmarkTable.MARK_TYPE,context!!)
                Log.v(LOG_TAG,"check PAGE_MARK--->bookmarkId:$bookmarkId")
                currentPageHasBookmark = bookmarkId != -1
                uiHandler.post{initBookMarkListen()}

            }
           /* if(actionList.contains(FolioReader.ACTION_READ_MARK)){
                //阅读记录
                val intent = Intent(FolioReader.ACTION_SAVE_READ_LOCATOR)
                intent.putExtra(FolioReader.EXTRA_READ_LOCATOR, lastReadLocator as Parcelable?)
                intent.putExtra(FolioReader.ACTION_TYPE, FolioReader.ACTION_READ_MARK)
                intent.putExtra(FolioReader.EXTRA_BOOK_ID, mBookId)
                LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
            }*/


            (this as java.lang.Object).notify()
        }
    }

    @JavascriptInterface
    fun setHorizontalPageCount(horizontalPageCount: Int) {
        Log.v(
            LOG_TAG, "-> setHorizontalPageCount = " + horizontalPageCount
                    + " -> " + spineItem.href +"->chapter:$spineIndex"
        )
        mWebview!!.setHorizontalPageCount(horizontalPageCount)
        //第一次完成页面时判断是否有跳转
        if(initPageNumber != null){
            uiHandler.post {
                webViewPager.currentItem = initPageNumber!!
            }
        }
        webViewOnFinished = true


    }

    fun loadRangy(rangy: String) {
        mWebview!!.loadUrl(
            String.format(
                "javascript:if(typeof ssReader !== \"undefined\"){ssReader.setHighlights('%s');}",
                rangy
            )
        )
    }
    //删除下划线
    fun unhighlightSelection(id: String?){
        mWebview!!.loadUrl(
            String.format(
                "javascript:if(typeof ssReader !== \"undefined\"){ssReader.unHighlightSelection('%s');}",
                id
            )
        )
    }
    private fun setupScrollBar() {
        UiUtil.setColorIntToDrawable(mConfig!!.currentThemeColor, mScrollSeekbar!!.progressDrawable)
        val thumbDrawable = ContextCompat.getDrawable(activity!!, R.drawable.icons_sroll)
        UiUtil.setColorIntToDrawable(mConfig!!.currentThemeColor, thumbDrawable!!)
        mScrollSeekbar!!.thumb = thumbDrawable
    }

    private fun initSeekbar() {
        mScrollSeekbar = mRootView!!.findViewById<View>(R.id.scrollSeekbar) as VerticalSeekbar
        mScrollSeekbar!!.progressDrawable
            .setColorFilter(
                resources
                    .getColor(R.color.default_theme_accent_color),
                PorterDuff.Mode.SRC_IN
            )
    }

    private fun setIndicatorVisibility() {
        if (mConfig != null) {
            mRootView?.findViewById<LinearLayout>(R.id.indicatorLayout)?.let { layout ->
                layout.visibility = if (mConfig!!.isShowRemainingIndicator) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    private fun updatePagesLeftTextBg() {

        if (mConfig!!.isNightMode) {
            mRootView!!.findViewById<View>(R.id.indicatorLayout)
                .setBackgroundColor(Color.parseColor("#131313"))
        } else {
            mRootView!!.findViewById<View>(R.id.indicatorLayout)
                .setBackgroundColor(Color.WHITE)
        }
    }

    private fun updatePagesLeftText(scrollY: Int) {
//        val intent = getIntent()
//        val message = intent.getStringExtra("pageNo")
//        Log.v(WebViewPager.LOG_TAG, "-> message -> $message")
    /*    try {
            val currentPage = (ceil(scrollY.toDouble() / mWebview!!.webViewHeight) + 1).toInt()
            val totalPages =
                ceil(mWebview!!.contentHeightVal.toDouble() / mWebview!!.webViewHeight).toInt()
            val pagesRemaining = totalPages - currentPage
            val pagesRemainingStrFormat = if (pagesRemaining > 1)
                getString(R.string.pages_left)
            else
                getString(R.string.page_left)
            val pagesRemainingStr = String.format(
                Locale.US,
                pagesRemainingStrFormat, pagesRemaining
            )

            val minutesRemaining =
                ceil((pagesRemaining * mTotalMinutes).toDouble() / totalPages).toInt()
            val minutesRemainingStr: String
            minutesRemainingStr = if (minutesRemaining > 1) {
                String.format(
                    Locale.US, getString(R.string.minutes_left),
                    minutesRemaining
                )
            } else if (minutesRemaining == 1) {
                String.format(
                    Locale.US, getString(R.string.minute_left),
                    minutesRemaining
                )
            } else {
                getString(R.string.less_than_minute)
            }

            mMinutesLeftTextView!!.text = minutesRemainingStr
            mPagesLeftTextView!!.text = pagesRemainingStr
        } catch (exp: java.lang.ArithmeticException) {
            Log.e("divide error", exp.toString())
        } catch (exp: IllegalStateException) {
            Log.e("divide error", exp.toString())
        }*/

    }

    private fun initAnimations() {
        mFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fadein)
        mFadeInAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
                mScrollSeekbar!!.visibility = View.VISIBLE
            }

            override fun onAnimationEnd(animation: Animation) {
                fadeOutSeekBarIfVisible()
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        mFadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fadeout)
        mFadeOutAnimation!!.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                mScrollSeekbar!!.visibility = View.INVISIBLE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
    }

    override fun fadeInSeekBarIfInvisible() {
        if (mScrollSeekbar!!.visibility == View.INVISIBLE || mScrollSeekbar!!.visibility == View.GONE) {
            mScrollSeekbar!!.startAnimation(mFadeInAnimation)
        }
    }

    fun fadeOutSeekBarIfVisible() {
        if (mScrollSeekbar!!.visibility == View.VISIBLE) {
            mScrollSeekbar!!.startAnimation(mFadeOutAnimation)
        }
    }

    override fun onDestroyView() {
        mFadeInAnimation!!.setAnimationListener(null)
        mFadeOutAnimation!!.setAnimationListener(null)
        EventBus.getDefault().unregister(this)

        super.onDestroyView()
    }

    /**
     * If called, this method will occur after onStop() for applications targeting platforms
     * starting with Build.VERSION_CODES.P. For applications targeting earlier platform versions
     * this method will occur before onStop() and there are no guarantees about whether it will
     * occur before or after onPause()
     *
     * @see Activity.onSaveInstanceState
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.v(LOG_TAG, "-> onSaveInstanceState -> ${spineItem.href}")

        this.outState = outState
        outState.putParcelable(BUNDLE_SEARCH_LOCATOR, searchLocatorVisible)
    }

    fun highlight(style: HighlightImpl.HighlightStyle,note:String, isAlreadyCreated: Boolean): Boolean {
        if (!isAlreadyCreated) {
           mWebview!!.loadUrl(
                String.format(
                    "javascript:if(typeof ssReader !== \"undefined\"){ssReader.highlightSelection('%s','%s');}",
                    HighlightImpl.HighlightStyle.classForStyle(style),note
                )
            )

        } else {
            mWebview!!.loadUrl(
                String.format(
                    "javascript:setHighlightStyle('%s')",
                    HighlightImpl.HighlightStyle.classForStyle(style)
                )
            )
        }
        return true
    }

    override fun resetCurrentIndex() {
        if (isCurrentFragment) {
            mWebview!!.loadUrl("javascript:rewindCurrentIndex()")
        }
    }
    //设置背景颜色
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun setHtmlBackground(event:ChangeBackgroundEvent){
        var color = event.color
        mConfig!!.backgroundColor = color
        AppUtil.saveConfig(activity, mConfig!!)
        mWebview!!.loadUrl(String.format("javascript:changeBackground('%s')",color))

    }
    @JavascriptInterface
    fun onReceiveHighlights(html: String?) {
        if (html != null) {
            rangy = HighlightUtil.createHighlightRangy(
                activity!!.applicationContext,
                html,
                mBookId,
                pageName,
                spineIndex,
                rangy,mWebview,uiHandler
            )
        }
    }
    //获取
    fun getFirstSentence(html: String?){

    }

    //划线重复
    @JavascriptInterface
    fun HighlightOverlap(result: Boolean){
        Log.v(LOG_TAG,"Highlightoverlap")
        uiHandler.post { Toast.makeText(activity,"此处已有划线或笔记，不允许划线或笔记交叉",Toast.LENGTH_LONG).show() }
    }
    override fun highLightText(fragmentId: String) {
        mWebview!!.loadUrl(String.format(getString(R.string.audio_mark_id), fragmentId))
    }

    override fun highLightTTS() {
        mWebview!!.loadUrl("javascript:alert(getSentenceWithIndex('epub-media-overlay-playing'))")
    }

    @JavascriptInterface
    fun getUpdatedHighlightId(id: String?, style: String) {
        if (id != null) {
            val highlightImpl = HighLightTable.updateHighlightStyle(id, style)
            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(
                    activity!!.applicationContext,
                    highlightImpl,
                    HighLight.HighLightAction.MODIFY
                )
            }
            val rangyString = HighlightUtil.generateRangyString(pageName)
            activity!!.runOnUiThread { loadRangy(rangyString) }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (isCurrentFragment) {
            if (outState != null)
                outState!!.putSerializable(BUNDLE_READ_LOCATOR_CONFIG_CHANGE, lastReadLocator)
            if (activity != null && !activity!!.isFinishing && lastReadLocator != null)
                mActivityCallback!!.storeLastReadLocator(lastReadLocator)
        }
        if (mWebview != null) mWebview!!.destroy()
    }

    override fun onError() {}

    fun scrollToHighlightId(highlightId: String) {
        this.highlightId = highlightId

        if (loadingView != null && loadingView!!.visibility != View.VISIBLE) {
            loadingView!!.show()
            mWebview!!.loadUrl(String.format(getString(R.string.go_to_highlight), highlightId))
            this.highlightId = null
        }
    }

    fun scrollToCFI(cfi: String) {
        if (loadingView != null && loadingView!!.visibility != View.VISIBLE) {
            loadingView!!.show()
            mWebview!!.loadUrl(String.format(getString(R.string.callScrollToCfi), cfi))
        }
    }

    fun highlightSearchLocator(searchLocator: SearchLocator) {
        Log.v(LOG_TAG, "-> highlightSearchLocator")
        this.searchLocatorVisible = searchLocator

        if (loadingView != null && loadingView!!.visibility != View.VISIBLE) {
            loadingView!!.show()
            val callHighlightSearchLocator = String.format(
                getString(R.string.callHighlightSearchLocator),
                searchLocatorVisible?.locations?.cfi
            )
            mWebview!!.loadUrl(callHighlightSearchLocator)
        }
    }

    fun clearSearchLocator() {
        Log.v(LOG_TAG, "-> clearSearchLocator -> " + spineItem.href!!)
        mWebview!!.loadUrl(getString(R.string.callClearSelection))
        searchLocatorVisible = null
    }

    fun emitPageDetails() {
        Log.d("MYTAG", "webPageAdapter: ")
//        return null;
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.v(LOG_TAG,"onConfigurationChanged")
        AppUtil.initHorizontalColumn(newConfig.orientation,activity)
        initHorizontalDirection()
        reload(ReloadDataEvent())
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun bookPageChange(bookPageEvent: BookPageEvent){
        initHorizontalDirection()
        reload(ReloadDataEvent())
    }
    fun initHorizontalDirection(){
        mConfig = AppUtil.getSavedConfig(activity);
        mWebview!!.loadUrl( String.format(
            "javascript:initHorizontalDirection('%s')",
            mConfig!!.columnCount
        ))

    }

    //更新进度条
    fun updatePageProgress(){
        Log.v(LOG_TAG,"updatePageProgress-->chapter:$spineIndex -->currentChapterIndex"+mActivityCallback!!.currentChapterIndex)
        if(pageProgress != null && spineIndex == mActivityCallback!!.currentChapterIndex){
            //章节的开始占比+当前章节的阅读进度 * 章节总占比
           var totalProgress = pageProgress!!.start + (pageProgress!!.end - pageProgress!!.start) * (mWebview!!.currentProgress/100)

            mActivityCallback!!.updateProgressUi(totalProgress)
        }
    }
    //更新阅读记录
    fun updateReadRecord(){
        var addReadRecord : Boolean
        var readBook = mActivityCallback!!.readRecord


        if(readBook == null){//没有阅读记录，新增
            addReadRecord = BooksTable(activity).insertBook(mBookId,null,null,null,null, "1",null,spineIndex,webViewPager.currentItem)
            if(addReadRecord){
                Log.v(FolioActivity.LOG_TAG,"新增阅读记录成功")
                readBook = BooksTable.getBookByBooKId(mBookId,activity)
                mActivityCallback!!.updateReadRecord(readBook)
            }
        }else{
            addReadRecord = BooksTable.updateBookPage(mBookId,spineIndex,webViewPager.currentItem,activity)
            readBook!!.chapterNumber = spineIndex
            readBook!!.pageNumber = webViewPager.currentItem
            if(addReadRecord){
                Log.v(FolioActivity.LOG_TAG,"更新阅读记录成功")
            }
        }
    }
}
