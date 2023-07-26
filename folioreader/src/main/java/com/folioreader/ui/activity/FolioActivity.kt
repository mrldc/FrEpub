/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.*
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.Constants.*
import com.folioreader.FolioReader
import com.folioreader.FolioReader.OnClosedListener
import com.folioreader.R
import com.folioreader.databinding.CustomTipViewBinding
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl
import com.folioreader.model.MarkVo
import com.folioreader.model.db.Book
import com.folioreader.model.db.PageProgress
import com.folioreader.model.event.HighlightNoteEvent
import com.folioreader.model.event.MediaOverlayPlayPauseEvent
import com.folioreader.model.event.NoteSelectEvent
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.locators.SearchLocator
import com.folioreader.model.sqlite.BookmarkTable
import com.folioreader.model.sqlite.BooksTable
import com.folioreader.model.sqlite.PageProgressTable
import com.folioreader.ui.adapter.FolioPageFragmentAdapter
import com.folioreader.ui.adapter.SearchAdapter
import com.folioreader.ui.base.PageProgressTask
import com.folioreader.ui.base.PageProgressTaskCallback
import com.folioreader.ui.fragment.*
import com.folioreader.ui.view.ConfigBottomSheetDialogFragment
import com.folioreader.ui.view.DirectionalViewpager
import com.folioreader.ui.view.FolioAppBarLayout
import com.folioreader.ui.view.MediaControllerCallback
import com.folioreader.util.*
import com.folioreader.util.AppUtil.Companion.getSavedConfig
import com.folioreader.util.Utils.setColorAlpha
import com.folioreader.viewmodels.PageTrackerViewModel
import com.folioreader.viewmodels.PageTrackerViewModelFactory
import com.litao.slider.NiftySlider
import com.litao.slider.anim.RevealTransition
import com.litao.slider.effect.ITEffect
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.readium.r2.shared.Link
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.CbzParser
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.parser.PubBox
import org.readium.r2.streamer.server.Server
import java.lang.ref.WeakReference
import java.util.*
import kotlin.math.ceil


class FolioActivity : AppCompatActivity(), FolioActivityCallback, MediaControllerCallback,
    OnHighlightListener, ReadLocatorListener, OnClosedListener,
    View.OnSystemUiVisibilityChangeListener,PageProgressTaskCallback {
    private var bookFileName: String? = null

    private var mFolioPageViewPager: DirectionalViewpager? = null
    private var actionBar: ActionBar? = null
    private var appBarLayout: FolioAppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var createdMenu: Menu? = null
    //全屏模式
    private var distractionFreeMode: Boolean = false
    private var handler: Handler? = null

    private var currentChapterIndex: Int = 0
    private var mFolioPageFragmentAdapter: FolioPageFragmentAdapter? = null
    private var entryReadLocator: ReadLocator? = null
    private var lastReadLocator: ReadLocator? = null
    private var bookmarkReadLocator: ReadLocator? = null
    private var pageMarkReadLocator: ReadLocator? = null
    private var folioReader: FolioReader? = null
    private var outState: Bundle? = null
    private var savedInstanceState: Bundle? = null

    private var r2StreamerServer: Server? = null
    private var pubBox: PubBox? = null
    private var spine: List<Link>? = null

    private var config: Config? = null
    private var mBookId: String? = null
    private var mEpubFilePath: String? = null
    private var mEpubSourceType: EpubSourceType? = null
    private var mEpubRawId = 0
    private var mediaControllerFragment: MediaControllerFragment? = null
    private var direction: Config.Direction = Config.Direction.HORIZONTAL
    private var portNumber: Int = DEFAULT_PORT_NUMBER
    private var streamerUri: Uri? = null

    private var searchUri: Uri? = null
    private var searchAdapterDataBundle: Bundle? = null
    private var searchQuery: CharSequence? = null
    private var searchLocator: SearchLocator? = null
    private var originBrightness : Int = 10

    private var tabIndex: Int = -1

    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var topActivity: Boolean? = null
    private var taskImportance: Int = 0
    private var ivBack: ImageView? = null
    private var flMain: FrameLayout? = null
    private var rlMain: RelativeLayout? = null
    private var tvLeft: TextView? = null
    private var ll_collect: LinearLayout? = null
    private var tv_collect: TextView? = null
    private var rl_comment: RelativeLayout? = null
    private var et_page_note: EditText? = null
    private var tv_mark_content: TextView? = null
    private var rl_mark_content: RelativeLayout? = null
    private var tv_page_save: TextView? = null

    private var tv_listen_book: TextView? = null
    private var ll_listen_book: LinearLayout? = null
    private var tv_video: TextView? = null
    private var ll_video: LinearLayout? = null
    private var iv_directory: ImageView? = null
    private var iv_write: ImageView? = null
    private var iv_light: ImageView? = null
    private var iv_font: ImageView? = null
    private var rl_top: RelativeLayout? = null
    private var rl_bottom: LinearLayout? = null
    private var rl_edit: LinearLayout? = null
    //阅读记录
    private var readBook: Book?  =null
    //页笔记编辑页
    private lateinit var viewNoteEdit: View

    //去听书
    private var hideListenAudeoL: Boolean = false
    private var HIDE_LISTEN_AUDEOL: String = "hideListenAudeoL"
    //看视频
    private var hideWatchVideo: Boolean = false
    private var HIDE_WATCHVIDEO: String = "hideWatchVideo"
    //收藏状态
    private var changeSaveButtonText:Boolean = false
    private var CHANGE_SAVE_BUTTONTEXT:String = "changeSaveButtonText"

    private var niftySlider : NiftySlider? = null
    private var customTipView : CustomTipViewBinding ?= null
    // page count
    private lateinit var pageTrackerViewModel: PageTrackerViewModel

    companion object {

        @JvmField
        val LOG_TAG: String = FolioActivity::class.java.simpleName

        const val INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path"
        const val INTENT_EPUB_SOURCE_TYPE = "epub_source_type"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        private const val BUNDLE_READ_LOCATOR_CONFIG_CHANGE = "BUNDLE_READ_LOCATOR_CONFIG_CHANGE"
        private const val BUNDLE_DISTRACTION_FREE_MODE = "BUNDLE_DISTRACTION_FREE_MODE"
        const val EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM"
        const val ACTION_SEARCH_CLEAR = "ACTION_SEARCH_CLEAR"
        private const val HIGHLIGHT_ITEM = "highlight_item"
        private const val BOOKMARK_ITEM = "bookmark_item"
    }

    private val closeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "-> closeBroadcastReceiver -> onReceive -> " + intent.action!!)

            val action = intent.action
            if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

                try {
                    val activityManager =
                        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.runningAppProcesses
                    taskImportance = tasks[0].importance
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "-> ", e)
                }

                val closeIntent = Intent(applicationContext, FolioActivity::class.java)
                closeIntent.flags =
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                closeIntent.action = FolioReader.ACTION_CLOSE_FOLIOREADER
                this@FolioActivity.startActivity(closeIntent)
            }
        }
    }

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) result = resources.getDimensionPixelSize(resourceId)
            return result
        }

    private val searchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.v(LOG_TAG, "-> searchReceiver -> onReceive -> " + intent.action!!)

            val action = intent.action ?: return
            when (action) {
                ACTION_SEARCH_CLEAR -> clearSearchLocator()
            }
        }
    }

    private val currentFragment: FolioPageFragment?
        get() = if (mFolioPageFragmentAdapter != null && mFolioPageViewPager != null &&  mFolioPageFragmentAdapter!!.getItem(mFolioPageViewPager!!.currentItem).mRootView != null) {
            mFolioPageFragmentAdapter!!.getItem(mFolioPageViewPager!!.currentItem) as FolioPageFragment
        } else {
            null
        }

    enum class EpubSourceType {
        RAW, ASSETS, SD_CARD
    }

    private enum class RequestCode(val value: Int) {
        CONTENT_HIGHLIGHT(77), SEARCH(101)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.v(LOG_TAG, "-> onNewIntent")

        val action = getIntent().action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

            if (topActivity == null || topActivity == false) {
                // FolioActivity was already left, so no need to broadcast ReadLocator again.
                // Finish activity without going through onPause() and onStop()
                finish()

                // To determine if app in background or foreground
                var appInBackground = false
                if (Build.VERSION.SDK_INT < 26) {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND == taskImportance) appInBackground =
                        true
                } else {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED == taskImportance) appInBackground =
                        true
                }
                if (appInBackground) moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.v(LOG_TAG, "-> onResume")
        topActivity = true

        val action = intent.action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {
            // FolioActivity is topActivity, so need to broadcast ReadLocator.
            finish()
        }
        handler!!.postDelayed({
            //读取章节位置信息-校验是否有书签
            Log.v(LOG_TAG,"ACTION_PAGE_MARK-->onResume ")
         /*   if(currentFragment != null){
                currentFragment!!.getLastReadLocator(FolioReader.ACTION_CHECK_BOOKMARK +"|" +FolioReader.ACTION_PAGE_MARK)
                //更新阅读进度条
                currentFragment!!.updatePageProgress()
            }*/
        },500)

        //当有阅读记录时，跳转
        if(readBook != null){
            gotoChapterByNumber(readBook!!.chapterNumber!!,readBook!!.pageNumber!!)
           // goToChapter(readBook!!.href,readBook!!.cfi)
        }

    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onMessageEvent(event: MessageEvent) {
//        //TODO 接收事件后Do something
//        rl_top!!.visibility = View.GONE
//        rl_bottom!!.visibility = View.GONE
//        rl_edit!!.visibility = View.GONE
//        flMain!!.visibility = View.GONE
//    }

    override fun onStop() {
        super.onStop()
        Log.v(LOG_TAG, "-> onStop")
        topActivity = false
        initScreenLight(originBrightness)
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v(LOG_TAG,"onCreate-->")
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        folioReader = FolioReader.get()
            .setOnHighlightListener(this)
            .setReadLocatorListener(this)
            .setOnClosedListener(this)
        // Need to add when vector drawables support library is used.
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)


        handler = Handler()

        val display = windowManager.defaultDisplay
        displayMetrics = resources.displayMetrics
        display.getRealMetrics(displayMetrics)
        density = displayMetrics!!.density
        LocalBroadcastManager.getInstance(this).registerReceiver(
            closeBroadcastReceiver, IntentFilter(FolioReader.ACTION_CLOSE_FOLIOREADER)
        )

        // Fix for screen get turned off while reading
        // TODO -> Make this configurable
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setConfig(savedInstanceState)

        initDistractionFreeMode(savedInstanceState)

        setContentView(R.layout.folio_activity)
        this.savedInstanceState = savedInstanceState

        if (savedInstanceState != null) {
            searchAdapterDataBundle = savedInstanceState.getBundle(SearchAdapter.DATA_BUNDLE)
            searchQuery =
                savedInstanceState.getCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY)
        }
        //配置阅读器
        config = getSavedConfig(applicationContext)
        if (config == null) config = Config()
        config!!.allowedDirection = Config.AllowedDirection.ONLY_HORIZONTAL
        config!!.isShowTextSelection = true
        //横屏双页
        AppUtil.saveConfig(this,config!!)
        AppUtil.initHorizontalColumn(resources.configuration.orientation,this)

        //获取其他参数
        hideListenAudeoL = intent.getBooleanExtra(this.HIDE_LISTEN_AUDEOL,false)
        hideWatchVideo = intent.getBooleanExtra(this.HIDE_WATCHVIDEO,false)
        changeSaveButtonText = intent.getBooleanExtra(this.CHANGE_SAVE_BUTTONTEXT,false)

        //外部传入电子书的路径
        var path  = intent.getStringExtra(FolioReader.BOOK_FILE_URL)
        if(path == null){
            //从共享文件夹读取文件
             path= applicationContext.getExternalFilesDir(
                 Environment.DIRECTORY_DOCUMENTS
             ).toString() + "/test.epub"

        }
        folioReader!!.setConfig(config, true)
        mBookId = intent.getStringExtra(FolioReader.EXTRA_BOOK_ID)
        //读取来源配置
        //从文件夹读取文件，开启此配置
        mEpubSourceType = EpubSourceType.SD_CARD
        //从assets中读取文件
       // mEpubSourceType = EpubSourceType.RAW

        //assets文件
       // mEpubRawId  = R.raw.test
        if(mEpubFilePath== null){
            mEpubFilePath = path
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//        initActionBar()
        initTopAndBottom()
        initMediaController()

        val pageCountTextView = findViewById<TextView>(R.id.pageCount)

        // pageTrackerViewModel
        pageTrackerViewModel = ViewModelProvider(
            this, PageTrackerViewModelFactory()
        )[PageTrackerViewModel::class.java]

        pageTrackerViewModel.chapterPage.observe(this, androidx.lifecycle.Observer {
            pageCountTextView.text = it
        })

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, getWriteExternalStoragePerms(), WRITE_EXTERNAL_STORAGE_REQUEST
                )
            } else {
                setupBook()
            }

        } else {
            setupBook()
        }
        //状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      //  window.statusBarColor = Color.TRANSPARENT;
       //  originBrightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS)
        val layoutParams: WindowManager.LayoutParams = getWindow().getAttributes()
        originBrightness = (layoutParams.screenBrightness*255).toInt()
        initScreenLight(config!!.light)
        selectBackground(config!!.lightBackground)
    }
    //初始化章节进度
    private fun initPageProgress() {
       //查询是否有计算过章节进度
        Log.v(LOG_TAG,"initPageProgress-->")
        PageProgressTask(this,this,streamerUrl,mBookId).execute(*spine!!.toTypedArray())
    }

    private fun initTopAndBottom() {
        flMain = findViewById(R.id.fl_main)
        rlMain = findViewById(R.id.rl_main)
        ivBack = findViewById(R.id.iv_back)
        tvLeft = findViewById(R.id.tv_left)
        ll_collect = findViewById(R.id.ll_collect)
        tv_collect = findViewById(R.id.tv_collect)
        tv_listen_book = findViewById(R.id.tv_listen_book)
        ll_listen_book = findViewById(R.id.ll_listen)
        tv_video = findViewById(R.id.tv_video)
        ll_video = findViewById(R.id.ll_video)
        rl_comment = findViewById(R.id.rl_comment)
        et_page_note = findViewById(R.id.et_page_note)
        tv_page_save = findViewById(R.id.tv_page_note_save)
        iv_directory = findViewById(R.id.iv_directory)
        iv_write = findViewById(R.id.iv_write)
        iv_light = findViewById(R.id.iv_light)
        iv_font = findViewById(R.id.iv_font)
        rl_top = findViewById(R.id.rl_top)
        rl_bottom = findViewById(R.id.rl_bottom)
        rl_edit = findViewById(R.id.rl_edit)
        tv_mark_content = findViewById(R.id.tv_mark_content)
        rl_mark_content = findViewById(R.id.rl_mark_content)

        rlMain!!.setOnClickListener(View.OnClickListener {
            hideSystemUI()
        })
        rl_edit!!.setOnClickListener(View.OnClickListener {

        })
        //进度条
        niftySlider = findViewById(R.id.ns_progress_bar);
        val activeTrackColor =
            setColorAlpha(ContextCompat.getColor(this, R.color.active_progress_color), 1f)
        val inactiveTrackColor = setColorAlpha(
            ContextCompat.getColor(this, R.color.inactive_progress_color),
            1f
        )
        val iconTintColor = setColorAlpha(
            ContextCompat.getColor(this, R.color.we_read_theme_color),
            0.7f
        )
        val animEffect = ITEffect(niftySlider!!).apply {
            startIconSize = 10.dp
            endIconSize = 14.dp
            startTintList = ColorStateList.valueOf(iconTintColor)
            endTintList = ColorStateList.valueOf(iconTintColor)
            startPadding = 12.dp
            endPadding = 12.dp

        }
        customTipView =  CustomTipViewBinding.bind(View.inflate(this,R.layout.custom_tip_view,null));
        niftySlider?.apply {
            effect = animEffect
            //添加自定义tip view
            addCustomTipView(customTipView!!.root)
            setTrackTintList(ColorStateList.valueOf(activeTrackColor))
            setTrackInactiveTintList(ColorStateList.valueOf(inactiveTrackColor))
            setOnIntValueChangeListener { slider, value, fromUser ->
                setThumbText(Utils.formatProgress(value))
            }
            //监听滑动开始/结束 控制滑块的显示状态
            niftySlider!!.setOnSliderTouchListener(object : NiftySlider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: NiftySlider) {
                    // slider.showThumb(false)
                }

                override fun onStopTrackingTouch(slider: NiftySlider) {
                    //slider.hideThumb(delayMillis = 2000)
                    //查询当前百分比在哪个页面
                    var progress = slider.value/100;
                   var pageProgress = PageProgressTable.getPageProgressByProgress(mBookId,progress,context)
                    //跳转页面
                   gotoPage(pageProgress,progress)

                }
            })

            //更换tip展示动画 - 揭露动画
            niftySlider!!.createTipAnimation {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    RevealTransition()
                } else {
                    null
                }
            }
        }


        tvLeft!!.text = bookFileName
        //返回
        ivBack?.setOnClickListener {
            topActivity = true
            finish()
        }
        //收藏
        if(changeSaveButtonText){
            tv_collect!!.text = "取消收藏"
        }else{
            tv_collect!!.text = "收藏"
        }
        ll_collect?.setOnClickListener {
            val intent = Intent(FolioReader.COLLECT_BOOK)
            if( tv_collect!!.text == "收藏"){
                tv_collect!!.text = "取消收藏"
            }else{
                tv_collect!!.text = "收藏"
            }
            if(changeSaveButtonText){
                intent.putExtra(FolioReader.COLLECT_BOOK_PARAM, false)
            }else{
                intent.putExtra(FolioReader.COLLECT_BOOK_PARAM, true)
            }
            sendBroadcast(intent)
        }
        //去听书
        //去听书是否显示
        if(hideListenAudeoL){
            ll_listen_book?.visibility = View.GONE
        }
        ll_listen_book?.setOnClickListener {
            val mLIntent = Intent("listenAudioNotification")
            sendBroadcast(mLIntent)
        }
        //看视频
        if(hideWatchVideo){
            ll_video?.visibility = View.GONE
        }
        ll_video?.setOnClickListener {
            val mWIntent = Intent("WatchVideoNotification")
            sendBroadcast(mWIntent)
        }
//        et_page_note?.addTextChangedListener(object : TextWatcher {
//            override fun beforeTextChanged(s: CharSequence, i: Int, i1: Int, i2: Int) {
//                InputMethodUtils.show(et_page_note)
//                tv_page_save!!.visibility = View.VISIBLE
//                val content = s.toString().trim { it <= ' ' }
//            }
//
//            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
//            override fun afterTextChanged(editable: Editable) {}
//        })
        //写笔记输入界面
        et_page_note?.setOnFocusChangeListener { view: View, hasFocus: Boolean ->
           /* if(hasFocus){
                Log.v(LOG_TAG,"点击写笔记输入界面")
                InputMethodUtils.show(et_page_note)
                //获取段落第一句

                Log.v(LOG_TAG,"ACTION_PAGE_MARK-->点击写笔记输入界面")
                val readLocator = currentFragment!!.getLastReadLocator(FolioReader.ACTION_PAGE_MARK)
                pageMarkReadLocator = readLocator;
                if(readLocator != null){
                    tv_mark_content!!.text = readLocator.title
                    rl_mark_content!!.visibility = View.VISIBLE
                }


            }*/


        }
        //保存页笔记
        tv_page_save?.setOnClickListener{

            var newNote = et_page_note!!.text.toString()

            currentFragment!!.highlight(HighlightImpl.HighlightStyle.mark,newNote,false)
            //关闭编辑
            rl_edit!!.visibility = View.GONE
            tv_mark_content!!.text = ""
            et_page_note!!.setText("")
            InputMethodUtils.close(et_page_note)
                //页笔记不要
           /* if(pageMarkReadLocator != null && et_page_note!!.text != null ){

                //查看当前页是否存在
                var markVo = BookmarkTable.getPageNote(mBookId,BookmarkTable.getReadLocatorString(pageMarkReadLocator),pageMarkReadLocator!!.locations.cfi) as MarkVo?
               var saveResult =if(markVo != null){
                   BookmarkTable.updateNote(et_page_note!!.text.toString(),markVo!!.id,this)
               } else {
                   BookmarkTable(this).insertBookmark(mBookId,pageMarkReadLocator!!.title,et_page_note!!.text.toString(),currentChapterIndex,BookmarkTable.getReadLocatorString(pageMarkReadLocator),pageMarkReadLocator!!.locations.cfi,BookmarkTable.NOTE_TYPE)

               }
                if(saveResult ){
                   et_page_note!!.text.clear()
                   et_page_note!!.clearFocus()
                   InputMethodUtils.close(et_page_note)
                   rl_mark_content!!.visibility = View.GONE
                   Toast.makeText(this,"保存成功",Toast.LENGTH_SHORT).show()

               }else{
                   Toast.makeText(this,"保存失败",Toast.LENGTH_SHORT).show()
               }


            }*/

        }
        //目录
        var ll_directory= findViewById<LinearLayout>(R.id.ll_directory)
        ll_directory?.setOnClickListener {
            tabController(true,false,false,false)
//            iv_directory!!.setImageResource(R.mipmap.ic_directory_select)
//            iv_write!!.setImageResource(R.mipmap.ic_note_select)
//            iv_light!!.setImageResource(R.mipmap.ic_day_night_select)
//            iv_font!!.setImageResource(R.mipmap.ic_font_select)

        }
        //笔记页面
        var ll_write= findViewById<LinearLayout>(R.id.ll_write)
        ll_write?.setOnClickListener {
            tabController(false,true,false,false)
           // startContentHighlightActivity()
//            iv_write!!.setImageResource(R.mipmap.ic_note_select)

        }
        //亮度、背景
        var ll_light= findViewById<LinearLayout>(R.id.ll_light)
        ll_light?.setOnClickListener {
            tabController(false,false,true,false)
//            iv_light!!.setImageResource(R.mipmap.ic_day_night_select)

        }
        //字体
        var ll_font= findViewById<LinearLayout>(R.id.ll_font)
        ll_font?.setOnClickListener {
            tabController(false,false,false,true)
//            iv_font!!.setImageResource(R.mipmap.ic_font_select)

//            showConfigBottomSheetDialogFragment()
        }

    }

    private fun statusIcon(directory : Boolean, write : Boolean, light : Boolean, font : Boolean) {
        iv_directory!!.setImageResource( if (directory) R.mipmap.ic_directory_select else R.mipmap.ic_directory)
        iv_write!!.setImageResource(if (write) R.mipmap.ic_note_select else R.mipmap.ic_write )
        iv_light!!.setImageResource( if (light) R.mipmap.ic_day_night_select else R.mipmap.ic_light )
        iv_font!!.setImageResource(if (font) R.mipmap.ic_font_select else R.mipmap.ic_font)
    }

    private fun initActionBar() {
        appBarLayout = findViewById(R.id.appBarLayout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar

        val config = AppUtil.getSavedConfig(applicationContext)!!

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_drawer)
        UiUtil.setColorIntToDrawable(config.currentThemeColor, drawable!!)
        toolbar!!.navigationIcon = drawable

        if (config.isNightMode) {
            setNightMode()
        } else {
            setDayMode()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val color: Int = if (config.isNightMode) {
                ContextCompat.getColor(this, R.color.black)
            } else {
                val attrs = intArrayOf(android.R.attr.navigationBarColor)
                val typedArray = theme.obtainStyledAttributes(attrs)
                typedArray.getColor(0, ContextCompat.getColor(this, R.color.white))
            }
            window.navigationBarColor = color
        }

        if (Build.VERSION.SDK_INT < 16) {
            // Fix for appBarLayout.fitSystemWindows() not being called on API < 16
            appBarLayout!!.setTopMargin(statusBarHeight)
        }
    }

    override fun setDayMode() {
        Log.v(LOG_TAG, "-> setDayMode")

        actionBar!!.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.white))
        )

        toolbar!!.setTitleTextColor(ContextCompat.getColor(this, R.color.black))

        config = AppUtil.getSavedConfig(applicationContext)!!

        // Update drawer color
        val newNavIcon = toolbar!!.navigationIcon
        UiUtil.setColorIntToDrawable(config!!.themeColor, newNavIcon)
        toolbar!!.navigationIcon = newNavIcon

        // Update toolbar colors
        createdMenu?.let { m ->
            UiUtil.setColorIntToDrawable(config!!.themeColor, m.findItem(R.id.itemBookmark).icon)
            UiUtil.setColorIntToDrawable(config!!.themeColor, m.findItem(R.id.itemSearch).icon)
            UiUtil.setColorIntToDrawable(config!!.themeColor, m.findItem(R.id.itemConfig).icon)
            UiUtil.setColorIntToDrawable(config!!.themeColor, m.findItem(R.id.itemTts).icon)
        }

        toolbar?.getOverflowIcon()?.setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_ATOP);

    }

    override fun setNightMode() {
        Log.v(LOG_TAG, "-> setNightMode")

        actionBar!!.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(this, R.color.black))
        )

        toolbar!!.setTitleTextColor(ContextCompat.getColor(this, R.color.night_title_text_color))

        config = AppUtil.getSavedConfig(applicationContext)!!

        // Update drawer color
        val newNavIcon = toolbar!!.navigationIcon
        UiUtil.setColorIntToDrawable(config!!.nightThemeColor, newNavIcon)
        toolbar!!.navigationIcon = newNavIcon

        // Update toolbar colors
        createdMenu?.let { m ->
            UiUtil.setColorIntToDrawable(config!!.nightThemeColor, m.findItem(R.id.itemBookmark).icon)
            UiUtil.setColorIntToDrawable(config!!.nightThemeColor, m.findItem(R.id.itemSearch).icon)
            UiUtil.setColorIntToDrawable(config!!.nightThemeColor, m.findItem(R.id.itemConfig).icon)
            UiUtil.setColorIntToDrawable(config!!.nightThemeColor, m.findItem(R.id.itemTts).icon)
        }

        toolbar?.getOverflowIcon()?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

    }

    private fun initMediaController() {
        Log.v(LOG_TAG, "-> initMediaController")

        mediaControllerFragment = MediaControllerFragment.getInstance(supportFragmentManager, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        try {
            createdMenu = menu
            menuInflater.inflate(R.menu.menu_main, menu)

            val config = AppUtil.getSavedConfig(applicationContext)!!

//            toolbar?.getOverflowIcon()?.setColorFilter(config.currentThemeColor, PorterDuff.Mode.SRC_ATOP);
//            for (i in 0 until menu.size()) {
//                val drawable: Drawable = menu.getItem(i).getIcon()
//                if (drawable != null) {
//                    drawable.mutate()
//                    drawable.setColorFilter(
//                        config.currentThemeColor,
//                        PorterDuff.Mode.SRC_ATOP
//                    )
//                }
//            }


            UiUtil.setColorIntToDrawable(
                config.currentThemeColor, menu.findItem(R.id.itemBookmark).icon
            )
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor, menu.findItem(R.id.itemSearch).icon
            )
            UiUtil.setColorIntToDrawable(
                config.currentThemeColor, menu.findItem(R.id.itemConfig).icon
            )
            UiUtil.setColorIntToDrawable(config.currentThemeColor, menu.findItem(R.id.itemTts).icon)

            if (!config.isShowTts) menu.findItem(R.id.itemTts).isVisible = false
        } catch (e: Exception) {
            Log.e("FOLIOREADER", e.message.toString())
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //Log.d(LOG_TAG, "-> onOptionsItemSelected -> " + item.getItemId());

        when (item.itemId) {
            android.R.id.home -> {
                Log.v(LOG_TAG, "-> onOptionsItemSelected -> drawer")
               // startContentHighlightActivity()
                return true
            }
            R.id.itemBookmark -> {
                val readLocator = currentFragment!!.getLastReadLocator(FolioReader.ACTION_BOOKMARK)
                Log.v(LOG_TAG, "-> onOptionsItemSelected 'if' -> bookmark")

                bookmarkReadLocator = readLocator
                val localBroadcastManager = LocalBroadcastManager.getInstance(this)
                val intent = Intent(FolioReader.ACTION_SAVE_READ_LOCATOR)
                intent.putExtra(FolioReader.EXTRA_READ_LOCATOR, readLocator as Parcelable?)
                localBroadcastManager.sendBroadcast(intent)
                //保存书签
                /*val simpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                val insertResult = BookmarkTable(this).insertBookmark(
                    mBookId,
                    simpleDateFormat.format(Date()),
                    bookmarkReadLocator!!.title,
                    bookmarkReadLocator!!.toJson().toString(),
                    bookmarkReadLocator!!.locations.cfi
                )
                if(insertResult){
                    Toast.makeText(
                        this, "已添加到书签", Toast.LENGTH_SHORT
                    ).show()
                }*/
                /*val dialog = Dialog(this, R.style.DialogCustomTheme)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.dialog_bookmark)
                dialog.show()
                dialog.setCanceledOnTouchOutside(true)
                dialog.setOnCancelListener {
                    Toast.makeText(
                        this, "please enter a Bookmark name and then press Save", Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.findViewById<View>(R.id.btn_save_bookmark).setOnClickListener {
                    val name =
                        (dialog.findViewById<View>(R.id.bookmark_name) as EditText).text.toString()
                    if (!TextUtils.isEmpty(name)) {
                        val simpleDateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())
                        val id = BookmarkTable(this).insertBookmark(
                            mBookId,
                            simpleDateFormat.format(Date()),
                            bookmarkReadLocator!!.title,
                            bookmarkReadLocator!!.toJson().toString()
                        )
                        Toast.makeText(
                            this, getString(R.string.book_mark_success), Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this,
                            "please Enter a Bookmark name and then press Save",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dialog.dismiss()
                }*/


                return true
            }
            R.id.itemSearch -> {
                Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)
                if (searchUri == null) return true
                val intent = Intent(this, SearchActivity::class.java)
                intent.putExtra(SearchActivity.BUNDLE_SPINE_SIZE, spine?.size ?: 0)
                intent.putExtra(SearchActivity.BUNDLE_SEARCH_URI, searchUri)
                intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
                intent.putExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery)
                startActivityForResult(intent, RequestCode.SEARCH.value)
                return true

            }
            R.id.itemConfig -> {
                Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)
                showConfigBottomSheetDialogFragment()
                return true

            }
            R.id.itemTts -> {
                Log.v(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)
                showMediaController()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }

    }

    private fun goToTableOfCOntentActivity() {
        var cfi = if(readBook == null){""}else readBook!!.cfi
        var pageNumber = if(readBook == null){null}else readBook!!.pageNumber
        val tableOfContentFragment = TableOfContentFragment.newInstance(
            pubBox!!.publication,
            spine!![currentChapterIndex].href,
            bookFileName,
            cfi,
            pageNumber
        )
        tableOfContentFragment.setActivityCallback(this);
        val ft =
            supportFragmentManager.beginTransaction()
        ft.replace(R.id.fl_main, tableOfContentFragment)
        ft.commit()
//
    }
    private fun gaToHighlightFragment(){
        val bookId = mBookId
        val bookTitle = bookFileName
        val noteFragment = NoteFragment.newInstance(bookId, bookTitle,this)
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fl_main, noteFragment)
        ft.commit()
    }
    /**
     * 修改为跳转笔记主页
     */
    private fun startContentHighlightActivity() {

//        val intent = Intent(this@FolioActivity, ContentHighlightActivity::class.java)
        val intent = Intent(this@FolioActivity, ContentTestActivity::class.java)

        intent.putExtra(Constants.PUBLICATION, pubBox!!.publication)
        try {
            intent.putExtra(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
        } catch (e: NullPointerException) {
            Log.w(LOG_TAG, "-> ", e)
            intent.putExtra(CHAPTER_SELECTED, "")
        } catch (e: IndexOutOfBoundsException) {
            Log.w(LOG_TAG, "-> ", e)
            intent.putExtra(CHAPTER_SELECTED, "")
        }

        intent.putExtra(FolioReader.EXTRA_BOOK_ID, mBookId)
        intent.putExtra(BOOK_TITLE, bookFileName)

       startActivityForResult(intent, RequestCode.CONTENT_HIGHLIGHT.value)
        overridePendingTransition(R.anim.slide_in_left, R.anim.disappear)

        /* val contentFrameLayout = TableOfContentFragment.newInstance(
            pubBox!!.publication,
            spine!![currentChapterIndex].href,
            bookFileName
        )
        val ft = supportFragmentManager.beginTransaction()
        ft.replace(R.id.parent, contentFrameLayout)
        ft.commit()*/
    }

    private fun showConfigBottomSheetDialogFragment() {
        ConfigBottomSheetDialogFragment().show(
            supportFragmentManager, ConfigBottomSheetDialogFragment.LOG_TAG
        )
    }

    private fun showMediaController() {
        mediaControllerFragment!!.show(supportFragmentManager)
    }

    private fun setupBook() {
        Log.v(LOG_TAG, "-> setupBook")
        try {
            initBook()
            onBookInitSuccess()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> Failed to initialize book", e)
            onBookInitFailure()
        }

    }

    @Throws(Exception::class)
    private fun initBook() {
        Log.v(LOG_TAG, "-> initBook")

        bookFileName = FileUtil.getEpubFilename(this, mEpubSourceType!!, mEpubFilePath, mEpubRawId)
        Log.v(LOG_TAG, "-> bookFileName $bookFileName")

        val path = FileUtil.saveEpubFileAndLoadLazyBook(
            this, mEpubSourceType, mEpubFilePath, mEpubRawId, bookFileName
        )
        Log.e("TAG", "mPath: $path")
        val extension: Publication.EXTENSION
        var extensionString: String? = null
        try {
            extensionString = FileUtil.getExtensionUppercase(path)
            extension = Publication.EXTENSION.valueOf(extensionString)
        } catch (e: IllegalArgumentException) {
            throw Exception("-> Unknown book file extension `$extensionString`", e)
        }


        pubBox = when (extension) {
            Publication.EXTENSION.EPUB -> {
                val epubParser = EpubParser()
                epubParser.parse(path!!, "")
            }
            Publication.EXTENSION.CBZ -> {
                val cbzParser = CbzParser()
                cbzParser.parse(path!!, "")
            }
            else -> {
                null
            }
        }


        portNumber = folioReader!!.portNumber
        portNumber = AppUtil.getAvailablePortNumber(portNumber)

        r2StreamerServer = Server(portNumber)
        r2StreamerServer!!.addEpub(
            pubBox!!.publication, pubBox!!.container, "/" + bookFileName!!, null
        )

        r2StreamerServer!!.start()

        FolioReader.initRetrofit(streamerUrl)
    }

    private fun onBookInitFailure() {
        //TODO -> Fail gracefully
    }

    private fun onBookInitSuccess() {
        val publication = pubBox!!.publication
        spine = publication.readingOrder
        title = publication.metadata.title

        if (mBookId == null) {
           /* mBookId = publication.metadata.identifier.ifEmpty {
                if (publication.metadata.title.isNotEmpty()) {
                    publication.metadata.title.hashCode().toString()
                } else {
                    bookFileName!!.hashCode().toString()
                }
            }*/
            mBookId =  publication.metadata.identifier +"-"+bookFileName

        }
        //获取当前阅读记录
        readBook = BooksTable.getBookByBooKId(mBookId,this)


        // searchUri currently not in use as it's uri is constructed through Retrofit,
        // code kept just in case if required in future.
        for (link in publication.links) {
            if (link.rel.contains("search")) {
                searchUri = Uri.parse("http://" + link.href!!)
                break
            }
        }
        if (searchUri == null) searchUri = Uri.parse(streamerUrl + "search")

        configFolio()
    }

    override fun getStreamerUrl(): String {

        if (streamerUri == null) {
            streamerUri = Uri.parse(
                String.format(
                    STREAMER_URL_TEMPLATE, LOCALHOST, portNumber.toString(), bookFileName
                )
            )
        }
        return streamerUri.toString()
    }

    override fun updateProgressUi(percent: Double) {
       niftySlider!!.setValue((percent*100).toFloat(),true)
    }

    override fun getReadRecord(): Book? {
        return readBook
    }

    override fun updateReadRecord(book: Book?) {
        readBook = book
    }

    override fun selectBackground(index: Int) {
        config!!.lightBackground = index
        AppUtil.saveConfig(this, config!!)
        if(index == 1){
            mFolioPageViewPager!!.setBackgroundResource(R.mipmap.read_background)
        }else if(index == 2){
            mFolioPageViewPager!!.setBackgroundResource(R.mipmap.read_background2)
        }else if(index == 3){
            mFolioPageViewPager!!.setBackgroundResource(R.mipmap.read_background3)
        }else if(index == 4){
            mFolioPageViewPager!!.setBackgroundResource(R.mipmap.read_background4)
        }else{
            mFolioPageViewPager!!.setBackgroundResource(R.mipmap.read_background)
        }
    }

    override fun tabController(
        directory: Boolean,
        write: Boolean,
        light: Boolean,
        font: Boolean
    ) {
        //目录
        if(directory){
            if (currentFragment!!.mWebview != null) currentFragment!!.mWebview!!.dismissPopupWindowAndClearSelection()
            if(rlMain!!.visibility == View.VISIBLE){
                //判读是否是当前页面

                if (tabIndex == 0) {
                    //关闭当前
                    statusIcon(false, false, false, false)
                    rlMain!!.visibility = View.GONE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.VISIBLE
                    rl_top!!.visibility = View.VISIBLE
                    tabIndex = -1
                } else {
                    //显示
                    statusIcon(true, false, false, false)
                    goToTableOfCOntentActivity()
                    rlMain!!.visibility = View.VISIBLE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.GONE
                    rl_top!!.visibility = View.GONE
                    tabIndex = 0
                }


            }else{
                statusIcon(true, false, false, false)
                goToTableOfCOntentActivity()
                rlMain!!.visibility = View.VISIBLE
                rl_edit!!.visibility = View.GONE
                niftySlider!!.visibility = View.GONE
                rl_top!!.visibility = View.GONE
                tabIndex = 0
            }
        }
        //笔记
        else if(write){
            if (currentFragment!!.mWebview != null) currentFragment!!.mWebview!!.dismissPopupWindowAndClearSelection()

            if(rlMain!!.visibility == View.VISIBLE){
                if(tabIndex == 1){
                    //关闭
                    statusIcon(false, false, false, false)
                    rlMain!!.visibility = View.GONE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.VISIBLE
                    rl_top!!.visibility = View.VISIBLE
                    tabIndex = -1
                }else{
                    //显示
                    statusIcon(false, true, false, false)
                    gaToHighlightFragment()
                    rlMain!!.visibility = View.VISIBLE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.GONE
                    rl_top!!.visibility = View.GONE
                    tabIndex = 1
                }

            }else{
                statusIcon(false, true, false, false)
                gaToHighlightFragment()
                rlMain!!.visibility = View.VISIBLE
                rl_edit!!.visibility = View.GONE
                niftySlider!!.visibility = View.GONE
                rl_top!!.visibility = View.GONE
                tabIndex = 1
            }
        }
        //亮度
        else if(light){
            if (currentFragment!!.mWebview != null) currentFragment!!.mWebview!!.dismissPopupWindowAndClearSelection()

            if(rlMain!!.visibility == View.VISIBLE){
                if(tabIndex == 2){
                    //关闭
                    statusIcon(false, false, false, false)
                    rlMain!!.visibility = View.GONE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.VISIBLE
                    rl_top!!.visibility = View.VISIBLE
                    tabIndex = -1
                }else{
                    //显示
                    statusIcon(false, false, true, false)
                    val ft: FragmentTransaction =
                        supportFragmentManager.beginTransaction()
                    ft.replace(R.id.fl_main, LightFragment(this))
                    ft.commit()
                    rlMain!!.visibility = View.VISIBLE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.GONE
                    rl_top!!.visibility = View.GONE
                    tabIndex = 2
                }

            }else{
                statusIcon(false, false, true, false)
                val ft: FragmentTransaction =
                    supportFragmentManager.beginTransaction()
                ft.replace(R.id.fl_main, LightFragment(this))
                ft.commit()
                rlMain!!.visibility = View.VISIBLE
                rl_edit!!.visibility = View.GONE
                niftySlider!!.visibility = View.GONE
                rl_top!!.visibility = View.GONE
                tabIndex = 2
            }
        }
        //字体
        else if(font){
            if (currentFragment!!.mWebview != null) currentFragment!!.mWebview!!.dismissPopupWindowAndClearSelection()

            if(rlMain!!.visibility == View.VISIBLE){
                if(tabIndex == 3){
                    //关闭
                    statusIcon(false, false, false, false)
                    rlMain!!.visibility = View.GONE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.VISIBLE
                    rl_top!!.visibility = View.VISIBLE
                    tabIndex = -1
                }else{
                    //显示
                    statusIcon(false, false, false, true)
                    val ft: FragmentTransaction =
                        supportFragmentManager.beginTransaction()
                    ft.replace(R.id.fl_main, FontFragment())
                    ft.commit()
                    rlMain!!.visibility = View.VISIBLE
                    rl_edit!!.visibility = View.GONE
                    niftySlider!!.visibility = View.GONE
                    rl_top!!.visibility = View.GONE
                    tabIndex = 3
                }

            }else{
                statusIcon(false, false, false, true)
                val ft: FragmentTransaction =
                    supportFragmentManager.beginTransaction()
                ft.replace(R.id.fl_main, FontFragment())
                ft.commit()
                rlMain!!.visibility = View.VISIBLE
                rl_edit!!.visibility = View.GONE
                niftySlider!!.visibility = View.GONE
                rl_top!!.visibility = View.GONE
                tabIndex = 3
            }
        }else{
            statusIcon(false, false, false, false)
            rlMain!!.visibility = View.GONE
            rl_edit!!.visibility = View.GONE
            niftySlider!!.visibility = View.VISIBLE
            rl_top!!.visibility = View.VISIBLE
        }
    }

    override fun onDirectionChange(newDirection: Config.Direction) {
        Log.v(LOG_TAG, "-> onDirectionChange")

        var folioPageFragment: FolioPageFragment? = currentFragment ?: return
        entryReadLocator = folioPageFragment!!.getLastReadLocator("")
        val searchLocatorVisible = folioPageFragment.searchLocatorVisible

        direction = newDirection

        mFolioPageViewPager!!.setDirection(newDirection)
        var pageProgressList = PageProgressTable.getAllPageProgress(mBookId,this)

        mFolioPageFragmentAdapter = FolioPageFragmentAdapter(
            supportFragmentManager, spine, bookFileName, mBookId, pageTrackerViewModel
        )
        mFolioPageFragmentAdapter!!.pageProgressesList = pageProgressList
        mFolioPageViewPager!!.adapter = mFolioPageFragmentAdapter
        mFolioPageViewPager!!.currentItem = currentChapterIndex

        folioPageFragment = currentFragment ?: return
        searchLocatorVisible?.let {
            folioPageFragment.highlightSearchLocator(searchLocatorVisible)
        }
    }

    private fun initDistractionFreeMode(savedInstanceState: Bundle?) {
        if(Build.VERSION.SDK_INT >= 30){
            //监听状态栏变化
            window.decorView.setOnApplyWindowInsetsListener { view:View,insets->
                distractionFreeMode = !insets.isVisible(WindowInsets.Type.statusBars())
                systemUiVisibilityChange()

                return@setOnApplyWindowInsetsListener insets
            }
        }else{
            window.decorView.setOnSystemUiVisibilityChangeListener(this)
        }


        // Deliberately Hidden and shown to make activity contents lay out behind SystemUI

        showSystemUI()
        hideSystemUI()
        distractionFreeMode =
            savedInstanceState != null && savedInstanceState.getBoolean(BUNDLE_DISTRACTION_FREE_MODE)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.v(LOG_TAG, "-> onPostCreate")

        if (distractionFreeMode) {
            handler!!.post { hideSystemUI() }
        }
    }

    /**
     * @return returns height of status bar + app bar as requested by param [DisplayUnit]
     */
    override fun getTopDistraction(unit: DisplayUnit): Int {

        var topDistraction = 0
        if (!distractionFreeMode) {
            //topDistraction = statusBarHeight
            if (actionBar != null) topDistraction += actionBar!!.height
        }

        return when (unit) {
            DisplayUnit.PX -> topDistraction

            DisplayUnit.DP -> {
                topDistraction /= density.toInt()
                topDistraction
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }

    /**
     * Calculates the bottom distraction which can cause due to navigation bar.
     * In mobile landscape mode, navigation bar is either to left or right of the screen.
     * In tablet, navigation bar is always at bottom of the screen.
     *
     * @return returns height of navigation bar as requested by param [DisplayUnit]
     */
    override fun getBottomDistraction(unit: DisplayUnit): Int {

        var bottomDistraction = 0
        if (!distractionFreeMode) bottomDistraction = rl_bottom!!.height

        return when (unit) {
            DisplayUnit.PX -> bottomDistraction

            DisplayUnit.DP -> {
                bottomDistraction /= density.toInt()
                bottomDistraction
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }

    /**
     * Calculates the Rect for visible viewport of the webview in PX.
     * Visible viewport changes in following cases -
     * 1. In distraction free mode,
     * 2. In mobile landscape mode as navigation bar is placed either on left or right side,
     * 3. In tablets, navigation bar is always placed at bottom of the screen.
     */
    private fun computeViewportRect(): Rect {
        //Log.v(LOG_TAG, "-> computeViewportRect");

        val viewportRect = Rect()
        rl_bottom!!.getGlobalVisibleRect(viewportRect)

        if (distractionFreeMode) viewportRect.left = 0
        viewportRect.top = getTopDistraction(DisplayUnit.PX)
        viewportRect.right = displayMetrics!!.widthPixels
       /* if (distractionFreeMode) {
            viewportRect.right = displayMetrics!!.widthPixels
        } else {
            viewportRect.right = displayMetrics!!.widthPixels - viewportRect.right
        }*/
        viewportRect.bottom = displayMetrics!!.heightPixels - getBottomDistraction(DisplayUnit.PX)

        return viewportRect
    }

    override fun getViewportRect(unit: DisplayUnit): Rect {

        val viewportRect = computeViewportRect()
        when (unit) {
            DisplayUnit.PX -> return viewportRect

            DisplayUnit.DP -> {
                viewportRect.left /= density.toInt()
                viewportRect.top /= density.toInt()
                viewportRect.right /= density.toInt()
                viewportRect.bottom /= density.toInt()
                return viewportRect
            }

            DisplayUnit.CSS_PX -> {
                viewportRect.left = ceil((viewportRect.left / density).toDouble()).toInt()
                viewportRect.top = ceil((viewportRect.top / density).toDouble()).toInt()
                viewportRect.right = ceil((viewportRect.right / density).toDouble()).toInt()
                viewportRect.bottom = ceil((viewportRect.bottom / density).toDouble()).toInt()
                return viewportRect
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }

    override fun getActivity(): WeakReference<FolioActivity> {
        return WeakReference(this)
    }

    @Deprecated("Deprecated in Java")
    override fun onSystemUiVisibilityChange(visibility: Int) {
        Log.v(LOG_TAG, "-> onSystemUiVisibilityChange -> visibility = $visibility")
        distractionFreeMode = visibility != View.SYSTEM_UI_FLAG_VISIBLE
        systemUiVisibilityChange()
//        if (actionBar != null) {
//            if (distractionFreeMode) {
//                actionBar!!.hide()
//            } else {
//                actionBar!!.show()
//            }
//        }
    }
    //显示隐藏应用工具栏
     fun systemUiVisibilityChange(){
         Log.v(LOG_TAG, "-> distractionFreeMode = $distractionFreeMode")
         if (distractionFreeMode) {
             rl_top!!.visibility = View.GONE
             rl_bottom!!.visibility = View.GONE
             rl_edit!!.visibility = View.GONE
             rl_mark_content!!.visibility = View.GONE
             tv_mark_content!!.text = ""
             rlMain!!.visibility = View.GONE
             niftySlider!!.visibility = View.GONE
             statusIcon(false, false, false, false)
             InputMethodUtils.close(window.decorView)
         } else {
           /*  val layoutParams = rl_top!!.getLayoutParams() as ConstraintLayout.LayoutParams
             layoutParams.topMargin = UiUtil.getStatusBarHeight(this)
             rl_top!!.setLayoutParams(layoutParams)*/
             rl_top!!.visibility = View.VISIBLE
             rl_bottom!!.visibility = View.VISIBLE
             //状态栏有内容是，笔记页面隐藏
             if(rlMain!!.visibility == View.VISIBLE){
                 rl_edit!!.visibility = View.GONE
                 rl_top!!.visibility = View.GONE
             }else{
               //  rl_edit!!.visibility = View.VISIBLE
                 niftySlider!!.visibility = View.VISIBLE
             }

         }
     }
    override fun toggleSystemUI() {

        if (distractionFreeMode) {
            showSystemUI()
        } else {
            hideSystemUI()
        }
        distractionFreeMode = !distractionFreeMode
        rl_top!!.visibility = View.GONE
        rl_bottom!!.visibility = View.GONE
        rl_edit!!.visibility = View.GONE
        rlMain!!.visibility = View.GONE
    }

    private fun showSystemUI() {
        Log.v(LOG_TAG, "-> showSystemUI")
        //停止滑动
        if(currentFragment != null &&  currentFragment!!.mWebview != null){
            currentFragment!!.mWebview!!.stopScroll = true
        }

        if(Build.VERSION.SDK_INT >= 30){
            val windowInsetsController = window.decorView.windowInsetsController
            windowInsetsController!!.show(WindowInsets.Type.statusBars())
            windowInsetsController!!.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)

        }
        else if (Build.VERSION.SDK_INT >= 16) {
            Log.v(LOG_TAG, "-> showSystemUI01")
            val decorView = window.decorView
            decorView.systemUiVisibility =View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        } else {
            Log.v(LOG_TAG, "-> showSystemUI02")
          //  window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
//            if (appBarLayout != null) appBarLayout!!.setTopMargin(statusBarHeight)
            onSystemUiVisibilityChange(View.SYSTEM_UI_FLAG_VISIBLE)
        }
    }

    private fun hideSystemUI() {
        Log.v(LOG_TAG, "-> hideSystemUI")
        //允许滑动
        if(currentFragment != null &&  currentFragment!!.mWebview != null){
            currentFragment!!.mWebview!!.stopScroll = false
        }
        if(Build.VERSION.SDK_INT >= 30){
            Log.v(LOG_TAG, "-> hideSystemUI01")
            val windowInsetsController = window.decorView.windowInsetsController
            windowInsetsController!!.hide(WindowInsets.Type.statusBars())
        }else if (Build.VERSION.SDK_INT >= 16) {
            Log.v(LOG_TAG, "-> hideSystemUI02")
            val decorView = window.decorView
            decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN)
        } else {
            Log.v(LOG_TAG, "-> hideSystemUI03")
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            // Specified 1 just to mock anything other than View.SYSTEM_UI_FLAG_VISIBLE
            onSystemUiVisibilityChange(1)
        }
    }

    override fun getEntryReadLocator(): ReadLocator? {
        if (entryReadLocator != null) {
            val tempReadLocator = entryReadLocator
            entryReadLocator = null
            return tempReadLocator
        }
        return null
    }

    /**
     * Go to chapter specified by href
     *
     * @param href http link or relative link to the page or to the anchor
     * @return true if href is of EPUB or false if other link
     */
    override fun goToChapter(href: String): Boolean {
        return goToChapter(href,null)
    }
    override fun goToChapter(href: String?, cfi: String?): Boolean {
        Log.v(LOG_TAG,"goToChapter->href:"+href+" cfi:"+cfi)
        for (link in spine!!) {
            if (href!!.contains(link.href!!)) {
                currentChapterIndex = spine!!.indexOf(link)
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment
                if(folioPageFragment != null){
                    folioPageFragment!!.scrollToFirst()
                    folioPageFragment.scrollToAnchorId(href!!)
                    if(cfi != null){
                        val handlerTime = Handler()
                        handlerTime.postDelayed({
                            folioPageFragment!!.scrollToCFI(cfi)
                        }, 1000)

                    }
                    rlMain!!.visibility = View.GONE
                    return true
                }else{
                    return false
                }

            }
        }

        return false
    }

    fun gotoPage(pageProgress: PageProgress?, progress: Float){
        if(pageProgress != null){
            currentChapterIndex = pageProgress.pageNumber

        }else if(progress == 0.0f){
            currentChapterIndex = 0
        }else if(progress == 100.0f){
            currentChapterIndex = mFolioPageFragmentAdapter!!.count-1
        }
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        val folioPageFragment = currentFragment
        if(folioPageFragment != null){
            folioPageFragment!!.scrollToProgress(progress)
        }


    }
    fun gotoChapterByNumber( chapterNumber:Int,pageNumber:Int ){
        currentChapterIndex = chapterNumber
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        Log.v(LOG_TAG,"gotoChapterByNumber-->chapterNumber:$chapterNumber-->pageNumber:$pageNumber-->"+currentFragment)
        if(currentFragment == null){
            handler!!.postDelayed({
                gotoChapterByNumber(chapterNumber,pageNumber)
            },500)
        }else{
            currentFragment!!.scrollToPage(pageNumber)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.SEARCH.value) {
            Log.v(LOG_TAG, "-> onActivityResult -> " + RequestCode.SEARCH)

            if (resultCode == Activity.RESULT_CANCELED) return

            searchAdapterDataBundle = data!!.getBundleExtra(SearchAdapter.DATA_BUNDLE)
            searchQuery = data.getCharSequenceExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY)

            if (resultCode == SearchActivity.ResultCode.ITEM_SELECTED.value) {

                searchLocator = data.getParcelableExtra(EXTRA_SEARCH_ITEM)
                // In case if SearchActivity is recreated due to screen rotation then FolioActivity
                // will also be recreated, so mFolioPageViewPager might be null.
                if (mFolioPageViewPager == null) return
                currentChapterIndex = getChapterIndex(HREF, searchLocator!!.href)
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment ?: return
                folioPageFragment.highlightSearchLocator(searchLocator!!)
                searchLocator = null
            }

        } else if (requestCode == RequestCode.CONTENT_HIGHLIGHT.value && resultCode == Activity.RESULT_OK && data!!.hasExtra(
                TYPE
            )
        ) {

            when (data.getStringExtra(TYPE)) {
                CHAPTER_SELECTED -> {
                    goToChapter(data.getStringExtra(SELECTED_CHAPTER_POSITION)!!,data.getStringExtra(CFI)!!)

                }
                HIGHLIGHT_SELECTED -> {
                    val highlightImpl = data.getParcelableExtra<HighlightImpl>(HIGHLIGHT_ITEM)
                    val markVo = data.getParcelableExtra<MarkVo>(NoteFragment.NODE_ITEM)
                    currentChapterIndex = getChapterIndex(HREF,markVo!!.href)
                    mFolioPageViewPager!!.currentItem = currentChapterIndex
                    val folioPageFragment = currentFragment ?: return
                    folioPageFragment.scrollToHighlightId(markVo.rangy)
                }
                BOOKMARK_SELECTED -> {
                    val bookmark =
                        data.getSerializableExtra(BOOKMARK_ITEM) as HashMap<String, String>
                    bookmarkReadLocator = ReadLocator.fromJson(bookmark["readlocator"].toString())
                    currentChapterIndex = getChapterIndex(bookmarkReadLocator)
                    mFolioPageViewPager!!.currentItem = currentChapterIndex
                    val folioPageFragment = currentFragment
                    val handlerTime = Handler()
                    handlerTime.postDelayed({
                        folioPageFragment!!.scrollToCFI(bookmarkReadLocator!!.locations.cfi.toString())
                    }, 1000)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)

        if (outState != null) outState!!.putSerializable(
            BUNDLE_READ_LOCATOR_CONFIG_CHANGE,
            lastReadLocator
        )

        val localBroadcastManager = LocalBroadcastManager.getInstance(this)
        localBroadcastManager.unregisterReceiver(searchReceiver)
        localBroadcastManager.unregisterReceiver(closeBroadcastReceiver)

        if (r2StreamerServer != null) r2StreamerServer!!.stop()

        if (isFinishing) {
            localBroadcastManager.sendBroadcast(Intent(FolioReader.ACTION_FOLIOREADER_CLOSED))
            FolioReader.get().retrofit = null
            FolioReader.get().r2StreamerApi = null
        }
        EventBus.getDefault().unregister(this)
    }

    override fun getCurrentChapterIndex(): Int {
        return currentChapterIndex
    }

    private fun configFolio() {

        mFolioPageViewPager = findViewById(R.id.folioPageViewPager)
        // Replacing with addOnPageChangeListener(), onPageSelected() is not invoked
        mFolioPageViewPager!!.addOnPageChangeListener(object :
            DirectionalViewpager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {
                Log.v(
                    LOG_TAG,
                    "-> onPageScrolled value -> DirectionalViewpager -> position = $position"
                )


            }

            override fun onPageSelected(position: Int) {
                Log.v(
                    LOG_TAG,
                    "-> onPageSelected value -> DirectionalViewpager -> position = $position"
                )

                EventBus.getDefault().post(
                    MediaOverlayPlayPauseEvent(
                        spine!![currentChapterIndex].href, false, true
                    )
                )
                mediaControllerFragment!!.setPlayButtonDrawable()
                currentChapterIndex = position
                pageTrackerViewModel.setCurrentChapter(position + 1)
                if(currentFragment != null){
                    //读取章节位置信息-校验是否有书签
                    Log.v(LOG_TAG,"ACTION_PAGE_MARK-->onPageSelected ")
                    currentFragment!!.getLastReadLocator(FolioReader.ACTION_CHECK_BOOKMARK +"|" +FolioReader.ACTION_READ_MARK)
                    //更新阅读进度条
                    currentFragment!!.updatePageProgress()
                    //更新阅读记录
                    currentFragment!!.updateReadRecord()
                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                Log.v(
                    LOG_TAG,
                    "-> onPageScrollStateChanged value -> DirectionalViewpager -> state = $state"
                )
                if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                    val position = mFolioPageViewPager!!.currentItem
                    Log.v(
                        LOG_TAG,
                        "-> onPageScrollStateChanged -> DirectionalViewpager -> " + "position = " + position +"-->pageCount:"+mFolioPageViewPager!!.adapter.count
                    )
                    var folioPageFragment =
                        mFolioPageFragmentAdapter!!.getItem(position - 1) as FolioPageFragment?
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToLast()
                        if (folioPageFragment.mWebview != null) folioPageFragment.mWebview!!.dismissPopupWindow()
                    }

                    folioPageFragment =
                        mFolioPageFragmentAdapter!!.getItem(position + 1) as FolioPageFragment?
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToFirst()
                        if (folioPageFragment.mWebview != null) folioPageFragment.mWebview!!.dismissPopupWindow()
                    }
                }
            }
        })

        mFolioPageViewPager!!.setDirection(direction)
       // mFolioPageViewPager!!.offscreenPageLimit = 5
        mFolioPageFragmentAdapter = FolioPageFragmentAdapter(
            supportFragmentManager, spine, bookFileName, mBookId, pageTrackerViewModel
        )
        mFolioPageViewPager!!.adapter = mFolioPageFragmentAdapter

        var pageProgressList = PageProgressTable.getAllPageProgress(mBookId,this)
        if(pageProgressList != null && pageProgressList.size > 0){
            mFolioPageFragmentAdapter!!.pageProgressesList = pageProgressList
            //有记录
            onCompletePageCalculate(pageProgressList)
        }else{
            //无记录，初始化
            initPageProgress()
        }
        // In case if SearchActivity is recreated due to screen rotation then FolioActivity
        // will also be recreated, so searchLocator is checked here.
        if (searchLocator != null) {

            currentChapterIndex = getChapterIndex(HREF, searchLocator!!.href)
            mFolioPageViewPager!!.currentItem = currentChapterIndex
            val folioPageFragment = currentFragment ?: return
            folioPageFragment.highlightSearchLocator(searchLocator!!)
            searchLocator = null

        } else {

            val readLocator: ReadLocator?
            if (savedInstanceState == null) {
                readLocator = intent.getParcelableExtra(EXTRA_READ_LOCATOR)
                entryReadLocator = readLocator
            } else {
                readLocator = savedInstanceState!!.getParcelable(BUNDLE_READ_LOCATOR_CONFIG_CHANGE)
                lastReadLocator = readLocator
            }
            currentChapterIndex = getChapterIndex(readLocator)
            if(currentChapterIndex == 0 && readBook != null){
                currentChapterIndex = readBook!!.chapterNumber!!
            }
            mFolioPageViewPager!!.currentItem = currentChapterIndex

        }

        LocalBroadcastManager.getInstance(this).registerReceiver(
            searchReceiver, IntentFilter(ACTION_SEARCH_CLEAR)
        )
    }

    private fun getChapterIndex(readLocator: ReadLocator?): Int {

        if (readLocator == null) {
            return 0
        } else if (!TextUtils.isEmpty(readLocator.href)) {
            return getChapterIndex(HREF, readLocator.href)
        }

        return 0
    }

    private fun getChapterIndex(caseString: String, value: String): Int {
        for (i in spine!!.indices) {
            when (caseString) {
                HREF -> if (spine!![i].href == value) return i
            }
        }
        return 0
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
        Log.v(LOG_TAG, "-> onSaveInstanceState")
        this.outState = outState

        outState.putBoolean(BUNDLE_DISTRACTION_FREE_MODE, distractionFreeMode)
        outState.putBundle(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
        outState.putCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery)
    }

    override fun storeLastReadLocator(lastReadLocator: ReadLocator) {
        Log.v(LOG_TAG, "-> storeLastReadLocator")
        this.lastReadLocator = lastReadLocator
    }

    private fun setConfig(savedInstanceState: Bundle?) {

        var config: Config?
        val intentConfig = this.config
        val overrideConfig = folioReader!!.overrideConfig
        val savedConfig = AppUtil.getSavedConfig(this)

        config = if (savedInstanceState != null) {
            savedConfig

        } else if (savedConfig == null) {
            intentConfig ?: Config()

        } else {
            if (intentConfig != null && overrideConfig) {
                intentConfig
            } else {
                savedConfig
            }
        }

        // Code would never enter this if, just added for any unexpected error
        // and to avoid lint warning
        if (config == null) config = Config()

        AppUtil.saveConfig(this, config)
        direction = config.direction
    }

    override fun play() {
        EventBus.getDefault().post(
            MediaOverlayPlayPauseEvent(
                spine!![currentChapterIndex].href, true, false
            )
        )
    }

    override fun pause() {
        EventBus.getDefault().post(
            MediaOverlayPlayPauseEvent(
                spine!![currentChapterIndex].href, false, false
            )
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupBook()
            } else {
                Toast.makeText(
                    this, getString(R.string.cannot_access_epub_message), Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }
    }

    override fun getDirection(): Config.Direction {
        return direction
    }

    private fun clearSearchLocator() {
        Log.v(LOG_TAG, "-> clearSearchLocator")

        val fragments = mFolioPageFragmentAdapter!!.fragments
        for (i in fragments.indices) {
            val folioPageFragment = fragments[i] as FolioPageFragment?
            folioPageFragment?.clearSearchLocator()
        }

        val savedStateList = mFolioPageFragmentAdapter!!.savedStateList
        if (savedStateList != null) {
            for (i in savedStateList.indices) {
                val savedState = savedStateList[i]
                val bundle = FolioPageFragmentAdapter.getBundleFromSavedState(savedState)
                bundle?.putParcelable(FolioPageFragment.BUNDLE_SEARCH_LOCATOR, null)
            }
        }
    }
    //接收阅读位置信息
    override fun saveReadLocator(readLocator: ReadLocator, mBookId: String?, markType: String) {
        Log.i(
            LOG_TAG,
            "-> saveReadLocator -> " + readLocator.toJson() + " markType:" + markType
        )
        //收到获取阅读位置信息
        val cfi = readLocator.href + readLocator.locations.cfi
        if (FolioReader.EXTRA_BOOKMARK_ADD == markType) { //添加标签
            val insertResult = BookmarkTable(this).insertBookmark(
                mBookId,
                readLocator.title,
                null,
                currentChapterIndex,
                readLocator.toJson().toString(),
                cfi,BookmarkTable.MARK_TYPE
            )
            if (insertResult) {
                Toast.makeText(
                    this, "已添加到书签", Toast.LENGTH_SHORT
                ).show()
            }
        } else if (FolioReader.EXTRA_BOOKMARK_DELETE == markType) { //删除标签
            val bookmarkId = BookmarkTable.getBookmarkIdByCfi(cfi, mBookId,BookmarkTable.MARK_TYPE, this)
            if (bookmarkId != -1) {
                val deleteResult = BookmarkTable.deleteBookmarkById(bookmarkId, this)
                if (deleteResult) {
                    Toast.makeText(
                        this, "已删除书签", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onFolioReaderClosed() {
        Log.v(LOG_TAG,"onFolioReaderClosed")
    }

    override fun onHighlight(highlight: HighLight?, type: HighLight.HighLightAction?) {
        Log.v(LOG_TAG,"onHighlight")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun noteSelect(noteSelectEvent: NoteSelectEvent){
        var markVo = noteSelectEvent.markVo;
        if(markVo != null){
            currentChapterIndex = markVo.pageNumber;
            mFolioPageViewPager!!.currentItem = currentChapterIndex
            val folioPageFragment = currentFragment ?: return
            if(markVo.rangy != null){
                folioPageFragment.scrollToHighlightId(markVo.rangy)
            }
            if(markVo.cfi != null){
                val handlerTime = Handler()
                handlerTime.postDelayed({
                    folioPageFragment!!.scrollToCFI(markVo.cfi)
                }, 1000)
            }
            hideSystemUI()
        }

    }

    fun initNoteEditView(){
        viewNoteEdit = LayoutInflater.from(this).inflate(R.layout.dialog_folio_bookmark, null)
    }

    //写笔记请求
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun writeHighlightNote(highlightNoteEvent: HighlightNoteEvent ){
        val noteDetailFragment = AddNoteFragment(null,null,null,highlightNoteEvent.content,MarkVo.HighlightMarkType,currentFragment!!,null)
        noteDetailFragment.show(supportFragmentManager,"")

        //
        rl_edit!!.visibility = View.GONE
        rl_top!!.visibility = View.GONE
        rl_bottom!!.visibility = View.GONE
        rl_mark_content!!.visibility = View.GONE
        tv_mark_content!!.text = highlightNoteEvent.content
        rlMain!!.visibility = View.GONE
        niftySlider!!.visibility = View.GONE
     /*   et_page_note!!.requestFocus()
        InputMethodUtils.show(et_page_note)*/
    }

    override fun onError() {
    }

    /**完成页面进度计算**/
    override fun onCompletePageCalculate(pageProgressList: MutableList<PageProgress>?) {
        if(pageProgressList != null && pageProgressList.size > 0){
            if(mFolioPageFragmentAdapter != null && mFolioPageFragmentAdapter!!.fragments.size == pageProgressList.size){
                //初始化页面
                for (i in pageProgressList.indices){
                   var fragment = mFolioPageFragmentAdapter!!.fragments[i]
                    if(fragment != null){
                        fragment.pageProgress = pageProgressList[i];
                    }

                }
            }else{
                Log.v(LOG_TAG,"页面进度计算结果与现有章节数对应不上")
            }

        }
    }

    fun initScreenLight(light: Int){
        val layoutParams: WindowManager.LayoutParams = getWindow().getAttributes()
        layoutParams.screenBrightness = light / 255f //因为这个值是[0, 1]范围的
        getWindow().setAttributes(layoutParams)

    }

}
